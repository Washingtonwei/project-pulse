package team.projectpulse.ram.requirement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The artifact key format and order (design: art.md, "Artifact keys"; the Project Glossary's Artifact Key).
 */
class ArtifactKeysTest {

    @Test
    @DisplayName("A key is the type's prefix and a slug of lowercase letters, digits and single hyphens, starting with a letter")
    void validKeys() {
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "FR-export-csv")).isEmpty();
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "FR-step-2")).isEmpty();
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.PRECONDITION, "PRE-student-logged-in")).isEmpty();
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.RISK, "RSK-" + "a".repeat(40))).isEmpty();
    }

    @Test
    @DisplayName("A missing or malformed key gives the format message (4a)")
    void malformedKeys() {
        for (String key : new String[]{null, "", " ", "FR-", "FR-Export", "FR-1abc", "FR-1", "FR--x", "FR-x-", "FR-x--y", "FR-x_y", "fr-x", "FRexport", "RSK-" + "a".repeat(41)}) {
            assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, key)).as(String.valueOf(key))
                    .hasValueSatisfying(message -> assertThat(message).isNotBlank());
        }
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "FR-1abc"))
                .hasValue("The artifact key must be FR- followed by a name of lowercase letters, digits and single hyphens, starting with a letter, at most 40 characters.");
    }

    @Test
    @DisplayName("Another type's prefix names the right one")
    void wrongPrefix() {
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.RISK, "FR-data-loss")).hasValue("The artifact key of a risk starts with RSK-.");
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.ASSUMPTION, "DEP-campus")).hasValue("The artifact key of an assumption starts with ASM-.");
        // The prefix is checked before the slug, so a wrong prefix is named even when the slug is also out of format.
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "RSK-Data")).hasValue("The artifact key of a functional requirement starts with FR-.");
        assertThat(ArtifactKeys.problemWith(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "UC-export-csv")).hasValue("The artifact key of a functional requirement starts with FR-.");
    }

    @Test
    @DisplayName("Keys sort by prefix, then with numbers compared as numbers; a missing key sorts last")
    void naturalOrder() {
        List<String> keys = new ArrayList<>(Arrays.asList("UC-submit-report", "FR-step-10", null, "FR-step-2", "BO-quality", "FR-step-02b"));
        keys.sort(ArtifactKeys.NATURAL_ORDER);
        assertThat(keys).containsExactly("BO-quality", "FR-step-2", "FR-step-02b", "FR-step-10", "UC-submit-report", null);
    }

}
