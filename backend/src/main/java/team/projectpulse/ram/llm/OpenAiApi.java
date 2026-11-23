package team.projectpulse.ram.llm;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.JsonNode;

/**
 * OpenAI's Responses API, as a declarative HTTP interface (Spring Framework 7). There is no
 * implementation to write: Spring generates one at startup that turns each method call into an HTTP
 * request, through the {@code openai} HTTP service group that {@link OpenAiConfiguration} registers.
 * The group supplies the base URL ({@code https://api.openai.com/v1}), the {@code Authorization}
 * header, the timeouts, and the JDK HTTP client.
 *
 * <p>So {@code createResponse(body)} becomes, in effect:
 * <pre>
 * POST https://api.openai.com/v1/responses
 * Authorization: Bearer sk-...
 * Content-Type: application/json
 *
 * {"model":"...","instructions":"...","input":"...","max_output_tokens":50,"store":false,
 *  "text":{"format":{"type":"json_schema","name":"artifact-key-suggestion","schema":{...},"strict":true}}}
 * </pre>
 * and returns the response body parsed as JSON. A 4xx or 5xx throws a
 * {@link org.springframework.web.client.RestClientResponseException}, which {@link LlmProxy} turns into
 * {@link UnavailableReason#DOWN}.
 *
 * <p>The response is read as a plain {@link JsonNode}, not a typed record, so a field OpenAI adds
 * later breaks nothing; {@link OpenAiLlmClient#toReply} picks out the few fields it needs.
 *
 * <p>Package-private: only the adapter calls it (SI-llm-proxy-only).
 */
@HttpExchange
interface OpenAiApi {

    /** Creates one model response. Checked against OpenAI's API reference on 2026-11-22. */
    @PostExchange("/responses")
    JsonNode createResponse(@RequestBody OpenAiRequest request);
}
