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

    @ParameterizedTest
    @EnumSource(RequirementArtifactType.class)
    @DisplayName("Every artifact type has a prefix of its own, so no type falls back to UNK")
    void everyTypeHasAPrefix(RequirementArtifactType type) {
        assertThat(ArtifactKeyPrefix.of(type)).isNotEqualTo("UNK");
    }
}
