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
 * {@link LlmResult} it gets back. Design: {@code docs/design/ai.md}, "AI proxy".
 *
 * <p>The order of checks in {@link #complete}, and what each outcome does to the circuit breaker:
 * <pre>
 * no adapter, or adapter not configured  -&gt; NOT_CONFIGURED   breaker untouched, no network call
 * breaker refuses                        -&gt; DOWN             no network call
 * send() on a virtual thread, then wait up to the request's timeout:
 *   deadline passes, caller waited the full default -&gt; TIMEOUT   recordFailure, task cancelled
 *   deadline passes, caller chose a shorter one     -&gt; TIMEOUT   release (counts nothing), task cancelled
 *   send() throws (connection, 4xx, 5xx)            -&gt; DOWN      recordFailure
 *   reply arrives                                              recordSuccess (the service answered), then
 *     no reply object, refused, or not a JSON object -&gt; BAD_OUTPUT
 *     text is a JSON object                          -&gt; Answered(json)
 * </pre>
 * Example: a key suggestion with a 3 s timeout while OpenAI takes 4 s. At 3 s the proxy stops waiting,
 * cancels the task, and returns {@code Unavailable(TIMEOUT)}. It counts no breaker failure, because the
 * caller ran out of patience before the service did: otherwise three suggestions on a slow but healthy
 * provider would open the breaker, and an assistant call with a 15 s budget would then get {@code DOWN}
 * for 30 s. If OpenAI's reply turns up at 4 s, nothing reads it and nothing is recorded. A timeout at the
 * full default (15 s) does count: the service could not answer within the longest wait any caller gets.
 *
 * <p><b>Why a virtual thread and not the HTTP client's read timeout:</b> a read timeout is set once
 * per HTTP client, but each caller asks for its own deadline (a suggestion wants 3 s, an assistant
 * up to 15 s). So the proxy enforces the per-call deadline itself; the client's 15 s read timeout is
 * only the backstop (PER-ai-response-time).
 *
 * <p><b>Nothing escapes.</b> No exception leaves {@link #complete} except the
 * {@link IllegalArgumentException} that {@link LlmRequest} throws while a caller builds a bad request.
 * That matters: a provider's 401 (a bad or rotated key) must never become a 401 to the browser,
 * because the frontend treats every 401 as an expired session and logs the student out.
 *
 * <p><b>Logging (CO-ferpa):</b> one line per call with the feature, outcome, latency, token counts,
 * and on failure the exception's class name and HTTP status. Never the instructions, the input, the
 * reply, or the exception message: a Spring HTTP client exception's message carries the provider's
 * response body, which can echo the input. Levels: {@code info} answered; {@code warn} timeout, bad
 * output, and a transient {@code DOWN} (connection error, 429, 5xx); {@code error} for a 400, 401,
 * 403 or 404, which fail every call alike and need someone to act (a rejected schema, a bad key, a
 * wrong model name); {@code debug} for not configured and for a breaker that refused.
 *
 * <p><b>Tracing:</b> the call runs on another thread, so the executor carries the request's context
 * (the current Micrometer observation, which holds the trace and the log line's {@code [traceId-spanId]})
 * over to it. The outbound OpenAI call then appears as a child of the student's request in Zipkin and
 * Application Insights, not as a separate trace.
 *
 * <p><b>Known limit:</b> if the request thread itself is interrupted while it waits (only at shutdown),
 * that counts as a breaker failure though the provider did nothing wrong. It is left that way on purpose:
 * it needs a shutdown in the middle of a call, and the breaker recovers on its own 30 s later.
 */
public class LlmProxy implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmProxy.class);

    private final LlmClient client;
    private final LlmCircuitBreaker breaker;
    private final Duration defaultTimeout;
    private final JsonMapper jsonMapper;

    /**
     * One virtual thread per call: the thread blocks on the HTTP call so the request thread can stop
     * waiting at the deadline. Wrapped so each task runs with the submitting thread's context (see
     * Tracing in the class comment). Closed (with any running calls interrupted) on context shutdown.
     */
    private final ExecutorService executor = ContextExecutorService.wrap(
            Executors.newVirtualThreadPerTaskExecutor(), ContextSnapshotFactory.builder().build());

    /**
     * Built by {@link LlmConfiguration}, and directly by tests.
     *
     * @param client         the selected provider's adapter, or empty when {@code ai.llm.provider} is unset
     * @param breaker        the circuit breaker (on the real clock in production)
     * @param defaultTimeout the deadline for a request whose timeout is null
     * @param jsonMapper     parses the reply text
     */
    LlmProxy(Optional<LlmClient> client, LlmCircuitBreaker breaker, Duration defaultTimeout, JsonMapper jsonMapper) {
        this.client = client.orElse(null);
        this.breaker = breaker;
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
        String feature = request.feature();
        long start = System.nanoTime();

        if (client == null || !client.isConfigured()) {
            LOGGER.debug("LLM call feature={} outcome=NOT_CONFIGURED", feature);
            return unavailable(UnavailableReason.NOT_CONFIGURED);
        }
        if (!breaker.allowsCall()) {
            LOGGER.debug("LLM call feature={} outcome=DOWN cause=breaker-open", feature);
            return unavailable(UnavailableReason.DOWN);
        }

        // From here on the breaker said yes, so every path below reports back to it exactly once.
        Duration timeout = request.timeout() != null ? request.timeout() : defaultTimeout;
        Future<LlmReply> call;
        try {
            call = executor.submit(() -> client.send(request));
        } catch (RejectedExecutionException e) { // logged as class only, see the class comment
            // Only after close(): the executor no longer takes work.
            breaker.recordFailure();
            logDown(feature, start, e);
            return unavailable(UnavailableReason.DOWN);
        }
        LlmReply reply;
        try {
            reply = call.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) { // logged without the exception object, see the class comment
            call.cancel(true); // interrupts the HTTP call; a reply arriving later is never read
            // Only a wait as long as the default says the service is too slow; see the class comment.
            boolean countsAsFailure = timeout.compareTo(defaultTimeout) >= 0;
            if (countsAsFailure) {
                breaker.recordFailure();
            } else {
                breaker.release();
            }
            long latency = elapsedMillis(start);
            long timeoutMs = timeout.toMillis();
            LOGGER.warn("LLM call feature={} outcome=TIMEOUT latencyMs={} timeoutMs={} breakerFailure={}",
                    feature, latency, timeoutMs, countsAsFailure);
            return unavailable(UnavailableReason.TIMEOUT);
        } catch (ExecutionException e) { // logged as class and status only, see the class comment
            breaker.recordFailure();
            logDown(feature, start, e.getCause());
            return unavailable(UnavailableReason.DOWN);
        } catch (InterruptedException e) { // logged as class only, see the class comment
            // The request thread itself was interrupted (the server is shutting down). Stop the call,
            // keep the interrupt flag for whoever interrupted us, and still report to the breaker
            // (counted as a failure: the known limit in the class comment).
            call.cancel(true);
            Thread.currentThread().interrupt();
            breaker.recordFailure();
            logDown(feature, start, e);
            return unavailable(UnavailableReason.DOWN);
        }

        // The service answered, even if the answer turns out unusable below.
        breaker.recordSuccess();
        long latency = elapsedMillis(start);
        if (reply == null) { // an adapter bug, not an outage; it must not escape as a NullPointerException
            LOGGER.warn("LLM call feature={} outcome=BAD_OUTPUT latencyMs={} cause=no-reply", feature, latency);
            return unavailable(UnavailableReason.BAD_OUTPUT);
        }
        int inputTokens = reply.inputTokens();
        int outputTokens = reply.outputTokens();
        boolean refused = reply.refused();

        JsonNode json = refused ? null : parseObject(reply.text());
        if (json == null) {
            LOGGER.warn("LLM call feature={} outcome=BAD_OUTPUT latencyMs={} inputTokens={} outputTokens={} refused={}",
                    feature, latency, inputTokens, outputTokens, refused);
            return unavailable(UnavailableReason.BAD_OUTPUT);
        }
        LOGGER.info("LLM call feature={} outcome=ANSWERED latencyMs={} inputTokens={} outputTokens={}",
                feature, latency, inputTokens, outputTokens);
        return new LlmResult.Answered(json);
    }

    /** Stops the executor on context shutdown, interrupting any call still waiting on the network. */
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
        } catch (JacksonException e) { // the message would quote the reply text (CO-ferpa); BAD_OUTPUT is logged by the caller
            return null;
        }
    }

    /**
     * Logs a {@code DOWN} that reached the network, at {@code error} when someone must act (400, 401,
     * 403, 404: the same request fails every time) and at {@code warn} when it may pass on its own.
     * Logs the cause's class name and HTTP status only, never its message (see the class comment).
     */
    private static void logDown(String feature, long start, Throwable cause) {
        Integer status = cause instanceof RestClientResponseException http ? http.getStatusCode().value() : null;
        boolean needsAction = status != null && (status == 400 || status == 401 || status == 403 || status == 404);
        LOGGER.atLevel(needsAction ? Level.ERROR : Level.WARN)
                .log("LLM call feature={} outcome=DOWN latencyMs={} exception={} status={}",
                        feature, elapsedMillis(start), cause == null ? "unknown" : cause.getClass().getName(),
                        status == null ? "-" : status);
    }

    private static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private static LlmResult unavailable(UnavailableReason reason) {
        return new LlmResult.Unavailable(reason);
    }

}
