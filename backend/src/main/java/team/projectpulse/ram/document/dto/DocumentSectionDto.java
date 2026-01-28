package team.projectpulse.ram.document.dto;

import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.List;

public record DocumentSectionDto(Long id,
                                 String sectionKey,
                                 SectionType type,
                                 String title,
                                 String content,
                                 List<RequirementArtifactDto> requirementArtifacts,
                                 String guidance,
                                 Instant createdAt,
                                 Instant updatedAt,
                                 PeerEvaluationUserDto createdBy,
                                 PeerEvaluationUserDto updatedBy,
                                 DocumentSectionLockDto lock) {
}
