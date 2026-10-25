package team.projectpulse.system;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The one place that turns calendar time into an ISO-8601 week key such as {@code "2026-W40"}, and a week key back
 * into its days.
 *
 * <p>Weeks are stored as these keys in {@code Activity.week}, {@code PeerEvaluation.week}, and
 * {@code Section.activeWeeks}. The fields are ISO: a week starts on Monday, and the week-based year is the year the
 * week belongs to, which at a year boundary is not always the calendar year of its days (January 1 can fall in W52
 * or W53 of the year before).
 *
 * <p>Read the date from the injected {@link Clock}: a week is calendar time, so the {@code dev} profile's fixed clock
 * has to move it too.
 */
public final class WeekKeys {

    private static final Pattern WEEK_KEY = Pattern.compile("^(\\d{4})-W(\\d{2})$");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private WeekKeys() {
    }

    /**
     * The week before the one the clock is in. Both weekly activity reports and peer evaluations are about this
     * week while they are due.
     */
    public static String previousWeek(Clock clock) {
        return of(LocalDate.now(clock).minusWeeks(1));
    }

    /**
     * The key of the week that contains the given date.
     */
    public static String of(LocalDate date) {
        return String.format("%d-W%02d", date.get(WeekFields.ISO.weekBasedYear()), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    /**
     * The Monday of the week a key names.
     *
     * @throws IllegalArgumentException if the key is not a well-formed week key, or names a week its year does not
     *                                  have (W00, or W53 in a 52-week year)
     */
    public static LocalDate monday(String weekKey) {
        Matcher matcher = weekKey == null ? null : WEEK_KEY.matcher(weekKey);
        if (matcher == null || !matcher.matches()) {
            throw new IllegalArgumentException("A week must look like 2026-W40.");
        }
        int year = Integer.parseInt(matcher.group(1));
        int week = Integer.parseInt(matcher.group(2));
        // January 4 is always in week 1 of its week-based year, so stepping from it never lands in the wrong year
        LocalDate monday = LocalDate.of(year, 1, 4)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), Math.max(week, 1))
                .with(DayOfWeek.MONDAY);
        // The week field rolls an out-of-range week into the next year rather than refusing it; round-tripping
        // catches that
        if (week < 1 || !of(monday).equals(weekKey)) {
            throw new IllegalArgumentException("Week " + weekKey + " does not exist.");
        }
        return monday;
    }

    /**
     * Checks a caller-supplied week key and returns it unchanged.
     *
     * @throws IllegalArgumentException as {@link #monday(String)} does
     */
    public static String requireValid(String weekKey) {
        monday(weekKey);
        return weekKey;
    }

    /**
     * The week's Monday-to-Sunday range as shown to people, for example {@code "09-28-2026 to 10-04-2026"}.
     */
    public static String dateRange(String weekKey) {
        LocalDate monday = monday(weekKey);
        return monday.format(DATE_FORMAT) + " to " + monday.plusDays(6).format(DATE_FORMAT);
    }

}
