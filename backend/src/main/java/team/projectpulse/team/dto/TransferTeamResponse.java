package team.projectpulse.team.dto;

import java.util.List;

public record TransferTeamResponse(
        Integer teamId,
        String teamName,
        Integer oldSectionId,
        String oldSectionName,
        Integer newSectionId,
        String newSectionName,
        Integer studentsMoved,
        List<String> warnings
) {
}
