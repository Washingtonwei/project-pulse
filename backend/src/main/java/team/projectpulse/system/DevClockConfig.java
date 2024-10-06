package team.projectpulse.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
@Profile("dev")
public class DevClockConfig {

    @Bean
    public Clock clock() {
        // Set to a specific past date and time for development
        // August 20, 2023 at 11:30 PM is a Sunday in week 33 of 2023, which is a good test case since Sunday is the last day of the week
        LocalDateTime pastDateTime = LocalDateTime.of(2023, 8, 20, 23, 30);
        ZoneId zone = ZoneId.of("America/Chicago");
        return Clock.fixed(pastDateTime.atZone(zone).toInstant(), zone);
    }

}
