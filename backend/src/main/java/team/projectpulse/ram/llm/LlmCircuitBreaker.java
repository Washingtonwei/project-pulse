package team.projectpulse.ram.llm;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stops calling the LLM service for a while once it keeps failing, so that during an outage every
 * caller gets {@link UnavailableReason#DOWN} at once instead of waiting out its timeout.
 *
 * <p>Worked example, with the defaults (3 failures, open for 30 s):
 * <pre>
 * 10:00:00  call 1 times out          failures = 1        CLOSED
 * 10:00:05  call 2 gets a 503         failures = 2        CLOSED
 * 10:00:09  call 3 connection refused failures = 3        OPEN until 10:00:39
 * 10:00:20  call 4                    allowsCall() false  DOWN, no network call
 * 10:00:40  calls 5 and 6 at once     5 gets the probe    PROBING; 6 gets false, DOWN
 * 10:00:41  probe 5 gets a reply      failures = 0        CLOSED; calls flow again
 *   (had probe 5 failed instead:      OPEN until 10:01:11, and the next probe after that)
 * </pre>
 * Any reply counts as success, even one the proxy then rejects as {@link UnavailableReason#BAD_OUTPUT},
 * because the service did answer. {@link UnavailableReason#NOT_CONFIGURED} never reaches the breaker.
 *
 * <p>Three failures, not one, so a single blip does not switch AI off; the probe means recovery needs
 * no restart. There is no retry here: retries multiply load during an outage, and the student's own
 * retry is the one QS-llm-outage asks for.
 *
 * <p><b>Time is elapsed time, so it is the real clock.</b> Production passes {@link InstantSource#system()}
 * directly. It must never be injected by type: the {@code dev} profile's fixed {@code Clock} bean is an
 * {@code InstantSource} too, and a breaker on a frozen clock would never close again. Tests pass a
 * source they control.
 *
 * <p>Thread-safe: one lock guards all state. A {@link ReentrantLock} rather than {@code synchronized},
 * so a virtual thread waiting on it is never pinned to its carrier thread.
 */
final class LlmCircuitBreaker {

    private enum State {
        /** Calls flow; failures are being counted. */
        CLOSED,
        /** Calls are refused until {@code openUntil}. */
        OPEN,
        /** One probe call is in flight; every other call is refused until it reports back. */
        PROBING
    }

    private final InstantSource clock;
    private final int failureThreshold;
    private final Duration openFor;
    private final ReentrantLock lock = new ReentrantLock();

    private State state = State.CLOSED;
    private int consecutiveFailures;
    private Instant openUntil = Instant.MIN;

    /**
     * @param clock            the real clock in production ({@link InstantSource#system()})
     * @param failureThreshold consecutive failures that open the breaker ({@code ai.llm.breaker.failures}, 3)
     * @param openFor          how long it stays open before one probe ({@code ai.llm.breaker.open-for}, 30 s)
     */
    LlmCircuitBreaker(InstantSource clock, int failureThreshold, Duration openFor) {
        this.clock = clock;
        this.failureThreshold = failureThreshold;
        this.openFor = openFor;
    }

    /**
     * Asks whether a call may go to the network now. A true answer obliges the caller to report the
     * outcome with exactly one {@link #recordSuccess()} or {@link #recordFailure()}; otherwise a probe
     * would never report back and the breaker would refuse every call forever.
     */
    boolean allowsCall() {
        lock.lock();
        try {
            return switch (state) {
                case CLOSED -> true;
                case PROBING -> false;
                case OPEN -> {
                    if (clock.instant().isBefore(openUntil)) {
                        yield false;
                    }
                    state = State.PROBING; // this caller is the probe; the rest wait for its outcome
                    yield true;
                }
            };
        } finally {
            lock.unlock();
        }
    }

    /** The service answered. Resets the count and closes the breaker (ends a probe successfully). */
    void recordSuccess() {
        lock.lock();
        try {
            consecutiveFailures = 0;
            state = State.CLOSED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The call timed out or the adapter threw. A failed probe reopens the breaker for another
     * {@code openFor}; otherwise the count grows and the breaker opens at the threshold.
     */
    void recordFailure() {
        lock.lock();
        try {
            consecutiveFailures++;
            if (state == State.PROBING || consecutiveFailures >= failureThreshold) {
                state = State.OPEN;
                openUntil = clock.instant().plus(openFor);
            }
        } finally {
            lock.unlock();
        }
    }

    /** For tests: the current failure count. */
    int consecutiveFailures() {
        lock.lock();
        try {
            return consecutiveFailures;
        } finally {
            lock.unlock();
        }
    }

}
