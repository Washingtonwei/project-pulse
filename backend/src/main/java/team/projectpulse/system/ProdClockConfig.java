package team.projectpulse.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@Profile("prod")
public class ProdClockConfig {

    @Bean
    public ZoneId appZoneId(@Value("${app.timezone:UTC}") String tz) { // Default to UTC if not set
        return ZoneId.of(tz);
    }

    @Bean
    public Clock clock(ZoneId appZoneId) {
        return Clock.system(appZoneId);
    }

}
