package team.projectpulse.notification;

import team.projectpulse.section.Section;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * The two weekly items a student owes (BR-submission-owed), each with the course section's due day and time for it.
 * Both are about the previous week while they are due.
 */
public enum SubmittableItem {

    WEEKLY_ACTIVITY_REPORT("Weekly activity report") {
        @Override
        public DayOfWeek dueDay(Section section) {
            return section.getWarWeeklyDueDay();
        }

        @Override
        public LocalTime dueTime(Section section) {
            return section.getWarDueTime();
        }
    },

    PEER_EVALUATION("Peer evaluation") {
        @Override
        public DayOfWeek dueDay(Section section) {
            return section.getPeerEvaluationWeeklyDueDay();
        }

        @Override
        public LocalTime dueTime(Section section) {
            return section.getPeerEvaluationDueTime();
        }
    };

    private final String label;


    SubmittableItem(String label) {
        this.label = label;
    }

    /**
     * The item's name as shown to students and instructors, in sentence case.
     */
    public String label() {
        return this.label;
    }

    /**
     * The course section's weekly due day for this item, or null if none is configured.
     */
    public abstract DayOfWeek dueDay(Section section);

    /**
     * The course section's due time for this item, or null if none is configured.
     */
    public abstract LocalTime dueTime(Section section);

}
