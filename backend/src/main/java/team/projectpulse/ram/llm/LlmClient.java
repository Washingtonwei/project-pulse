package team.projectpulse.ram.llm;

/**
 * What every LLM client implements. An LLM client is one class per provider that translates our
 * neutral types into that provider's API and back: {@link OpenAiLlmClient} today, Claude or Gemini
 * later, each one more implementation. Only {@link LlmProxy} calls it.
 *
 * <p>Package-private on purpose, like the LLM clients and {@link LlmProperties}: the compiler then
 * stops every class outside {@code ram/llm} from reaching the LLM service except through
 * {@link LlmProxy} (SI-llm-proxy-only, SEC-llm-proxy).
 *
 * <p>Example, the OpenAI LLM client answering the key suggestion:
 * <pre>
 * client.isConfigured()  // true: ai.llm.openai.api-key and ai.llm.openai.model are both set
 * client.send(new LlmRequest("artifact-key-suggestion", "Suggest a key.", "Title: Reset password",
 *                            schema, 50, Duration.ofSeconds(3)))
 *     // returns ProviderReply("{\"slug\":\"reset-password\"}", false, 42, 9, null)
 *     // or, if the key was rotated, throws HttpClientErrorException.Unauthorized
 * </pre>
 * A second provider is one more implementation (an {@code AnthropicLlmClient} with its own
 * {@code @HttpExchange} interface); {@link LlmProxy} and every caller stay as they are.
 */
interface LlmClient {

    /**
     * Whether this LLM client can make a call at all: false when its API key or model is blank, as it
     * is in dev and whenever the Key Vault secret is missing. The proxy then answers
     * {@link UnavailableReason#NOT_CONFIGURED} without calling {@link #send}.
     */
    boolean isConfigured();

    /**
     * Sends one request to the provider and waits for its reply.
     *
     * @return the reply; {@link ProviderReply#refused()} is true when the provider declined, or cut the
     * reply short at the output token cap
     * @throws RuntimeException on any transport or HTTP error (a connection failure, any 4xx or
     *                          5xx). The proxy turns every exception into {@link UnavailableReason#DOWN}; an LLM client
     *                          never catches them itself
     */
    ProviderReply send(LlmRequest request);
}
