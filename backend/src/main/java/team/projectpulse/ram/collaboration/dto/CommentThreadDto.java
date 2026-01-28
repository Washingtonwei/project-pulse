package team.projectpulse.ram.collaboration.dto;

import team.projectpulse.ram.collaboration.CommentThreadStatus;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.List;

public record CommentThreadDto(Long id,
                               CommentThreadStatus status,
                               Instant createdAt,
                               PeerEvaluationUserDto createdBy,
                               List<CommentDto> comments) {
}
