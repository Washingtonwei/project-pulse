package team.projectpulse.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import team.projectpulse.activity.ActivityCategory;
import team.projectpulse.activity.ActivityStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ActivityDto(Integer activityId,
                          Integer studentId,
                          String studentName,
                          @NotEmpty(message = "week is required.")
                          String week,
                          Integer teamId,
                          String teamName,
                          @NotNull(message = "category is required.")
                          ActivityCategory category,
                          @NotEmpty(message = "activity is required.")
                          String activity,
                          @NotEmpty(message = "activity description is required.")
                          String description,
                          @NotNull(message = "planned hours is required.")
                          Double plannedHours,
                          @NotNull(message = "actual hours is required.")
                          Double actualHours,
                          @NotNull(message = "status is required.")
                          ActivityStatus status,
                          String comments,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
                          LocalDateTime createdAt,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
                          LocalDateTime updatedAt) {
}
