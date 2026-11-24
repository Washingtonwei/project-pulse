package team.projectpulse.ram.llm;

import io.micrometer.context.ContextRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * {@link LlmProxy} against a fake {@link LlmClient}, so no network is involved. Covers the design's
 * test rows for SI-llm-json, SI-llm-degradation, FR-AI-degradation, "Provider failure stays inside",
 * SI-llm-credentials, and the log rows of SEC-llm-data-minimization / CO-ferpa (docs/design/ai.md).
 */
@ExtendWith(OutputCaptureExtension.class)
class LlmProxyTest {

    /** Words that stand for student content; none may appear in a log line. */
    static final String SECRET_INSTRUCTIONS = "INSTRUCTIONS-Alice";
    static final String SECRET_INPUT = "INPUT-alice@abc.edu";
    static final String SECRET_REPLY = "REPLY-reset-password";
    static final String SECRET_PROVIDER_BODY = "PROVIDER-BODY-echoes-INPUT-alice@abc.edu";

    static final LlmRequest REQUEST = new LlmRequest(
            "test-caller", SECRET_INSTRUCTIONS, SECRET_INPUT, LlmRequestTest.SCHEMA, 50, Duration.ofSeconds(2));

    /** A fake LLM client whose behavior each test sets; counts how often it was called. */
    static final class FakeClient implements LlmClient {
        boolean configured = true;
        Function<LlmRequest, ProviderReply> behavior = r -> new ProviderReply("{\"slug\":\"" + SECRET_REPLY + "\"}", false, 12, 5);
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public ProviderReply send(LlmRequest request) {
            calls.incrementAndGet();
            return behavior.apply(request);
        }
    }

