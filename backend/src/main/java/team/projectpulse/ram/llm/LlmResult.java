package team.projectpulse.ram.llm;

import tools.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * What {@link LlmProxy#complete} returns: the model's answer, or the fact that AI is unavailable
 * and why. An unavailable LLM is a normal state of the system, not an error (FR-AI-degradation),
 * so it is a value the caller must handle, never an exception it might forget to catch.
 *
 * <p><b>Not to be confused with {@link ProviderReply}</b>, the provider's raw words that an LLM client
 * hands the proxy inside {@code ram/llm}. This is the proxy's verdict on them, with outages folded
 * in: four different replies (a full answer that is not JSON, one cut short at the token cap, a
 * refusal, an empty one) all reach the caller as the same {@code Unavailable(BAD_OUTPUT)}, and a
 * provider 500, which produces no reply at all, reaches it as {@code Unavailable(DOWN)}.
 *
 * <p>Because the interface is sealed, a caller can switch over it and the compiler checks that both
 * cases are handled:
 * <pre>
 * switch (llmProxy.complete(request)) {
 *     case LlmResult.Answered a    -&gt; useSlug(a.json().path("slug").asString());
 *     case LlmResult.Unavailable u -&gt; showNoSuggestion();   // the caller decides what "unavailable" looks like
 * }
 * </pre>
 */
public sealed interface LlmResult permits LlmResult.Answered, LlmResult.Unavailable {

    /**
     * The model answered, and its reply parsed as a JSON object. The proxy checks only that it is
     * an object; each caller validates the fields it reads (a missing or wrongly typed field is the
     * caller's to treat as no answer).
     *
     * @param json the reply as a JSON object
     */
    record Answered(JsonNode json) implements LlmResult {
        public Answered {
            Objects.requireNonNull(json, "json must not be null");
        }
    }

    /**
     * AI is unavailable for this call. The reason is for the caller's choice of message and for
     * tests; the log line already says what happened in more detail.
     *
     * @param reason why the call produced no answer
     */
    record Unavailable(UnavailableReason reason) implements LlmResult {
        public Unavailable {
            Objects.requireNonNull(reason, "reason must not be null");
        }
    }

}
