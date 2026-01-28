package team.projectpulse.ram.requirement.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.ArtifactLinkType;

public record CreateArtifactLinkRequest(@NotNull(message = "Source artifact ID must not be null")
                                        Long sourceArtifactId,
                                        @NotNull(message = "Target artifact ID must not be null")
                                        Long targetArtifactId,
                                        @NotNull(message = "Artifact link type must not be null")
                                        ArtifactLinkType type,
                                        String notes) {
}
