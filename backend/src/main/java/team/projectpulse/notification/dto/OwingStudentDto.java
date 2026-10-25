package team.projectpulse.notification.dto;

/**
 * A student who owes an item, or who could not be reached with a reminder for it. Names only: nothing about her
 * submissions or anyone else's.
 */
public record OwingStudentDto(Integer id,
                              String firstName,
                              String lastName) {
}
