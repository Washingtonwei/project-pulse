package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotNull;

public record TransferTeamRequest(
        @NotNull(message = "Target section ID is required.")
        Integer sectionId
) {
}
