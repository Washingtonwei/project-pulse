package team.projectpulse.ram.document.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.document.DocumentType;

public record CreateRequirementDocumentRequest(
        @NotNull(message = "Document type must not be null.")
        DocumentType type) {
}
