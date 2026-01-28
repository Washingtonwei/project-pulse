package team.projectpulse.ram.requirement.dto;

import team.projectpulse.ram.requirement.ArtifactLinkType;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

public record ArtifactLinkViewDto(Long id,
                                  ArtifactLinkType type,
                                  String notes,
                                  RequirementArtifactSummaryDto otherArtifact,
                                  Instant createdAt,
                                  Instant updatedAt,
                                  PeerEvaluationUserDto createdBy,
                                  PeerEvaluationUserDto updatedBy) {
}
