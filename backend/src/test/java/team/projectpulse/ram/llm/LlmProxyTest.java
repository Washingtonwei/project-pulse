package team.projectpulse.ram.llm;

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
import java.time.InstantSource;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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
            "test-feature", SECRET_INSTRUCTIONS, SECRET_INPUT, LlmRequestTest.SCHEMA, 50, Duration.ofSeconds(2));

    /** A fake adapter whose behavior each test sets; counts how often it was called. */
    static final class FakeClient implements LlmClient {
        boolean configured = true;
        Function<LlmRequest, LlmReply> behavior = r -> new LlmReply("{\"slug\":\"" + SECRET_REPLY + "\"}", false, 12, 5);
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public LlmReply send(LlmRequest request) {
            calls.incrementAndGet();
            return behavior.apply(request);
        }
    }

    final FakeClient client = new FakeClient();
    // A high threshold, so these tests can count failures without the breaker opening.
    final LlmCircuitBreaker breaker = new LlmCircuitBreaker(InstantSource.system(), 100, Duration.ofSeconds(30));
    final LlmProxy proxy = new LlmProxy(Optional.of(client), breaker, Duration.ofSeconds(15), JsonMapper.builder().build());

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
    @DisplayName("Text that is not a JSON object is BAD_OUTPUT, and the reply still resets the breaker's count")
    void notAnObject() {
        breaker.recordFailure(); // one earlier failure, to show the reset
        for (String text : new String[] {"Sure! Here is a key: reset-password", "[1,2]", "\"slug\"", ""}) {
            client.behavior = r -> new LlmReply(text, false, 1, 1);

            assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.BAD_OUTPUT));
            assertThat(breaker.consecutiveFailures()).isZero();
        }
    }

    @Test
    @DisplayName("A reply marked refused (declined, or cut short at the token cap) is BAD_OUTPUT, even if its text parses")
    void refused() {
        client.behavior = r -> new LlmReply("{\"slug\":\"x\"}", true, 1, 50);

        assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.BAD_OUTPUT));
        assertThat(breaker.consecutiveFailures()).isZero();
    }

    // --- SI-llm-degradation, timeout ---------------------------------------------------------

    @Test
    @DisplayName("A reply slower than the request's timeout is TIMEOUT, within the deadline, and the late reply changes nothing")
    void timeout() throws InterruptedException {
        client.behavior = r -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // the proxy cancelled us; this reply is never read
            }
            return new LlmReply("{\"slug\":\"late\"}", false, 1, 1);
        };
        LlmRequest fast = new LlmRequest("test-feature", "x", "", LlmRequestTest.SCHEMA, 50, Duration.ofMillis(200));

        long start = System.nanoTime();
        LlmResult result = proxy.complete(fast);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(result).isEqualTo(new LlmResult.Unavailable(UnavailableReason.TIMEOUT));
        assertThat(elapsedMs).isLessThan(1_000);
        assertThat(breaker.consecutiveFailures()).isEqualTo(1);

        Thread.sleep(300); // give the cancelled task time to finish; it must not record a success
        assertThat(breaker.consecutiveFailures()).isEqualTo(1);
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
    @DisplayName("A connection error and every 4xx/5xx are DOWN, count as a breaker failure, and never escape")
    void down(RuntimeException failure) {
        client.behavior = r -> {
            throw failure;
        };

        assertThatCode(() -> assertThat(proxy.complete(REQUEST))
                .isEqualTo(new LlmResult.Unavailable(UnavailableReason.DOWN)))
                .doesNotThrowAnyException();
        assertThat(breaker.consecutiveFailures()).isEqualTo(1);
    }

    @Test
    @DisplayName("An open breaker answers DOWN without calling the provider")
    void breakerOpen() {
        LlmCircuitBreaker tripped = new LlmCircuitBreaker(InstantSource.system(), 1, Duration.ofMinutes(5));
        tripped.recordFailure();
        try (LlmProxy p = new LlmProxy(Optional.of(client), tripped, Duration.ofSeconds(15), JsonMapper.builder().build())) {
            assertThat(p.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.DOWN));
        }
        assertThat(client.calls).hasValue(0);
    }

    // --- SI-llm-credentials, not configured --------------------------------------------------

    @Test
    @DisplayName("With no adapter at all, the answer is NOT_CONFIGURED")
    void noAdapter() {
        try (LlmProxy p = new LlmProxy(Optional.empty(), breaker, Duration.ofSeconds(15), JsonMapper.builder().build())) {
            assertThat(p.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        }
        assertThat(breaker.consecutiveFailures()).isZero();
    }

    @Test
    @DisplayName("With an adapter that has no key, the answer is NOT_CONFIGURED, send is never called, and the breaker stays closed")
    void adapterNotConfigured() {
        client.configured = false;

        for (int i = 0; i < 5; i++) {
            assertThat(proxy.complete(REQUEST)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        }
        assertThat(client.calls).hasValue(0);
        assertThat(breaker.allowsCall()).isTrue();
        assertThat(breaker.consecutiveFailures()).isZero();
    }

    @Test
    @DisplayName("A null timeout uses the proxy's default")
    void defaultTimeout() {
        client.behavior = r -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new LlmReply("{}", false, 0, 0);
        };
        LlmRequest noTimeout = new LlmRequest("test-feature", "x", "", LlmRequestTest.SCHEMA, 50, null);
        try (LlmProxy p = new LlmProxy(Optional.of(client), breaker, Duration.ofMillis(200), JsonMapper.builder().build())) {
            assertThat(p.complete(noTimeout)).isEqualTo(new LlmResult.Unavailable(UnavailableReason.TIMEOUT));
        }
    }

    // --- SEC-llm-data-minimization, CO-ferpa, logs -------------------------------------------

    @Test
    @DisplayName("An answered call logs one line with feature, outcome, latency, and tokens, and no content")
    void answeredLogLine(CapturedOutput output) {
        proxy.complete(REQUEST);

        assertThat(output).containsOnlyOnce("LLM call feature=test-feature outcome=ANSWERED latencyMs=");
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

        assertThat(output).containsOnlyOnce("LLM call feature=test-feature outcome=DOWN latencyMs=");
        assertThat(output).contains("exception=org.springframework.web.client.HttpClientErrorException$Unauthorized status=401");
        assertThat(output).contains("ERROR"); // a 401 needs someone to act: the key is bad
        assertThat(output).doesNotContain(SECRET_INSTRUCTIONS, SECRET_INPUT, SECRET_PROVIDER_BODY);
    }

}
