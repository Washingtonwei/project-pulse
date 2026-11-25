package team.projectpulse.ram.requirement.dto;

import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactType;

/**
 * The requirement artifact of a team that holds an artifact key: the key-holder lookup's answer, and the {@code data}
 * of a refusal for a key in use (UC-ART-create-artifact 4b and 8c, and their siblings). The editor names the holder by
 * its key and title, offers to open it, and, for a condition row, reads {@code type} to offer "use that condition".
 * For example, after {@code GET /teams/1/requirement-artifacts/key-holder?artifactKey=FR-user-authentication}:
 * <pre>
 * {"id": 9, "artifactKey": "FR-user-authentication", "title": "User authentication", "type": "FUNCTIONAL_REQUIREMENT"}
 * </pre>
 *
 * @param id          the holder's artifact id
 * @param artifactKey the key it holds
 * @param title       its title; empty for a precondition or postcondition, whose wording is its content
 * @param type        its type
 */
public record ArtifactKeyHolderDto(Long id, String artifactKey, String title, RequirementArtifactType type) {

    public static ArtifactKeyHolderDto of(RequirementArtifact artifact) {
        return new ArtifactKeyHolderDto(artifact.getId(), artifact.getArtifactKey(), artifact.getTitle(), artifact.getType());
    }

}
