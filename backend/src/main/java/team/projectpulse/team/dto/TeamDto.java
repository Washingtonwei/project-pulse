package team.projectpulse.team.dto;

import jakarta.validation.constraints.NotEmpty;

public record TeamDto(Integer teamId,
                      @NotEmpty(message = "team rubricName is required.")
                      String teamName,
                      @NotEmpty(message = "team description is required.")
                      String description,
                      @NotEmpty(message = "team website URL is required.")
                      String teamWebsiteUrl,
                      Integer sectionId,
                      String sectionName) {
}
