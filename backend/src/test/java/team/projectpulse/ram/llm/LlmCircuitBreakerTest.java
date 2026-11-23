package team.projectpulse.ram.llm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/** Design test row "Circuit breaker" (docs/design/ai.md), on a clock the test moves by hand. */
class LlmCircuitBreakerTest {

    /** A clock that stands still until the test advances it. */
    static final class ManualClock implements InstantSource {
        private volatile Instant now = Instant.parse("2026-11-22T10:00:00Z");

        @Override
        public Instant instant() {
            return now;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }
    }

    final ManualClock clock = new ManualClock();
    final LlmCircuitBreaker breaker = new LlmCircuitBreaker(clock, 3, Duration.ofSeconds(30));

    void fail(int times) {
        for (int i = 0; i < times; i++) {
            assertThat(breaker.allowsCall()).isTrue();
            breaker.recordFailure();
        }
    }

    @Test
    @DisplayName("Two failures keep it closed; the third opens it, and it stays open for 30 s")
    void opensAtThreshold() {
        fail(2);
        assertThat(breaker.allowsCall()).isTrue();
        breaker.recordFailure(); // the third

        assertThat(breaker.allowsCall()).isFalse();
        clock.advance(Duration.ofSeconds(29));
        assertThat(breaker.allowsCall()).isFalse();
    }

    @Test
    @DisplayName("A reply resets the count, so failures must be consecutive")
    void successResetsCount() {
        fail(2);
        assertThat(breaker.allowsCall()).isTrue();
        breaker.recordSuccess();

        assertThat(breaker.consecutiveFailures()).isZero();
        fail(2);
        assertThat(breaker.allowsCall()).isTrue(); // still closed: only 2 since the reset
    }

    @Test
    @DisplayName("After 30 s exactly one of two concurrent callers gets the probe")
    void oneProbe() throws Exception {
        fail(3);
        clock.advance(Duration.ofSeconds(30));

        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> ask = () -> {
            start.await();
            return breaker.allowsCall();
        };
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Boolean> a = pool.submit(ask);
            Future<Boolean> b = pool.submit(ask);
            start.countDown();

            assertThat(List.of(a.get(), b.get())).containsExactlyInAnyOrder(true, false);
        }
    }

    @Test
    @DisplayName("A successful probe closes it")
    void probeSucceeds() {
        fail(3);
        clock.advance(Duration.ofSeconds(30));
        assertThat(breaker.allowsCall()).isTrue(); // the probe
        breaker.recordSuccess();

        assertThat(breaker.allowsCall()).isTrue();
        assertThat(breaker.allowsCall()).isTrue();
    }

    @Test
    @DisplayName("A failed probe reopens it for another 30 s")
    void probeFails() {
        fail(3);
        clock.advance(Duration.ofSeconds(30));
        assertThat(breaker.allowsCall()).isTrue(); // the probe
        breaker.recordFailure();

        assertThat(breaker.allowsCall()).isFalse();
        clock.advance(Duration.ofSeconds(29));
        assertThat(breaker.allowsCall()).isFalse();
        clock.advance(Duration.ofSeconds(1));
        assertThat(breaker.allowsCall()).isTrue(); // the next probe
    }

    @Test
    @DisplayName("A late failure while open does not push the reopen time later")
    void lateFailureWhileOpen() {
        fail(3); // open until 10:00:30
        clock.advance(Duration.ofSeconds(20));
        breaker.recordFailure(); // a call that started before it opened

        clock.advance(Duration.ofSeconds(10)); // 10:00:30, the original reopen time
        assertThat(breaker.allowsCall()).isTrue();
    }

    @Test
    @DisplayName("During a probe, only the probe's own failure reopens it; a late failure from another thread is ignored")
    void staleFailureDuringProbe() throws Exception {
        fail(3);
        clock.advance(Duration.ofSeconds(30));
        assertThat(breaker.allowsCall()).isTrue(); // this thread holds the probe

        Thread other = Thread.ofPlatform().start(breaker::recordFailure);
        other.join();
        breaker.recordSuccess(); // the probe succeeds

        assertThat(breaker.allowsCall()).isTrue(); // closed, not reopened by the other thread
    }

    @Test
    @DisplayName("A probe that ends with no verdict releases the slot, so the next caller becomes the probe")
    void releaseByProbe() {
        fail(3);
        clock.advance(Duration.ofSeconds(30));
        assertThat(breaker.allowsCall()).isTrue(); // the probe
        breaker.release();

        assertThat(breaker.consecutiveFailures()).isEqualTo(3); // nothing counted
        assertThat(breaker.allowsCall()).isTrue(); // the next probe, without another 30 s wait
    }

    @Test
    @DisplayName("Release from a thread that is not the probe changes nothing, and release while closed changes nothing")
    void releaseByOther() throws Exception {
        breaker.release(); // closed: no effect
        assertThat(breaker.consecutiveFailures()).isZero();

        fail(3);
        clock.advance(Duration.ofSeconds(30));
        assertThat(breaker.allowsCall()).isTrue(); // this thread holds the probe

        Thread other = Thread.ofPlatform().start(breaker::release);
        other.join();

        assertThat(breaker.allowsCall()).isFalse(); // still probing: the slot was not released
    }

}
