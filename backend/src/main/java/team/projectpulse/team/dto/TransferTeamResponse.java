package team.projectpulse.team.dto;

/**
 * Response DTO for transferring a team between sections.
 *
 * @param teamId         The ID of the team being transferred.
 * @param teamName       The name of the team being transferred.
 * @param oldSectionId   The ID of the section from which the team is being moved.
 * @param oldSectionName The name of the section from which the team is being moved.
 * @param newSectionId   The ID of the section to which the team is being moved.
 * @param newSectionName The name of the section to which the team is being moved.
 * @param studentsMoved  The number of students moved along with the team.
 * @param s
 */
public record TransferTeamResponse(
        Integer teamId,
        String teamName,
        Integer oldSectionId,
        String oldSectionName,
        Integer newSectionId,
        String newSectionName,
        Integer studentsMoved,
        String oldInstructorName,
        String newInstructorName) {
}
