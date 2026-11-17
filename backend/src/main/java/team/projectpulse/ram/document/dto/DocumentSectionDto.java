package team.projectpulse.ram.document.dto;

import jakarta.validation.Valid;
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
                                 // @Valid on the element type, so each item's own constraints are checked; @Valid on the
                                 // request body alone would stop at this list (UC-ART-create-artifact extension 4a).
                                 List<@Valid RequirementArtifactDto> requirementArtifacts,
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
