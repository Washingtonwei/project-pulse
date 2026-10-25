package team.projectpulse.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import team.projectpulse.notification.SubmittableItem;

/**
 * What to remind the course section's students of. The week is the one the instructor reviewed, and it does not
 * choose the week: the server still sends for the previous week and refuses the request if the two differ, so a
 * Confirm that lands after Monday midnight cannot remind students about a week nobody reviewed. It carries no student
 * ids, so the request body cannot choose who is emailed.
 */
public record ReminderRequestDto(@NotNull(message = "item is required.")
                                 SubmittableItem item,
                                 @NotBlank(message = "week is required.")
                                 String week) {
}
