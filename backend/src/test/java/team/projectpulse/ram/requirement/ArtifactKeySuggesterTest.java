package team.projectpulse.ram.requirement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import team.projectpulse.ram.llm.LlmProxy;
import team.projectpulse.ram.llm.LlmRequest;
import team.projectpulse.ram.llm.LlmResult;
import team.projectpulse.ram.llm.UnavailableReason;
import team.projectpulse.ram.requirement.dto.ArtifactKeySuggestionDto;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The artifact key suggestion (design: art.md, "Artifact keys" and Tests): what it sends the AI proxy, how it reads
 * the answer, and what it does while AI is unavailable.
 */
class ArtifactKeySuggesterTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private LlmProxy llmProxy;
    private ArtifactKeySuggester suggester;

    @BeforeEach
    void setUp() {
        this.llmProxy = mock(LlmProxy.class);
        this.suggester = new ArtifactKeySuggester(this.llmProxy, this.jsonMapper);
    }

    @Test
    void answered_PrefixAdded_AndTheRequestCarriesOnlyTypeTitleAndContent() {
        answer("{\"slug\": \"export-csv\"}");

        ArtifactKeySuggestionDto suggestion = this.suggester.suggest(RequirementArtifactType.FUNCTIONAL_REQUIREMENT,
                "Data export", "The system shall let the client export project data as a CSV file.");

        assertThat(suggestion).isEqualTo(new ArtifactKeySuggestionDto("FR-export-csv", true));
        ArgumentCaptor<LlmRequest> sent = ArgumentCaptor.forClass(LlmRequest.class);
        verify(this.llmProxy).complete(sent.capture());
        assertThat(sent.getValue().caller()).isEqualTo("artifact-key-suggestion");
        assertThat(sent.getValue().timeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(sent.getValue().maxOutputTokens()).isEqualTo(50);
        // SEC-llm-data-minimization: the type's label, the title and the content, and nothing else.
        assertThat(sent.getValue().input()).isEqualTo("""
                Type: functional requirement
                Title: Data export
                Content: The system shall let the client export project data as a CSV file.""");
    }

    @Test
    void input_EachFieldCutAt2000Characters_BlankOnesLeftOut() {
        String input = ArtifactKeySuggester.input(RequirementArtifactType.PRECONDITION, "", "x".repeat(2500));
        assertThat(input).isEqualTo("Type: precondition\nContent: " + "x".repeat(2000));
    }

    @Test
    void answerOutOfFormat_NoSuggestion_NeverRepaired() {
        // Only an answer that is already a slug is used, exactly as written; anything else gives no key.
        for (String answer : new String[]{"Export CSV", "export_csv", "FR-export-csv", " export-csv", "export-csv.", "2fa-login", "a".repeat(41)}) {
            answer("{\"slug\": \"" + answer + "\"}");
            assertThat(this.suggester.suggest(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "Data export", ""))
                    .as(answer).isEqualTo(new ArtifactKeySuggestionDto(null, true));
        }
    }

    @Test
    void unusableAnswer_NoKey_ButAiWasAvailable() {
        answer("{\"slug\": \"!!!\"}");
        assertThat(this.suggester.suggest(RequirementArtifactType.RISK, "Data loss", "")).isEqualTo(new ArtifactKeySuggestionDto(null, true));
        answer("{\"slug\": 42}");
        assertThat(this.suggester.suggest(RequirementArtifactType.RISK, "Data loss", "")).isEqualTo(new ArtifactKeySuggestionDto(null, true));
    }

    @Test
    void unavailable_NoKey_AndSaysSo() {
        for (UnavailableReason reason : UnavailableReason.values()) {
            given(this.llmProxy.complete(any())).willReturn(new LlmResult.Unavailable(reason));
            assertThat(this.suggester.suggest(RequirementArtifactType.RISK, "Data loss", "")).as(reason.name())
                    .isEqualTo(new ArtifactKeySuggestionDto(null, false));
        }
    }

    @Test
    void nothingToGoOn_RefusedWithoutCallingTheLlm() {
        assertThatThrownBy(() -> this.suggester.suggest(RequirementArtifactType.RISK, " ", null))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessage("Enter a title or content first, so there is something to suggest a key from.");
        verifyNoInteractions(this.llmProxy);
    }

    private void answer(String json) {
        given(this.llmProxy.complete(any())).willReturn(new LlmResult.Answered(this.jsonMapper.readTree(json)));
    }

}
