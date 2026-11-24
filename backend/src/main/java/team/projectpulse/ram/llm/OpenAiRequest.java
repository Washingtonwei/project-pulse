package team.projectpulse.ram.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

/**
 * The body of one OpenAI Responses API call, field for field. Jackson writes each record component
 * as a JSON field of the same name ({@code max_output_tokens} is renamed for OpenAI's snake case).
 *
 * <p><b>What is deliberately absent</b> (SEC-llm-data-minimization): OpenAI's optional identity and
 * tracking fields {@code user}, {@code safety_identifier}, {@code metadata}, and {@code prompt_cache_key}.
 * Because this record has no such component, no code path can set one. Temperature is absent too, so
 * the model's default applies.
 *
 * @param model           the configured model id ({@code ai.llm.openai.model})
 * @param instructions    the request's instructions, sent in OpenAI's system-prompt slot
 * @param input           the request's input, as a plain string
 * @param maxOutputTokens the output cap (OpenAI counts a reasoning model's hidden reasoning tokens in it)
 * @param store           always false: OpenAI does not keep the exchange for later retrieval
 * @param text            the structured-output setting: reply with JSON that follows the schema
 */
record OpenAiRequest(
        String model,
        String instructions,
        String input,
        @JsonProperty("max_output_tokens") int maxOutputTokens,
        boolean store,
        Text text) {

    /** {@code "text": {"format": {...}}} */
    record Text(Format format) {
    }

    /**
     * {@code {"type":"json_schema","name":"artifact-key-suggestion","schema":{...},"strict":true}}.
     * Strict mode makes the model's reply follow the schema exactly, which is why each caller's
     * schema must be an object with {@code additionalProperties: false} and every property required.
     */
    record Format(String type, String name, JsonNode schema, boolean strict) {
    }

}
