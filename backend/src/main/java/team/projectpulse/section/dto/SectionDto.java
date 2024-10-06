package team.projectpulse.section.dto;


import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionDto(Integer sectionId,
                         @NotEmpty(message = "section section name is required.")
                         String sectionName,
                         @NotEmpty(message = "start date is required.")
                         String startDate,
                         @NotEmpty(message = "end date is required.")
                         String endDate,
                         Integer rubricId,
                         String rubricName,
                         List<String> activeWeeks,
                         Integer courseId) {
}
