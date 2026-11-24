package team.projectpulse.ram.llm;

/**
 * What an LLM client hands back to {@link LlmProxy}, already stripped of the provider's wire format.
 * The LLM client only reports what the provider said; the proxy decides what it means.
 *
 * <p><b>Not to be confused with {@link LlmResult}.</b> This is the provider's raw words, unjudged
 * (the text may not even be JSON), and it never leaves {@code ram/llm}. {@link LlmResult} is the
 * proxy's verdict on them, {@code Answered} or {@code Unavailable}, and it is what callers see:
 * <pre>
 * caller  ──LlmRequest──►  LlmProxy  ──LlmRequest──►  LLM client ── provider
 * caller  ◄──LlmResult───  LlmProxy  ◄─ProviderReply─  LLM client
 * </pre>
 *
 * <p>Examples, for the artifact key suggestion's request (see {@link LlmRequest}):
 * <pre>
 * // The model answered in full:
 * new ProviderReply("{\"slug\":\"reset-password\"}", false, 42, 9, null)
 *     // proxy: the text parses as a JSON object, so Answered({"slug":"reset-password"})
 *
 * // The model hit the 50-token cap mid-answer (OpenAI status "incomplete"):
 * new ProviderReply("{\"slug\":\"reset-pass", true, 42, 50, "max_output_tokens")
 *     // proxy: refused, so Unavailable(BAD_OUTPUT), even before trying to parse the text;
 *     // the log line says incompleteReason=max_output_tokens
 *
 * // The model declined (an OpenAI "refusal" content item, so there is no text):
 * new ProviderReply("", true, 42, 7, null)
 *     // proxy: refused, so Unavailable(BAD_OUTPUT); incompleteReason=- tells it apart from a cut
 *
 * // The model answered, but not with JSON:
 * new ProviderReply("Sure! A good key is reset-password.", false, 42, 11, null)
 *     // proxy: not a JSON object, so Unavailable(BAD_OUTPUT)
 * </pre>
 * An HTTP error never becomes a {@code ProviderReply}: the LLM client lets the exception through, and
 * the proxy makes it {@link UnavailableReason#DOWN}.
 *
 * @param text         the model's reply text, expected to be the JSON object the schema asked for;
 *                     the proxy parses it. Empty, never null, when there is no text
 * @param refused      true when the provider declined to answer, or stopped at the output token cap,
 *                     so the text is not a complete answer
 * @param inputTokens  tokens the provider billed for the request, 0 when it did not say (logged only)
 * @param outputTokens tokens the provider billed for the reply, 0 when it did not say (logged only)
 * @param incompleteReason the provider's short code for why the reply stopped before it was complete;
 *                     null when it completed. Logged, so the LLM client must pass a code, never the
 *                     provider's text. The values OpenAI can give (Responses API reference, 2026-11-24):
 *                     <pre>
 * max_output_tokens   the cap ran out (seen live: a reasoning model spent it on its reasoning)
 * content_filter      OpenAI's safety filter stopped the reply partway
 * max_messages        a message limit was hit (multi-turn and tool use, which we do not send)
 * steered             the reply was steered off course (not explained further by the reference)
 * failed, cancelled   the status, used when there is no cut reason (queued and in_progress
 *                     happen only in background mode, which we do not use)
 * other               neither field was there, or the value was not a short code
 *                     </pre>
 */
record ProviderReply(String text, boolean refused, int inputTokens, int outputTokens, String incompleteReason) {

    ProviderReply {
        text = text == null ? "" : text;
    }
}
