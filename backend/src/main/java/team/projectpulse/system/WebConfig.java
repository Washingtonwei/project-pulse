package team.projectpulse.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward non-API routes (not starting with /api) to index.html, e.g. /home, /login
        registry.addViewController("/{spring:(?!api)[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");

        // Forward non-API routes (not starting with /api) to index.html, e.g. /evaluation/my-evaluations
        registry.addViewController("/{spring1:(?!api)[a-zA-Z0-9-]+}/{spring2:[a-zA-Z0-9-]+}")
                .setViewName("forward:/index.html");
    }

}
