package team.projectpulse.ram.collaboration.dto;

import jakarta.validation.constraints.NotEmpty;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

public record CommentDto(Long id,
                         PeerEvaluationUserDto createdBy,
                         @NotEmpty(message = "Comment must not be empty")
                         String content,
                         Instant createdAt,
                         Instant updatedAt) {
}