    /** A reply that arrives after 5 s, unless the proxy cancels the call first. */
    static ProviderReply slowReply(LlmRequest request) {
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // the proxy cancelled us; this reply is never read
        }
        return new ProviderReply("{\"slug\":\"late\"}", false, 1, 1);
    }

    final FakeClient client = new FakeClient();
    final LlmProxy proxy = new LlmProxy(Optional.of(client), Duration.ofSeconds(15), JsonMapper.builder().build());

    @AfterEach
    void closeProxy() {
        proxy.close();
    }

    // --- SI-llm-json -------------------------------------------------------------------------

    @Test
    @DisplayName("A reply that parses as a JSON object returns Answered with that JSON")
    void answered() {
        LlmResult result = proxy.complete(REQUEST);

        assertThat(result).isInstanceOf(LlmResult.Answered.class);
        assertThat(((LlmResult.Answered) result).json().path("slug").asString()).isEqualTo(SECRET_REPLY);
    }

    @Test
    @DisplayName("Text that is not a JSON object is BAD_OUTPUT")
    void notAnObject() {
        for (String text : new String[] {"Sure! Here is a key: reset-password", "[1,2]", "\"slug\"", ""}) {
            client.behavior = r -> new ProviderReply(text, false, 1, 1);

            assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.BAD_OUTPUT));
        }
    }

    @Test
    @DisplayName("A reply marked refused (declined, or cut short at the token cap) is BAD_OUTPUT, even if its text parses")
    void refused() {
        client.behavior = r -> new ProviderReply("{\"slug\":\"x\"}", true, 1, 50);

        assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.BAD_OUTPUT));
    }

    @Test
    @DisplayName("A null reply (a broken LLM client) is BAD_OUTPUT, not a NullPointerException")
    void nullReply() {
        client.behavior = r -> null;

        assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.BAD_OUTPUT));
    }

    // --- SI-llm-degradation, timeout ---------------------------------------------------------

    @Test
    @DisplayName("A reply slower than the request's timeout is TIMEOUT within the deadline, and the call is cancelled")
    void timeout() {
        CompletableFuture<Boolean> interrupted = new CompletableFuture<>();
        client.behavior = r -> {
            ProviderReply late = slowReply(r);
            interrupted.complete(Thread.currentThread().isInterrupted());
            return late;
        };
        LlmRequest fast = new LlmRequest("test-caller", "x", "", LlmRequestTest.SCHEMA, 50, Duration.ofMillis(200));

        long start = System.nanoTime();
        LlmResult result = proxy.complete(fast);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(result).isEqualTo(new LlmResult.Unavailable(UnavailableReason.TIMEOUT));
        assertThat(elapsedMs).isLessThan(1_000);
        // The proxy cancelled the call: the fake's 5 s sleep was interrupted well before it ended.
        assertThat(interrupted).succeedsWithin(Duration.ofSeconds(1)).isEqualTo(true);
    }

    // --- SI-llm-degradation, FR-AI-degradation, down; provider failure stays inside -----------

    static Stream<RuntimeException> providerFailures() {
        byte[] body = SECRET_PROVIDER_BODY.getBytes(StandardCharsets.UTF_8);
        return Stream.of(
                new ResourceAccessException("I/O error: Connection refused " + SECRET_PROVIDER_BODY),
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, body, StandardCharsets.UTF_8),
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, body, StandardCharsets.UTF_8),
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, body, StandardCharsets.UTF_8),
                HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, body, StandardCharsets.UTF_8),
                HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, body, StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("providerFailures")
    @DisplayName("A connection error and every 4xx/5xx are DOWN, and never escape")
    void down(RuntimeException failure) {
        client.behavior = r -> {
            throw failure;
        };

        assertThatCode(() -> assertThat(proxy.complete(REQUEST))
                .isEqualTo(new LlmResult.Unavailable(UnavailableReason.DOWN)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("After close() (context shutdown) a call is DOWN, not a RejectedExecutionException")
    void afterClose() {
        proxy.close();

        assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.DOWN));
        assertThat(client.calls).hasValue(0);
    }

    // --- SI-llm-credentials, not configured --------------------------------------------------

    @Test
    @DisplayName("With no LLM client at all, the answer is NOT_CONFIGURED")
    void noLlmClient() {
        try (LlmProxy p = new LlmProxy(Optional.empty(), Duration.ofSeconds(15), JsonMapper.builder().build())) {
            assertThat(p.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        }
    }

    @Test
    @DisplayName("With an LLM client that has no key, the answer is NOT_CONFIGURED and send is never called")
    void llmClientNotConfigured() {
        client.configured = false;

        assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        assertThat(client.calls).hasValue(0);
    }

    @Test
    @DisplayName("A null timeout uses the proxy's default")
    void defaultTimeout() {
        client.behavior = LlmProxyTest::slowReply;
        LlmRequest noTimeout = new LlmRequest("test-caller", "x", "", LlmRequestTest.SCHEMA, 50, null);
        try (LlmProxy p = new LlmProxy(Optional.of(client), Duration.ofMillis(200), JsonMapper.builder().build())) {
            assertThat(p.complete(noTimeout)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.TIMEOUT));
        }
    }

    // --- Tracing ---------------------------------------------------------------------------

    static final ThreadLocal<String> REQUEST_CONTEXT = new ThreadLocal<>();

    @Test
    @DisplayName("The call's thread sees the request thread's context, which is how the trace reaches the outbound call")
    void contextPropagates() {
        ContextRegistry registry = ContextRegistry.getInstance();
        registry.registerThreadLocalAccessor("llm-proxy-test", REQUEST_CONTEXT::get, REQUEST_CONTEXT::set, REQUEST_CONTEXT::remove);
        AtomicReference<String> seen = new AtomicReference<>();
        client.behavior = r -> {
            seen.set(REQUEST_CONTEXT.get());
            return new ProviderReply("{}", false, 0, 0);
        };
        try {
            REQUEST_CONTEXT.set("trace-42");
            proxy.complete(REQUEST);
        } finally {
            REQUEST_CONTEXT.remove();
            registry.removeThreadLocalAccessor("llm-proxy-test");
        }

        assertThat(seen).hasValue("trace-42");
    }

    // --- SEC-llm-data-minimization, CO-ferpa, logs -------------------------------------------

    @Test
    @DisplayName("An answered call logs one line with caller, outcome, latency, and tokens, and no content")
    void answeredLogLine(CapturedOutput output) {
        proxy.complete(REQUEST);

        assertThat(output).containsOnlyOnce("LLM call caller=test-caller outcome=ANSWERED latencyMs=");
        assertThat(output).contains("inputTokens=12 outputTokens=5");
        assertThat(output).doesNotContain(SECRET_INSTRUCTIONS, SECRET_INPUT, SECRET_REPLY);
    }

    @Test
    @DisplayName("A failed call logs the exception class and status, and never the provider's message")
    void failedLogLine(CapturedOutput output) {
        client.behavior = r -> {
            throw HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null,
                    SECRET_PROVIDER_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        };

        proxy.complete(REQUEST);

        assertThat(output).containsOnlyOnce("LLM call caller=test-caller outcome=DOWN latencyMs=");
        assertThat(output).contains("exception=org.springframework.web.client.HttpClientErrorException$Unauthorized status=401");
        assertThat(output).contains("ERROR"); // a 401 needs someone to act: the key is bad
        assertThat(output).doesNotContain(SECRET_INSTRUCTIONS, SECRET_INPUT, SECRET_PROVIDER_BODY);
    }

}
