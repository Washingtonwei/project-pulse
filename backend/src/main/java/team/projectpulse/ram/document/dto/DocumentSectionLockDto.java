package team.projectpulse.ram.document.dto;

import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

public record DocumentSectionLockDto(boolean locked,
                                     Long documentSectionId,
                                     PeerEvaluationUserDto lockedBy,
                                     Instant lockedAt,
                                     Instant expiresAt,
                                     String reason) {
}
