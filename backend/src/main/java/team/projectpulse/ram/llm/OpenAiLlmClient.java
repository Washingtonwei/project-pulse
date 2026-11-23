package team.projectpulse.ram.llm;

import tools.jackson.databind.JsonNode;

/**
 * The OpenAI adapter: maps a provider-neutral {@link LlmRequest} onto OpenAI's Responses API and the
 * reply back into an {@link LlmReply}. It catches nothing; any HTTP or transport exception goes to
 * {@link LlmProxy}, which turns it into {@link UnavailableReason#DOWN}.
 *
 * <p>Exists only when {@code ai.llm.provider=openai} (see {@link OpenAiConfiguration}).
 */
final class OpenAiLlmClient implements LlmClient {

    /** OpenAI rejects a {@code max_output_tokens} below 16 with a 400, so a smaller cap is raised to 16. */
    static final int MIN_OUTPUT_TOKENS = 16;

    private final OpenAiApi api;
    private final LlmProperties.OpenAi settings;

    OpenAiLlmClient(OpenAiApi api, LlmProperties.OpenAi settings) {
        this.api = api;
        this.settings = settings;
    }

    /** Both the key and the model must be set; with either blank, the proxy never calls {@link #send}. */
    @Override
    public boolean isConfigured() {
        return hasText(settings.apiKey()) && hasText(settings.model());
    }

    @Override
    public LlmReply send(LlmRequest request) {
        return toReply(api.createResponse(toOpenAiRequest(request, settings.model())));
    }

    /**
     * Example: {@code LlmRequest("artifact-key-suggestion", "Suggest a key.", "Title: Reset password",
     * schema, 50, 3s)} with model {@code gpt-x} becomes
     * <pre>
     * {"model":"gpt-x","instructions":"Suggest a key.","input":"Title: Reset password",
     *  "max_output_tokens":50,"store":false,
     *  "text":{"format":{"type":"json_schema","name":"artifact-key-suggestion","schema":{...},"strict":true}}}
     * </pre>
     * The request's timeout is not sent; {@link LlmProxy} enforces it.
     */
    static OpenAiRequest toOpenAiRequest(LlmRequest request, String model) {
        return new OpenAiRequest(
                model,
                request.instructions(),
                request.input(),
                Math.max(request.maxOutputTokens(), MIN_OUTPUT_TOKENS),
                false,
                new OpenAiRequest.Text(new OpenAiRequest.Format(
                        "json_schema", request.feature(), request.responseSchema(), true)));
    }

    /**
     * Reads the few fields the proxy needs out of OpenAI's response. A typical answered response:
     * <pre>
     * {"status":"completed",
     *  "output":[{"type":"reasoning", ...},                                   (skipped: not a message)
     *            {"type":"message","content":[{"type":"output_text","text":"{\"slug\":\"reset-password\"}"}]}],
     *  "usage":{"input_tokens":42,"output_tokens":9}}
     * </pre>
     * gives {@code LlmReply("{\"slug\":\"reset-password\"}", refused=false, 42, 9)}.
     *
     * <p>{@code refused} is true when the status is anything but {@code completed} (for example
     * {@code incomplete}, with {@code incomplete_details.reason} {@code max_output_tokens}: the reply
     * was cut short), or when a content item is a {@code refusal} instead of text. The proxy turns
     * either into {@link UnavailableReason#BAD_OUTPUT}. A missing {@code usage} gives 0 tokens.
     */
    static LlmReply toReply(JsonNode response) {
        StringBuilder text = new StringBuilder();
        boolean refusal = false;
        for (JsonNode item : response.path("output")) {
            if (!"message".equals(item.path("type").asString(""))) {
                continue;
            }
            for (JsonNode content : item.path("content")) {
                String type = content.path("type").asString("");
                if ("output_text".equals(type)) {
                    text.append(content.path("text").asString(""));
                } else if ("refusal".equals(type)) {
                    refusal = true;
                }
            }
        }
        boolean completed = "completed".equals(response.path("status").asString(""));
        JsonNode usage = response.path("usage");
        return new LlmReply(text.toString(), refusal || !completed,
                usage.path("input_tokens").asInt(0), usage.path("output_tokens").asInt(0));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
