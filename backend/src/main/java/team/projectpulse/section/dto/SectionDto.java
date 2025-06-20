package team.projectpulse.section.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
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
                         Integer courseId,
                         @NotNull(message = "isActive is required.")
                         Boolean isActive,
                         @NotNull(message = "WAR weekly due day is required.")
                         DayOfWeek warWeeklyDueDay,
                         @NotEmpty(message = "WAR due time is required.")
                         String warDueTime,
                         @NotNull(message = "Peer evaluation weekly due day is required.")
                         DayOfWeek peerEvaluationWeeklyDueDay,
                         @NotEmpty(message = "Peer evaluation due time is required.")
                         String peerEvaluationDueTime) {
}
