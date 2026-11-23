package team.projectpulse.ram.llm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Design test row "Request validation" (docs/design/ai.md). */
class LlmRequestTest {

    static final JsonNode SCHEMA = JsonMapper.builder().build().readTree("""
            {"type":"object","properties":{"slug":{"type":"string"}},"required":["slug"],"additionalProperties":false}
            """);

    static LlmRequest request(String feature, String instructions, Duration timeout) {
        return new LlmRequest(feature, instructions, "input", SCHEMA, 50, timeout);
    }

    @Test
    @DisplayName("A well-formed request is accepted, and a null timeout stays null (the proxy applies the default)")
    void validRequest() {
        LlmRequest request = request("artifact-key-suggestion", "Suggest a key.", null);

        assertThat(request.timeout()).isNull();
    }

    @Test
    @DisplayName("Blank instructions are rejected")
    void blankInstructions() {
        assertThatThrownBy(() -> request("f", "  ", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A feature outside [a-z0-9-]+ is rejected, because it is logged and names the schema")
    void badFeature() {
        assertThatThrownBy(() -> request("Key Suggestion", "x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> request("", "x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> request("a".repeat(65), "x", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A timeout of zero, below zero, under 1 ms, or over 15 s is rejected; exactly 1 ms and 15 s are accepted")
    void badTimeout() {
        assertThatThrownBy(() -> request("f", "x", Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        // 0.5 ms would truncate to a 0 ms wait, time out at once, and blame the provider
        assertThatThrownBy(() -> request("f", "x", Duration.ofNanos(500_000))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> request("f", "x", Duration.ofSeconds(-1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> request("f", "x", Duration.ofMillis(15_001))).isInstanceOf(IllegalArgumentException.class);

        assertThat(request("f", "x", Duration.ofSeconds(15)).timeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(request("f", "x", Duration.ofMillis(1)).timeout()).isEqualTo(Duration.ofMillis(1));
    }

    @Test
    @DisplayName("A null input, a missing or non-object schema, and a non-positive token cap are rejected")
    void otherFields() {
        assertThatThrownBy(() -> new LlmRequest("f", "x", null, SCHEMA, 50, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LlmRequest("f", "x", "", null, 50, null)).isInstanceOf(IllegalArgumentException.class);
        JsonNode notAnObject = JsonMapper.builder().build().readTree("[1]");
        assertThatThrownBy(() -> new LlmRequest("f", "x", "", notAnObject, 50, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LlmRequest("f", "x", "", SCHEMA, 0, null)).isInstanceOf(IllegalArgumentException.class);
    }

}
