package team.projectpulse.ram.requirement.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.ArtifactLinkType;

// source/target must not change — delete + recreate instead
public record UpdateArtifactLinkRequest(@NotNull(message = "Link ID must not be null")
                                        Long linkId,
                                        @NotNull(message = "Link type must not be null")
                                        ArtifactLinkType type,
                                        String notes) {
}
