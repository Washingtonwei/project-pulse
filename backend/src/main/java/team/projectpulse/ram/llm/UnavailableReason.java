package team.projectpulse.ram.llm;

/**
 * Why a call to the LLM service produced no answer. Every failure of the external service maps to
 * one of these four, so nothing about the provider (its HTTP status, its error body) reaches a
 * caller, and through it the browser.
 */
public enum UnavailableReason {

    /**
     * No provider is selected ({@code ai.llm.provider} unset), or the selected one has no API key or
     * model. This is the normal state in dev and in the test suite. The proxy makes no call.
     */
    NOT_CONFIGURED,

    /**
     * The service could not be reached or refused the request: the connection failed, or the
     * provider answered any 4xx or 5xx (including 401 for a bad key and 429 for a rate limit).
     */
    DOWN,

    /** No reply arrived before the request's deadline. The call is cancelled and a late reply is discarded. */
    TIMEOUT,

    /**
     * The service answered, but not with a usable JSON object: the text did not parse as an object,
     * the model refused, or the reply was cut short at the output token cap.
     */
    BAD_OUTPUT
}
