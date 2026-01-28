package team.projectpulse.ram.requirement.dto;

import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;

// This DTO is used inside artifact links to avoid circular references
public record RequirementArtifactSummaryDto(Long id,
                                            String artifactKey,
                                            String title,
                                            String content,
                                            RequirementArtifactType type,
                                            Priority priority) {
}
