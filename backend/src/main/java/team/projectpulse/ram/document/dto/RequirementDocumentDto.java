package team.projectpulse.ram.document.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.document.DocumentStatus;
import team.projectpulse.ram.document.DocumentType;

import java.util.List;

public record RequirementDocumentDto(Long id,
                                     @NotNull(message = "Document type must not be null")
                                     DocumentType type,
                                     @NotNull(message = "Team Id must not be null")
                                     Integer teamId,
                                     String documentKey,
                                     String title,
                                     List<DocumentSectionDto> sections,
                                     DocumentStatus status,
                                     Integer version) {
}
