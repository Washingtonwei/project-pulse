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

    @Value("${app.timezone:UTC}") // Default to UTC if not defined
    private String timeZone;


    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(this.timeZone));
    }

}
