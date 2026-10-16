package team.projectpulse.system;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeekKeysTest {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");

    @Test
    void testPreviousWeekOfTheDevClock() {
        // The dev profile's clock is Sunday 2023-08-20, in 2023-W33
        Clock clock = Clock.fixed(LocalDate.of(2023, 8, 20).atTime(23, 30).atZone(ZONE).toInstant(), ZONE);

        assertThat(WeekKeys.previousWeek(clock)).isEqualTo("2023-W32");
    }

    @Test
    void testPreviousWeekAcrossAYearBoundaryUsesTheWeekBasedYear() {
        // Monday 2021-01-04 is in 2021-W01; the week before is 2020-W53, whose Sunday is in 2021
        Clock clock = Clock.fixed(LocalDate.of(2021, 1, 4).atStartOfDay(ZONE).toInstant(), ZONE);

        assertThat(WeekKeys.previousWeek(clock)).isEqualTo("2020-W53");
    }

    @Test
    void testDateRange() {
        assertThat(WeekKeys.dateRange("2026-W40")).isEqualTo("09-28-2026 to 10-04-2026");
        assertThat(WeekKeys.dateRange("2020-W53")).isEqualTo("12-28-2020 to 01-03-2021");
    }

    @Test
    void testRequireValidRefusesMalformedAndNonexistentWeeks() {
        assertThat(WeekKeys.requireValid("2023-W32")).isEqualTo("2023-W32");
        assertThatThrownBy(() -> WeekKeys.requireValid("2023-32")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WeekKeys.requireValid("2023-W00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WeekKeys.requireValid("2021-W53")).isInstanceOf(IllegalArgumentException.class); // 2021 has 52 weeks
        assertThatThrownBy(() -> WeekKeys.requireValid(null)).isInstanceOf(IllegalArgumentException.class);
    }

}
