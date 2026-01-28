package team.projectpulse.ram.document.dto;

import team.projectpulse.ram.document.DocumentStatus;
import team.projectpulse.ram.document.DocumentType;

public record RequirementDocumentSummaryDto(Long id,
                                            DocumentType type,
                                            Integer teamId,
                                            String documentKey,
                                            String title,
                                            DocumentStatus status,
                                            int version) {
}
