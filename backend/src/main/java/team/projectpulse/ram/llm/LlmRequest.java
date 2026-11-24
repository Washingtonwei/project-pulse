package team.projectpulse.ram.llm;

import tools.jackson.databind.JsonNode;

import java.time.Duration;
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
 * <p>Example 1, the artifact key suggestion (UC-ART-create-artifact), the first caller. Short and
 * cheap: a small reply, a short wait, and no teaching context (SI-llm-context):
 * <pre>
 * new LlmRequest("artifact-key-suggestion",                      // caller
 *         "Suggest a short kebab-case key for this requirement.",   // instructions
 *         "Title: Reset password\nContent: The system shall ...",    // input
 *         schema,          // {"type":"object","properties":{"slug":{"type":"string"}},
 *                          //  "required":["slug"],"additionalProperties":false}
 *         50,              // reply at most 50 tokens
 *         Duration.ofSeconds(3));   // a convenience: give up after 3 s
 * </pre>
 *
 * <p>Example 2, an assistant (UC-AI-critique; illustrative, since its own design will set the real
 * values). Longer: the assistant assembles its context into {@code instructions} and {@code input},
 * asks for a bigger reply, and takes the default wait:
 * <pre>
 * new LlmRequest("critique",
 *         systemPrompt + teachingContext + assistantInstructions,   // assembled by the assistant
 *         documentSectionContent,                                   // the authoring destination
 *         schema,          // {"type":"object","properties":{"findings":{"type":"array", ...}},
 *                          //  "required":["findings"],"additionalProperties":false}
 *         2000,
 *         null);           // the default, 15 s
 * </pre>
 *
 * <p>What the two produce in the log (one line per call, see {@link LlmProxy}):
 * <pre>
 * LLM call caller=artifact-key-suggestion outcome=ANSWERED latencyMs=812 inputTokens=42 outputTokens=9
 * LLM call caller=critique outcome=TIMEOUT latencyMs=15003 timeoutMs=15000
 * </pre>
 *
 * @param caller          a short constant naming who is calling the proxy, one per AI caller. The planned
 *                        values follow the use-case slugs: {@code artifact-key-suggestion}
 *                        (UC-ART-create-artifact), {@code critique}, {@code whole-project-review},
 *                        {@code tutor}, {@code elicit-requirements}, {@code practice-interview},
 *                        {@code structure-notes}, {@code draft-skeleton}, and
 *                        {@code consult-project-assistant} (the {@code UC-AI-*} use cases). It goes into
 *                        every log line and names the response schema at the provider, so it must match
 *                        {@code [a-z0-9-]+} and be at most 64 characters. Not called "feature", because
 *                        the glossary's Feature is a coarser thing: a {@code FEAT-*} capability theme
 * @param instructions    what the model is told to do (the provider's system-prompt slot); not blank
 * @param input           what the model reads; not blank. OpenAI answers an empty input with a 400,
 *                        which would log as a {@code DOWN} at error, as if the deploy were broken, when
 *                        the fault is the caller's. Since a blank input throws rather than answering
 *                        {@code Unavailable}, a caller whose content can be empty (an empty document
 *                        section) checks first and skips the call, showing no AI result
 * @param responseSchema  a strict JSON schema the reply must follow: an object with
 *                        {@code additionalProperties: false} and every property required, because
 *                        every provider's strict structured-output mode demands that shape
 * @param maxOutputTokens the caller's cap on the reply's length; a reply cut short at this cap is
 *                        {@link UnavailableReason#BAD_OUTPUT}
 * @param timeout         how long the caller will wait, at least 1 ms and at most 15 s; null means the
 *                        configured default ({@code ai.llm.default-timeout}, 15 s)
 */
public record LlmRequest(
        String caller,
        String instructions,
        String input,
        JsonNode responseSchema,
        int maxOutputTokens,
        Duration timeout) {

    /** The longest any caller may wait (PER-ai-response-time). */
    public static final Duration MAX_TIMEOUT = Duration.ofSeconds(15);

    /** The shortest wait accepted; anything less would time out before the call could start. */
    public static final Duration MIN_TIMEOUT = Duration.ofMillis(1);

    private static final Pattern CALLER = Pattern.compile("[a-z0-9-]{1,64}");

    /**
     * Rejects a request no caller should be able to build. These are programming errors, not
     * outages, so they throw {@link IllegalArgumentException} rather than returning
     * {@link LlmResult.Unavailable}: a test of the caller catches them, a student never does.
     */
    public LlmRequest {
        if (caller == null || !CALLER.matcher(caller).matches()) {
            throw new IllegalArgumentException("caller must match [a-z0-9-]{1,64}");
        }
        if (instructions == null || instructions.isBlank()) {
            throw new IllegalArgumentException("instructions must not be blank");
        }
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input must not be blank");
        }
        if (responseSchema == null || !responseSchema.isObject()) {
            throw new IllegalArgumentException("responseSchema must be a JSON object");
        }
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("maxOutputTokens must be positive");
        }
        if (timeout != null && (timeout.compareTo(MIN_TIMEOUT) < 0 || timeout.compareTo(MAX_TIMEOUT) > 0)) {
            throw new IllegalArgumentException("timeout must be at least " + MIN_TIMEOUT + " and at most " + MAX_TIMEOUT);
        }
    }

}
