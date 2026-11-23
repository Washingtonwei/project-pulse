package team.projectpulse.ram.llm;

import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One call to the LLM service, in provider-neutral terms: what the model is told, what it reads,
 * and the shape of the JSON it must answer with. The caller builds it; {@link LlmProxy#complete}
 * sends it.
 *
 * <p>What is deliberately missing: message roles, a model name, provider fields, and any field for
 * who is asking. A provider switch therefore changes no caller, and no student identity can leave
 * through this type (SEC-llm-data-minimization). What a caller puts in {@code input} is limited to
 * requirement content; each caller's design says exactly what.
 *
 * <p>Example, the artifact key suggestion (UC-ART-create-artifact):
 * <pre>
 * new LlmRequest("artifact-key-suggestion",
 *         "Suggest a short kebab-case key for this requirement.",   // instructions
 *         "Title: Reset password\nContent: The system shall ...",    // input
 *         schema,          // {"type":"object","properties":{"slug":{"type":"string"}},
 *                          //  "required":["slug"],"additionalProperties":false}
 *         50,              // reply at most 50 tokens
 *         Duration.ofSeconds(3));
 * </pre>
 *
 * @param feature         a short constant naming the caller, such as {@code artifact-key-suggestion}.
 *                        It goes into every log line and names the response schema at the provider,
 *                        so it must match {@code [a-z0-9-]+} and be at most 64 characters
 * @param instructions    what the model is told to do (the provider's system-prompt slot); not blank
 * @param input           what the model reads; may be empty, never null
 * @param responseSchema  a strict JSON schema the reply must follow: an object with
 *                        {@code additionalProperties: false} and every property required, because
 *                        every provider's strict structured-output mode demands that shape
 * @param maxOutputTokens the caller's cap on the reply's length; a reply cut short at this cap is
 *                        {@link UnavailableReason#BAD_OUTPUT}
 * @param timeout         how long the caller will wait, above zero and at most 15 s; null means the
 *                        configured default ({@code ai.llm.default-timeout}, 15 s)
 */
public record LlmRequest(
        String feature,
        String instructions,
        String input,
        JsonNode responseSchema,
        int maxOutputTokens,
        Duration timeout) {

    /** The longest any caller may wait (PER-ai-response-time). */
    public static final Duration MAX_TIMEOUT = Duration.ofSeconds(15);

    private static final Pattern FEATURE = Pattern.compile("[a-z0-9-]{1,64}");

    /**
     * Rejects a request no caller should be able to build. These are programming errors, not
     * outages, so they throw {@link IllegalArgumentException} rather than returning
     * {@link LlmResult.Unavailable}: a test of the caller catches them, a student never does.
     */
    public LlmRequest {
        if (feature == null || !FEATURE.matcher(feature).matches()) {
            throw new IllegalArgumentException("feature must match [a-z0-9-]{1,64}");
        }
        if (instructions == null || instructions.isBlank()) {
            throw new IllegalArgumentException("instructions must not be blank");
        }
        Objects.requireNonNull(input, "input must not be null");
        if (responseSchema == null || !responseSchema.isObject()) {
            throw new IllegalArgumentException("responseSchema must be a JSON object");
        }
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("maxOutputTokens must be positive");
        }
        if (timeout != null && (timeout.isZero() || timeout.isNegative() || timeout.compareTo(MAX_TIMEOUT) > 0)) {
            throw new IllegalArgumentException("timeout must be above 0 and at most " + MAX_TIMEOUT);
        }
    }

}
