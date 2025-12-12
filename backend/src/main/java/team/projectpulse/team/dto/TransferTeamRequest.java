package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to transfer a team to a different section.
 *
 * @param sectionId the ID of the target section to which the team will be transferred
 */
public record TransferTeamRequest(
        @NotNull(message = "Target section ID is required.")
        Integer sectionId
) {
}
