package team.projectpulse.ram.llm;

/**
 * The port one provider adapter implements (OpenAI first; Claude or Gemini later, each one more
 * implementation). Only {@link LlmProxy} calls it.
 *
 * <p>Package-private on purpose, like the adapters and {@link LlmProperties}: the compiler then
 * stops every class outside {@code ram/llm} from reaching the LLM service except through
 * {@link LlmProxy} (SI-llm-proxy-only, SEC-llm-proxy).
 */
interface LlmClient {

    /**
     * Whether this adapter can make a call at all: false when its API key or model is blank, as it
     * is in dev and whenever the Key Vault secret is missing. The proxy then answers
     * {@link UnavailableReason#NOT_CONFIGURED} without calling {@link #send}.
     */
    boolean isConfigured();

    /**
     * Sends one request to the provider and waits for its reply.
     *
     * @return the reply; {@link LlmReply#refused()} is true when the provider declined, or cut the
     *         reply short at the output token cap
     * @throws RuntimeException on any transport or HTTP error (a connection failure, any 4xx or
     *         5xx). The proxy turns every exception into {@link UnavailableReason#DOWN}; an adapter
     *         never catches them itself
     */
    LlmReply send(LlmRequest request);
}
