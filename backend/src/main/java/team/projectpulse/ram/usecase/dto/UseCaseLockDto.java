package team.projectpulse.ram.usecase.dto;

import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

public record UseCaseLockDto(boolean locked,
                             Long useCaseId,
                             PeerEvaluationUserDto lockedBy,
                             Instant lockedAt,
                             Instant expiresAt,
                             String reason) {
}