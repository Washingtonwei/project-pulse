package team.projectpulse.ram.llm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Every {@code ai.llm.*} setting, bound and validated at startup, so a bad value stops the app from
 * starting instead of surfacing on the first AI call. Defaults are the {@code @DefaultValue}s here, and
 * only here: {@code application.yml} does not repeat them, so a changed default cannot be silently
 * overridden by a stale copy. A profile or an environment variable ({@code AI_LLM_DEFAULTTIMEOUT=10s})
 * still overrides them as usual.
 *
 * <pre>
 * ai:
 *   llm:
 *     provider: openai       # which LLM client to build; unset (dev, tests) means none: NOT_CONFIGURED
 *     default-timeout: 15s   # used when a request's timeout is null; at most 15 s
 *     openai:
 *       api-key: ${openai-api-key:}         # the Key Vault secret in staging and prod; blank = NOT_CONFIGURED
 *       base-url: https://api.openai.com/v1 # must start with https:// (CI-llm-https)
 *       model:                              # chosen at deploy; blank = NOT_CONFIGURED
 *       reasoning-effort:                   # chosen with the model: none for a reasoning model, blank (not sent) for one without
 * </pre>
 *
 * <p>The HTTP timeouts are not here: they belong to the Boot 4 HTTP service group the LLM client runs
 * in ({@code spring.http.serviceclient.openai.*}, see {@link OpenAiConfiguration}).
 *
 * <p>Package-private, so no class outside {@code ram/llm} can read the LLM settings, the API key
 * included (SI-llm-proxy-only, SI-llm-credentials).
 *
 * @param provider       the selected provider, or null for none. Binding is case-insensitive
 *                       ({@code openai} binds to {@link Provider#OPENAI}); an unknown value fails startup
 * @param defaultTimeout the deadline for a request whose timeout is null (PER-ai-response-time)
 * @param openai         the OpenAI LLM client's settings, read only when {@code provider} is {@code openai}
 */
@ConfigurationProperties("ai.llm")
@Validated
record LlmProperties(
        Provider provider,
        @DefaultValue("15s") @DurationMin(millis = 1) @DurationMax(seconds = 15) Duration defaultTimeout,
        @DefaultValue @Valid OpenAi openai) {

    /** The providers an LLM client exists for. Adding Claude or Gemini adds a constant and an LLM client. */
    enum Provider {
        OPENAI
    }

    /**
     * @param apiKey  the secret API key; blank means the LLM client is not configured. Never logged
     * @param baseUrl the API root, which must be HTTPS so a plain-HTTP URL fails startup (CI-llm-https)
     * @param model   the model id, chosen at deploy time; blank means the LLM client is not configured
     * @param reasoningEffort how much hidden reasoning a reasoning model does before it answers, sent as
     *                {@code "reasoning":{"effort":...}}; blank means the field is not sent. Chosen with the
     *                model, because the two must agree:
     *                <pre>
     * model: gpt-6-luna,   reasoning-effort: none   a reasoning model, told not to reason: answers in ~20 tokens
     * model: gpt-6-luna,   reasoning-effort: (blank) it reasons at its default, medium, which can use up a
     *                                                small cap like the key suggestion's 50: BAD_OUTPUT
     * model: gpt-4.1-mini, reasoning-effort: (blank) no reasoning to turn off; sending the field here is a 400
     *                </pre>
     *                Lowercase letters only, so a typo such as {@code None} fails startup; OpenAI checks
     *                the value itself (none, low, medium, high, ...)
     */
    record OpenAi(
            String apiKey,
            @DefaultValue("https://api.openai.com/v1") @Pattern(regexp = "https://\\S+", message = "must start with https://") String baseUrl,
            String model,
            @Pattern(regexp = "[a-z]*", message = "must be lowercase letters, such as none") String reasoningEffort) {

        /** Keeps the key out of {@code toString()}, so it cannot reach a log or an actuator dump by accident. */
        @Override
        public String toString() {
            return "OpenAi[apiKey=" + (apiKey == null || apiKey.isBlank() ? "(none)" : "(set)")
                    + ", baseUrl=" + baseUrl + ", model=" + model + ", reasoningEffort=" + reasoningEffort + "]";
        }
    }

}
