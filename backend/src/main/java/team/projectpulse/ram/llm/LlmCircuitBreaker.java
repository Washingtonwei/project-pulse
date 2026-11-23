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
 * 10:00:12  call 0, started at 09:59:58, finally fails     ignored: already OPEN, 10:00:39 stands
 * 10:00:20  call 4                    allowsCall() false  DOWN, no network call
 * 10:00:40  calls 5 and 6 at once     5 gets the probe    PROBING; 6 gets false, DOWN
 * 10:00:41  probe 5 gets a reply      failures = 0        CLOSED; calls flow again
 *   (had probe 5 failed instead:      OPEN until 10:01:11, and the next probe after that)
 *   (had probe 5 ended with no verdict, see release(): OPEN as before, so call 7 becomes the probe)
 * </pre>
 * Any reply counts as success, even one the proxy then rejects as {@link UnavailableReason#BAD_OUTPUT},
 * because the service did answer. {@link UnavailableReason#NOT_CONFIGURED} never reaches the breaker.
 *
 * <p><b>Late reports.</b> A call that started before the breaker opened can report after. A late
 * failure changes nothing: while OPEN the reopen time stays put, and while PROBING only the probe's own
 * outcome decides. A late success still closes the breaker, because a reply proves the service is back.
 * The probe is recognised by its thread: {@link LlmProxy} asks and reports on the same request thread,
 * so the thread that was handed the probe is the one whose report counts.
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
    /** The thread that was handed the probe, while PROBING; null otherwise. */
    private Thread probe;

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
     * outcome, on the same thread, with exactly one of {@link #recordSuccess()}, {@link #recordFailure()},
     * or {@link #release()}; otherwise a probe would never report back and the breaker would refuse
     * every call forever.
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
                    probe = Thread.currentThread();
                    yield true;
                }
            };
        } finally {
            lock.unlock();
        }
    }

    /** The service answered. Resets the count and closes the breaker, whoever reports it. */
    void recordSuccess() {
        lock.lock();
        try {
            consecutiveFailures = 0;
            state = State.CLOSED;
            probe = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The call timed out or the adapter threw. While CLOSED the count grows and the breaker opens at
     * the threshold; a failed probe reopens it for another {@code openFor}. A late failure (while OPEN,
     * or from anyone but the probe while PROBING) is ignored; see the class comment.
     */
    void recordFailure() {
        lock.lock();
        try {
            switch (state) {
                case CLOSED -> {
                    consecutiveFailures++;
                    if (consecutiveFailures >= failureThreshold) {
                        open();
                    }
                }
                case PROBING -> {
                    if (Thread.currentThread().equals(probe)) {
                        open();
                    }
                }
                case OPEN -> {
                    // A call that started before the breaker opened; the reopen time stays as it is.
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * The call ended without saying anything about the service: the caller's own deadline, shorter than
     * the default, passed first (a 3 s key suggestion while the provider takes 4 s). Nothing is counted. If this thread held the probe, the probe
     * slot is handed back (OPEN with its reopen time already passed), so the next caller becomes the probe.
     */
    void release() {
        lock.lock();
        try {
            if (state == State.PROBING && Thread.currentThread().equals(probe)) {
                state = State.OPEN;
                probe = null;
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

    /** Call with the lock held. */
    private void open() {
        state = State.OPEN;
        openUntil = clock.instant().plus(openFor);
        probe = null;
    }

}
