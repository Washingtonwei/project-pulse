package team.projectpulse.ram.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * The OpenAI LLM client: maps a provider-neutral {@link LlmRequest} onto OpenAI's Responses API and the
 * reply back into an {@link ProviderReply}. It catches nothing; any HTTP or transport exception goes to
 * {@link LlmProxy}, which turns it into {@link UnavailableReason#DOWN}.
 *
 * <p>Exists only when {@code ai.llm.provider=openai} (see {@link OpenAiConfiguration}).
 */
final class OpenAiLlmClient implements LlmClient {

    /** OpenAI rejects a {@code max_output_tokens} below 16 with a 400, so a smaller cap is raised to 16. */
    static final int MIN_OUTPUT_TOKENS = 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final OpenAiService api;

    /**
     * The {@code ai.llm.openai.*} block of the active profile. This class reads two of its fields: the
     * model goes into every request body, and the key and the model together decide
     * {@link #isConfigured()}. The base URL and the key were already put on the HTTP client by
     * {@link OpenAiConfiguration}. Examples:
     * <pre>
     * prod, after deploy    OpenAi[apiKey=(set),  baseUrl=https://api.openai.com/v1, model=gpt-x]  configured
     * staging, no model yet OpenAi[apiKey=(set),  baseUrl=https://api.openai.com/v1, model=null]   not configured
     * Key Vault secret gone OpenAi[apiKey=(none), baseUrl=https://api.openai.com/v1, model=gpt-x]  not configured
     * </pre>
     * (This is how {@code toString()} prints them, so the key itself never shows.) In dev there is no
     * {@code ai.llm.provider}, so this class is never built and there are no settings to read.
     */
    private final LlmProperties.OpenAi settings;

    /**
     * Warns once, here at startup, when OpenAI is the selected provider but the key or the model is
     * blank. Per call, {@code NOT_CONFIGURED} logs only at debug (it is the normal state in dev), so
     * without this line a lost Key Vault secret in prod would be silent. Example, staging before a
     * model is chosen:
     * <pre>
     * WARN  OpenAiLlmClient -- AI provider openai is selected but not configured
     *     (OpenAi[apiKey=(set), baseUrl=https://api.openai.com/v1, model=null]); every AI call answers NOT_CONFIGURED
     * </pre>
     * The settings print through their {@code toString()}, which never shows the key (SI-llm-credentials).
     */
    OpenAiLlmClient(OpenAiService api, LlmProperties.OpenAi settings) {
        this.api = api;
        this.settings = settings;
        if (!isConfigured()) {
            LOGGER.warn("AI provider openai is selected but not configured ({}); every AI call answers NOT_CONFIGURED",
                    settings);
        }
    }

    /** Both the key and the model must be set; with either blank, the proxy never calls {@link #send}. */
    @Override
    public boolean isConfigured() {
        return hasText(settings.apiKey()) && hasText(settings.model());
    }

    @Override
    public ProviderReply send(LlmRequest request) {
        return toReply(api.createResponse(toOpenAiRequest(request, settings.model())));
    }

    /**
     * Example: the key suggestion's request, with model {@code gpt-x},
     * <pre>
     * new LlmRequest("artifact-key-suggestion", "Suggest a key.", "Title: Reset password",
     *         schema, 50, Duration.ofSeconds(3))
     *
     * where schema is the JSON the reply must follow, one required string field:
     *   {"type":"object","properties":{"slug":{"type":"string"}},
     *    "required":["slug"],"additionalProperties":false}
     * </pre>
     * becomes this body:
     * <pre>
     * {"model":"gpt-x","instructions":"Suggest a key.","input":"Title: Reset password",
     *  "max_output_tokens":50,"store":false,
     *  "text":{"format":{"type":"json_schema","name":"artifact-key-suggestion",
     *                    "schema":{"type":"object","properties":{"slug":{"type":"string"}},
     *                              "required":["slug"],"additionalProperties":false},
     *                    "strict":true}}}
     * </pre>
     * The schema is copied in unchanged, named after the caller, and {@code strict} makes the
     * model's reply match it exactly, here {@code {"slug":"reset-password"}}. A cap below 16 is raised
     * to 16 ({@link #MIN_OUTPUT_TOKENS}). The request's timeout is not sent; {@link LlmProxy} enforces it.
     */
    static OpenAiRequest toOpenAiRequest(LlmRequest request, String model) {
        return new OpenAiRequest(
                model,
                request.instructions(),
                request.input(),
                Math.max(request.maxOutputTokens(), MIN_OUTPUT_TOKENS),
                false,
                new OpenAiRequest.Text(new OpenAiRequest.Format(
                        "json_schema", request.caller(), request.responseSchema(), true)));
    }

    /**
     * Reads the few fields the proxy needs out of OpenAI's response. A typical answered response:
     * <pre>
     * {"status":"completed",
     *  "output":[{"type":"reasoning", ...},                                   (skipped: not a message)
     *            {"type":"message","content":[{"type":"output_text","text":"{\"slug\":\"reset-password\"}"}]}],
     *  "usage":{"input_tokens":42,"output_tokens":9}}
     * </pre>
     * gives {@code ProviderReply("{\"slug\":\"reset-password\"}", refused=false, 42, 9)}.
     *
     * <p>{@code refused} is true when the status is anything but {@code completed} (for example
     * {@code incomplete}, with {@code incomplete_details.reason} {@code max_output_tokens}: the reply
     * was cut short), or when a content item is a {@code refusal} instead of text. The proxy turns
     * either into {@link UnavailableReason#BAD_OUTPUT}. A missing {@code usage} gives 0 tokens.
     */
    static ProviderReply toReply(JsonNode response) {
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
        return new ProviderReply(text.toString(), refusal || !completed,
                usage.path("input_tokens").asInt(0), usage.path("output_tokens").asInt(0));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
