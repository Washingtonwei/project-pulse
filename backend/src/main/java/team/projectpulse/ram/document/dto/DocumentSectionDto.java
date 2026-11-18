package team.projectpulse.ram.document.dto;

import team.projectpulse.ram.requirement.RequirementArtifactType;
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
                                 // Read-only: a list section's items, in order. Ignored in a request, so not validated:
                                 // items are created and edited through the requirement artifact endpoints.
                                 List<RequirementArtifactDto> requirementArtifacts,
                                 // Read-only: the types this document section admits (BR-artifact-home), empty for
                                 // RICH_TEXT. Filled from the template on every response; ignored in a request.
                                 List<RequirementArtifactType> artifactTypes,
                                 String guidance,
                                 Instant createdAt,
                                 Instant updatedAt,
                                 PeerEvaluationUserDto createdBy,
                                 PeerEvaluationUserDto updatedBy,
                                 Integer version,
                                 DocumentSectionLockDto lock) {

    // Immutable copies, so the record neither shares nor exposes a caller's list. requirementArtifacts stays null
    // when absent (a RICH_TEXT section); artifactTypes is never null.
    public DocumentSectionDto {
        requirementArtifacts = requirementArtifacts != null ? List.copyOf(requirementArtifacts) : null;
        artifactTypes = artifactTypes != null ? List.copyOf(artifactTypes) : List.of();
    }
}
