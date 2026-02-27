package team.projectpulse.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward non-API UI routes to index.html, e.g. /home, /login.
        registry.addViewController("/{spring:(?!api|actuator|v3|swagger-ui)[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");

        // Forward nested UI routes to index.html, e.g. /evaluation/my-evaluations.
        registry.addViewController("/{spring1:(?!api|actuator|v3|swagger-ui)[a-zA-Z0-9-]+}/{spring2:[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");

        // Forward 3-segment UI routes to index.html, e.g. /ram/documents/1.
        registry.addViewController("/{spring1:(?!api|actuator|v3|swagger-ui)[a-zA-Z0-9-]+}/{spring2:[a-zA-Z0-9-]+}/{spring3:[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");
    }

}
