package team.projectpulse.ram.llm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The OpenAI LLM client against a fake OpenAI ({@link MockRestServiceServer}), with no network. Covers the
 * design's test rows "CI-llm-https, OpenAI request shape" and "SEC-llm-data-minimization, request"
 * (docs/design/ai.md), plus the reply mapping.
 *
 * <p>The {@link OpenAiService} here is built by hand the way the HTTP service group builds it in the app:
 * a {@link RestClient} given exactly what {@link OpenAiConfiguration#configure} gives the group's
 * client, turned into an {@link OpenAiService} by {@link HttpServiceProxyFactory}.
 */
@ExtendWith(OutputCaptureExtension.class)
class OpenAiLlmClientTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final LlmProperties.OpenAi SETTINGS =
            new LlmProperties.OpenAi("sk-test-key", "https://llm.example.test/v1", "test-model");

    static final String ANSWERED = """
            {"id":"resp_1","status":"completed",
             "output":[{"type":"reasoning","summary":[]},
                       {"type":"message","role":"assistant","status":"completed",
                        "content":[{"type":"output_text","text":"{\\"slug\\":\\"reset-password\\"}","annotations":[]}]}],
             "usage":{"input_tokens":42,"output_tokens":9,"total_tokens":51}}
            """;

    final RestClient.Builder builder = RestClient.builder();
    final MockRestServiceServer openAi = MockRestServiceServer.bindTo(builder).build();

    OpenAiLlmClient client(LlmProperties.OpenAi settings) {
        OpenAiConfiguration.configure(builder, settings);
        OpenAiService api = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.build()))
                .build().createClient(OpenAiService.class);
        return new OpenAiLlmClient(api, settings);
    }

    static LlmRequest request(int maxOutputTokens) {
        return new LlmRequest("artifact-key-suggestion", "Suggest a key.", "Title: Reset password",
                LlmRequestTest.SCHEMA, maxOutputTokens, Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("The request goes over HTTPS to the configured base URL with the bearer key, model, system slot, strict schema, and cap")
    void requestShape() {
        openAi.expect(requestTo("https://llm.example.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer sk-test-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.instructions").value("Suggest a key."))
                .andExpect(jsonPath("$.input").value("Title: Reset password"))
                .andExpect(jsonPath("$.max_output_tokens").value(50))
                .andExpect(jsonPath("$.text.format.type").value("json_schema"))
                .andExpect(jsonPath("$.text.format.name").value("artifact-key-suggestion"))
                .andExpect(jsonPath("$.text.format.strict").value(true))
                .andExpect(jsonPath("$.text.format.schema.additionalProperties").value(false))
                .andExpect(jsonPath("$.text.format.schema.required[0]").value("slug"))
                .andRespond(withSuccess(ANSWERED, MediaType.APPLICATION_JSON));

        ProviderReply reply = client(SETTINGS).send(request(50));

        openAi.verify();
        assertThat(reply).isEqualTo(new ProviderReply("{\"slug\":\"reset-password\"}", false, 42, 9));
    }

    @Test
    @DisplayName("The body's top-level keys are exactly the expected set: store is false, and no identity or tracking field is sent")
    void dataMinimization() {
        Set<String> keys = new HashSet<>();
        openAi.expect(requestTo("https://llm.example.test/v1/responses"))
                .andExpect(req -> {
                    JsonNode body = JSON.readTree(req.getBody().toString());
                    keys.addAll(body.propertyNames());
                    assertThat(body.path("store").isBoolean()).isTrue();
                    assertThat(body.path("store").asBoolean()).isFalse();
                })
                .andRespond(withSuccess(ANSWERED, MediaType.APPLICATION_JSON));

        client(SETTINGS).send(request(50));

        assertThat(keys).containsExactlyInAnyOrder(
                "model", "instructions", "input", "max_output_tokens", "store", "text");
        assertThat(keys).doesNotContain("user", "safety_identifier", "metadata", "prompt_cache_key");
    }

    @Test
    @DisplayName("A cap below OpenAI's minimum of 16 is raised to 16 rather than sent and refused with a 400")
    void minimumCap() {
        openAi.expect(jsonPath("$.max_output_tokens").value(16))
                .andRespond(withSuccess(ANSWERED, MediaType.APPLICATION_JSON));

        client(SETTINGS).send(request(5));

        openAi.verify();
    }

    @Test
    @DisplayName("A provider error is thrown, not caught: the proxy is what turns it into DOWN")
    void errorIsThrown() {
        openAi.expect(requestTo("https://llm.example.test/v1/responses"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\":{\"message\":\"Incorrect API key\"}}")
                        .contentType(MediaType.APPLICATION_JSON));

        OpenAiLlmClient client = client(SETTINGS);

        assertThatThrownBy(() -> client.send(request(50))).isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("A response cut short at the cap, or a refusal, is marked refused")
    void refusedReplies() {
        JsonNode incomplete = JSON.readTree("""
                {"status":"incomplete","incomplete_details":{"reason":"max_output_tokens"},
                 "output":[{"type":"message","content":[{"type":"output_text","text":"{\\"slug\\":\\"res"}]}],
                 "usage":{"input_tokens":42,"output_tokens":16}}
                """);
        JsonNode refusal = JSON.readTree("""
                {"status":"completed",
                 "output":[{"type":"message","content":[{"type":"refusal","refusal":"I can't help with that."}]}]}
                """);

        assertThat(OpenAiLlmClient.toReply(incomplete)).isEqualTo(new ProviderReply("{\"slug\":\"res", true, 42, 16));
        assertThat(OpenAiLlmClient.toReply(refusal)).isEqualTo(new ProviderReply("", true, 0, 0));
    }

    @Test
    @DisplayName("The LLM client is configured only when both the key and the model are set")
    void isConfigured() {
        OpenAiService unused = req -> {
            throw new AssertionError("never called");
        };

        assertThat(new OpenAiLlmClient(unused, SETTINGS).isConfigured()).isTrue();
        assertThat(new OpenAiLlmClient(unused, new LlmProperties.OpenAi("", "https://x", "m")).isConfigured()).isFalse();
        assertThat(new OpenAiLlmClient(unused, new LlmProperties.OpenAi("k", "https://x", null)).isConfigured()).isFalse();
        assertThat(new OpenAiLlmClient(unused, new LlmProperties.OpenAi(null, "https://x", " ")).isConfigured()).isFalse();
    }

    @Test
    @DisplayName("An LLM client built without a model warns once at startup, and the warning never shows the key")
    void warnsWhenNotConfigured(CapturedOutput output) {
        OpenAiService unused = req -> {
            throw new AssertionError("never called");
        };

        new OpenAiLlmClient(unused, new LlmProperties.OpenAi("sk-test-key", "https://x", null));

        assertThat(output.getOut())
                .contains("WARN", "AI provider openai is selected but not configured", "apiKey=(set)", "model=null")
                .doesNotContain("sk-test-key");
    }

    @Test
    @DisplayName("A configured LLM client logs nothing at startup")
    void noWarningWhenConfigured(CapturedOutput output) {
        OpenAiService unused = req -> {
            throw new AssertionError("never called");
        };

        new OpenAiLlmClient(unused, SETTINGS);

        assertThat(output.getOut()).doesNotContain("not configured");
    }

    @Test
    @DisplayName("The settings' toString never shows the key")
    void keyNotInToString() {
        assertThat(SETTINGS.toString()).doesNotContain("sk-test-key").contains("apiKey=(set)");
    }

}
