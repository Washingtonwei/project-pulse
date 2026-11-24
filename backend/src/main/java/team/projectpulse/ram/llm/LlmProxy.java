package team.projectpulse.ram.llm;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The AI proxy: the only code in Project Pulse that talks to the external LLM service
 * (CO-server-side-llm-proxy). Every AI feature calls {@link #complete} and handles the
 * {@link LlmResult}. The reasons behind each choice are in {@code docs/design/ai.md}, "AI proxy".
 * <pre>
 * no LLM client, or not configured        -&gt; NOT_CONFIGURED   no network call
 * send() on a virtual thread, wait up to the request's timeout:
 *   deadline passes                       -&gt; TIMEOUT          the call is cancelled
 *   send() throws (connection, 4xx, 5xx)  -&gt; DOWN
 *   reply refused, or not a JSON object   -&gt; BAD_OUTPUT
 *   reply is a JSON object                -&gt; Answered(json)
 * </pre>
 * Three stories to check the table against:
 * <ul>
 * <li><b>A dev laptop with no key:</b> {@code NOT_CONFIGURED} at once, one debug line; the key
 *     suggestion shows no suggestion.</li>
 * <li><b>OpenAI is slow:</b> a key suggestion asks for 3 s and OpenAI takes 4 s. At 3 s the call is
 *     cancelled and the answer is {@code TIMEOUT}; the 4 s reply is never read.</li>
 * <li><b>Someone rotated the API key:</b> OpenAI answers 401, {@code send()} throws, and the answer is
 *     {@code DOWN}, logged at error because someone must fix the key. The student sees "AI unavailable"
 *     and stays logged in.</li>
 * </ul>
 * There is no circuit breaker, on purpose (OI-84): each call stands alone.
 *
 * <p>Three rules for anyone changing this class:
 * <ul>
 * <li><b>No exception escapes {@link #complete}.</b> A provider's 401 that reached the browser would
 *     log the student out, because the frontend reads every 401 as an expired session.</li>
 * <li><b>Logs carry identifiers, never content (CO-ferpa).</b> One line per call: caller, outcome,
 *     latency, tokens, and on failure the exception's class and HTTP status. Never the input, the
 *     reply, or an exception's message, which can quote the provider's response body.</li>
 * <li><b>A call that times out is cancelled,</b> so an abandoned HTTP call does not keep running on
 *     its thread after the caller has moved on.</li>
 * </ul>
 */
public class LlmProxy implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmProxy.class);

    private final LlmClient client;
    private final Duration defaultTimeout;
    private final JsonMapper jsonMapper;

    /**
     * One virtual thread per call, so the request thread can stop waiting at its own deadline (the HTTP
     * client has one read timeout for every caller, 20 s, kept only as a backstop: it sits above the 15 s
     * longest deadline, so a hang is always TIMEOUT here, never a DOWN from the HTTP client). The wrapper copies
     * the request's trace context onto that thread, so the OpenAI call shows up inside the student's
     * request trace. Closed, interrupting any running calls, on context shutdown.
     */
    private final ExecutorService executor = ContextExecutorService.wrap(
            Executors.newVirtualThreadPerTaskExecutor(), ContextSnapshotFactory.builder().build());

    /**
     * Built by {@link LlmProxyConfiguration}, and directly by tests. The client is an {@code Optional}
     * because in dev no LLM client bean exists, so Spring has nothing to inject; it is stored as null,
     * which the first check in {@link #complete} tests for.
     *
     * @param client         the selected provider's LLM client, or empty when {@code ai.llm.provider} is unset
     * @param defaultTimeout the deadline for a request whose timeout is null
     * @param jsonMapper     parses the reply text
     */
    LlmProxy(Optional<LlmClient> client, Duration defaultTimeout, JsonMapper jsonMapper) {
        this.client = client.orElse(null);
        this.defaultTimeout = defaultTimeout;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Sends the request to the configured LLM service and returns its answer, or why there is none.
     * Blocks the calling thread for at most the request's timeout (plus a few milliseconds).
     *
     * @param request what to send; build it with the {@link LlmRequest} constructor, which rejects a bad one
     * @return {@link LlmResult.Answered} with the reply as a JSON object, or {@link LlmResult.Unavailable}
     */
    public LlmResult complete(LlmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        String caller = request.caller();
        long start = System.nanoTime();

        // Step 1: can we call at all? With no LLM client (dev, tests), or one missing its key or model,
        // answer at once; nothing touches the network.
        if (client == null || !client.isConfigured()) {
            LOGGER.debug("LLM call caller={} outcome=NOT_CONFIGURED", caller);
            return unavailable(UnavailableReason.NOT_CONFIGURED);
        }

        // Step 2: pick the deadline: the request's own, or the 15 s default when it names none.
        Duration timeout = request.timeout() != null ? request.timeout() : defaultTimeout;

        // Step 3: start the call on its own virtual thread. submit() returns at once with a Future, a
        // receipt for a result that is not there yet, so this thread is free to decide how long to wait.
        Future<ProviderReply> call;
        try {
            call = executor.submit(() -> client.send(request));
        } catch (RejectedExecutionException e) { // only after close(), at shutdown; logged as class only
            return down(caller, start, e);
        }

        // Step 4: wait, but not forever. get() returns the reply, or ends in one of three ways.
        ProviderReply reply;
        try {
            reply = call.get(timeout.toNanos(), TimeUnit.NANOSECONDS); // This line blocks the request thread for at most the request's timeout (plus a few milliseconds).
        } catch (TimeoutException e) { // the deadline passed; logged without the exception object
            call.cancel(true); // interrupts the HTTP call (rule 3); a reply arriving later is never read
            long latency = elapsedMillis(start);
            long timeoutMs = timeout.toMillis();
            LOGGER.warn("LLM call caller={} outcome=TIMEOUT latencyMs={} timeoutMs={}", caller, latency, timeoutMs);
            return unavailable(UnavailableReason.TIMEOUT);
        } catch (ExecutionException e) { // logged as class and status only (rule 2)
            // send() threw (connection refused, a 401, a 500...). It ran on another thread, so Java
            // cannot throw its exception here directly: it arrives wrapped, and getCause() is the real one.
            return down(caller, start, e.getCause());
        } catch (InterruptedException e) { // logged as class only
            // This thread itself was interrupted (the server is shutting down): stop the call, and set
            // the interrupt flag back on for whoever interrupted us.
            call.cancel(true);
            Thread.currentThread().interrupt();
            return down(caller, start, e);
        }

        // Step 5: a reply arrived in time; judge it. A refusal, or a reply cut short at the token cap,
        // is not parsed at all. Otherwise the text must be a JSON object: prose, an array, or a bare
        // string is BAD_OUTPUT too.
        long latency = elapsedMillis(start);
        if (reply == null) { // a broken LLM client; reading it would throw out of complete (rule 1)
            LOGGER.warn("LLM call caller={} outcome=BAD_OUTPUT latencyMs={} reply=null", caller, latency);
            return unavailable(UnavailableReason.BAD_OUTPUT);
        }
        int inputTokens = reply.inputTokens();
        int outputTokens = reply.outputTokens();
        boolean refused = reply.refused();
        JsonNode json = refused ? null : parseObject(reply.text());
        if (json == null) {
            // incompleteReason tells a reply cut short (max_output_tokens) from a refusal or non-JSON text (-)
            String incompleteReason = reply.incompleteReason() == null ? "-" : reply.incompleteReason();
            LOGGER.warn("LLM call caller={} outcome=BAD_OUTPUT latencyMs={} inputTokens={} outputTokens={} refused={} incompleteReason={}",
                    caller, latency, inputTokens, outputTokens, refused, incompleteReason);
            return unavailable(UnavailableReason.BAD_OUTPUT);
        }
        LOGGER.info("LLM call caller={} outcome=ANSWERED latencyMs={} inputTokens={} outputTokens={}",
                caller, latency, inputTokens, outputTokens);
        return new LlmResult.Answered(json);
    }

    /**
     * Stops the executor on context shutdown, interrupting any call still waiting on the network.
     */
    @Override
    public void close() {
        executor.shutdownNow();
    }

    /**
     * Parses the reply text and returns it only if it is a JSON object, else null.
     * {@code {"slug":"reset-password"}} is returned; {@code "reset-password"}, {@code [1,2]},
     * {@code Sure! Here is...}, and an empty string are all null.
     */
    private JsonNode parseObject(String text) {
        try {
            JsonNode node = jsonMapper.readTree(text);
            return node != null && node.isObject() ? node : null;
        } catch (
                JacksonException e) { // the message would quote the reply text (CO-ferpa); BAD_OUTPUT is logged by the caller
            return null;
        }
    }

    /**
     * Logs a {@code DOWN} and returns it. The level is {@code error} when someone usually must act (400,
     * 401, 403, 404: a rejected schema, a bad key, a wrong model, which fail every call alike) and
     * {@code warn} when it may pass on its own (a connection error, 429, 5xx). A 400 can also come from
     * one input (an input too long for the model's context), so a lone 400 among answered calls points
     * at that input, not at the deploy. Seen live on 2026-11-24: a bad key is 401, a misspelled model 404.
     * Logs the cause's class name and HTTP status only, never its message (see the class comment), which
     * for the 401 quotes part of the key.
     * Example line: {@code LLM call caller=critique outcome=DOWN latencyMs=230
     * exception=org.springframework.web.client.HttpClientErrorException$Unauthorized status=401}.
     */
    private static LlmResult down(String caller, long start, Throwable cause) {
        Integer status = cause instanceof RestClientResponseException http ? http.getStatusCode().value() : null;
        boolean needsAction = status != null && (status == 400 || status == 401 || status == 403 || status == 404);
        LOGGER.atLevel(needsAction ? Level.ERROR : Level.WARN)
                .log("LLM call caller={} outcome=DOWN latencyMs={} exception={} status={}",
                        caller, elapsedMillis(start), cause == null ? "unknown" : cause.getClass().getName(),
                        status == null ? "-" : status);
        return unavailable(UnavailableReason.DOWN);
    }

    private static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private static LlmResult unavailable(UnavailableReason reason) {
        return new LlmResult.Unavailable(reason);
    }

}
