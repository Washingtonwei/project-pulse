package team.projectpulse.ram.llm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Every {@code ai.llm.*} setting, bound and validated at startup, so a bad value stops the app from
 * starting instead of surfacing on the first AI call. Defaults are the {@code @DefaultValue}s here;
 * {@code application.yml} repeats them so they are visible in one place.
 *
 * <pre>
 * ai:
 *   llm:
 *     default-timeout: 15s   # used when a request's timeout is null; at most 15 s
 *     breaker:
 *       failures: 3          # consecutive failures that open the circuit breaker
 *       open-for: 30s        # how long it stays open before one probe call
 * </pre>
 *
 * <p>Package-private, so no class outside {@code ram/llm} can read the LLM settings (SI-llm-proxy-only).
 *
 * @param defaultTimeout the deadline for a request whose timeout is null (PER-ai-response-time)
 * @param breaker        the circuit breaker's settings
 */
@ConfigurationProperties("ai.llm")
@Validated
record LlmProperties(
        @DefaultValue("15s") @DurationMin(millis = 1) @DurationMax(seconds = 15) Duration defaultTimeout,
        @DefaultValue @Valid Breaker breaker) {

    /**
     * @param failures consecutive failures that open the breaker; at least 1
     * @param openFor  how long the breaker stays open before letting one probe through; at least 1 s
     */
    record Breaker(
            @DefaultValue("3") @Min(1) int failures,
            @DefaultValue("30s") @DurationMin(seconds = 1) Duration openFor) {
    }

}
