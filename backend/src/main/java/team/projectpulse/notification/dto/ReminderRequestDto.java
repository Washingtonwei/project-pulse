package team.projectpulse.notification.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.notification.SubmittableItem;

/**
 * What to remind the course section's students of. It carries no week, so a caller cannot ask for a reminder about
 * a week whose window has closed, and no student ids, so the request body cannot choose who is emailed: the server
 * decides both.
 */
public record ReminderRequestDto(@NotNull(message = "item is required.")
                                 SubmittableItem item) {
}
