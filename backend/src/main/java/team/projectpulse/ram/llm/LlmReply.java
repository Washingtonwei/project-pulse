package team.projectpulse.ram.llm;

/**
 * What an adapter hands back to {@link LlmProxy}, already stripped of the provider's wire format.
 *
 * @param text         the model's reply text, expected to be the JSON object the schema asked for;
 *                     the proxy parses it. Empty, never null, when there is no text
 * @param refused      true when the provider declined to answer, or stopped at the output token cap,
 *                     so the text is not a complete answer
 * @param inputTokens  tokens the provider billed for the request, 0 when it did not say (logged only)
 * @param outputTokens tokens the provider billed for the reply, 0 when it did not say (logged only)
 */
record LlmReply(String text, boolean refused, int inputTokens, int outputTokens) {

    LlmReply {
        text = text == null ? "" : text;
    }
}
