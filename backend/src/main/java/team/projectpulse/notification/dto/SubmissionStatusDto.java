package team.projectpulse.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import team.projectpulse.notification.SubmittableItem;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Who owes an item for a week in a course section (BR-submission-owed), grouped by team.
 *
 * @param weekActive whether the week is one of the course section's active weeks. The list is computed either way,
 *                   so the report pages can show it for any week; only the reminder dialog reads this, to show 4a
 * @param weekRange  the week as people read it, for example "09-28-2026 to 10-04-2026"
 * @param dueDay     the course section's weekly due day for the item, or null if none is configured
 * @param dueTime    the course section's due time for the item, or null if none is configured
 */
public record SubmissionStatusDto(SubmittableItem item,
                                  String week,
                                  String weekRange,
                                  boolean weekActive,
                                  DayOfWeek dueDay,
                                  @JsonFormat(pattern = "HH:mm")
                                  LocalTime dueTime,
                                  List<OwingTeamDto> teams) {
}
