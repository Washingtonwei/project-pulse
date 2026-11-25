package team.projectpulse.ram.requirement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactKeyPrefixTest {

    @Test
    @DisplayName("An external system's artifact key starts with SYS")
    void externalSystemPrefix() {
        assertThat(ArtifactKeyPrefix.of(RequirementArtifactType.EXTERNAL_SYSTEM)).isEqualTo("SYS");
    }

    @Test
    @DisplayName("Condition keys are PRE and POST; a slug starts with a letter, so a key never reads as a display number such as PRE-1")
    void conditionPrefixes() {
        assertThat(ArtifactKeyPrefix.of(RequirementArtifactType.PRECONDITION)).isEqualTo("PRE");
        assertThat(ArtifactKeyPrefix.of(RequirementArtifactType.POSTCONDITION)).isEqualTo("POST");
    }

    @Test
    @DisplayName("An operating environment's artifact key starts with OE")
    void operatingEnvironmentPrefix() {
        assertThat(ArtifactKeyPrefix.of(RequirementArtifactType.OPERATING_ENVIRONMENT)).isEqualTo("OE");
    }

    @ParameterizedTest
    @EnumSource(RequirementArtifactType.class)
    @DisplayName("Every artifact type has a prefix of its own, so no type falls back to UNK")
    void everyTypeHasAPrefix(RequirementArtifactType type) {
        assertThat(ArtifactKeyPrefix.of(type)).isNotEqualTo("UNK");
    }
}
