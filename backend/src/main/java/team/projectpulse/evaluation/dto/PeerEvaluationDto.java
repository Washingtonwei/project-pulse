package team.projectpulse.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PeerEvaluationDto(Integer evaluationId,
                                @NotEmpty(message = "week is required.")
                                String week,
                                @NotNull(message = "evaluator Id is required.")
                                Integer evaluatorId,
                                String evaluatorName,
                                @NotNull(message = "evaluatee Id is required.")
                                Integer evaluateeId,
                                String evaluateeName,
                                @Valid
                                @NotEmpty(message = "ratings are required.")
                                List<RatingDto> ratings,
                                Double totalScore,
                                String publicComment,
                                String privateComment,
                                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
                                LocalDateTime createdAt,
                                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
                                LocalDateTime updatedAt) {
}
