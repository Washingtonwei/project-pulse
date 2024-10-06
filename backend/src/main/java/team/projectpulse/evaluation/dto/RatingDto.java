package team.projectpulse.evaluation.dto;

import jakarta.validation.constraints.NotNull;


public record RatingDto(Integer ratingId,
                        @NotNull(message = "criterion Id is required.")
                        Integer criterionId,
                        @NotNull(message = "actualScore is required.")
                        Double actualScore) {
}
