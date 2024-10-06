package team.projectpulse.rubric.dto;

public record CriterionDto(
        Integer criterionId,
        String criterion,
        String description,
        Double maxScore,
        Integer courseId) {
}
