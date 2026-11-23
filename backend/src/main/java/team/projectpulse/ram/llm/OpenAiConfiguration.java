package team.projectpulse.ram.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Builds the OpenAI adapter, and only when {@code ai.llm.provider=openai}. Without that property
 * (dev, the test suite) Spring skips this whole class: no {@link OpenAiApi}, no {@link OpenAiLlmClient},
 * and {@link LlmProxy} answers {@link UnavailableReason#NOT_CONFIGURED}.
 *
 * <h2>How the Boot 4 HTTP service group works here</h2>
 * <ol>
 *   <li>{@code @ImportHttpServices(group = "openai", types = OpenAiApi.class)} (Spring Framework 7)
 *       registers {@link OpenAiApi} as a bean. Spring writes its implementation at startup, backed by
 *       a {@link RestClient} that belongs to the group named {@code openai}.</li>
 *   <li>Spring Boot builds that group's {@link RestClient} from properties under
 *       {@code spring.http.serviceclient.openai.*}. {@code application.yml} sets
 *       {@code connect-timeout: 5s} and {@code read-timeout: 15s} there. The read timeout is only a
 *       backstop; each call's real deadline is enforced by {@link LlmProxy}.</li>
 *   <li>Boot picks the HTTP library for every {@link RestClient} it builds from
 *       {@code spring.http.clients.imperative.factory}. {@code application.yml} sets {@code jdk}
 *       (Java's own {@code java.net.http.HttpClient}). Without it Boot would pick Reactor Netty, which
 *       is on the classpath only because the Azure Key Vault starter brings it, since Boot's detection
 *       order is Apache HttpClient, Jetty, Reactor Netty, JDK. That property is app-wide: any other
 *       {@link RestClient} Boot builds would use JDK as well.</li>
 *   <li>The {@link RestClientHttpServiceGroupConfigurer} bean below then adds what Boot's properties
 *       do not hold, because it lives in {@link LlmProperties}: the base URL (validated as HTTPS at
 *       startup, CI-llm-https) and the {@code Authorization: Bearer} header with the API key
 *       (SI-llm-credentials: the key stays on the server and is never logged).</li>
 * </ol>
 * Checked against the Spring Boot 4.0.2 jars on 2026-11-22: the property names above, and
 * {@code JDK} as an allowed factory value.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ai.llm", name = "provider", havingValue = "openai")
@ImportHttpServices(group = OpenAiConfiguration.GROUP, types = OpenAiApi.class)
class OpenAiConfiguration {

    /** The HTTP service group's name; also the key under {@code spring.http.serviceclient}. */
    static final String GROUP = "openai";

    /** Adds the base URL and the bearer key to the {@code openai} group's client, and to no other group. */
    @Bean
    RestClientHttpServiceGroupConfigurer openAiGroupConfigurer(LlmProperties properties) {
        return groups -> groups.filterByName(GROUP)
                .forEachClient((group, builder) -> configure(builder, properties.openai()));
    }

    /**
     * What the configurer does to the group's client builder. Static so the request-shape test applies
     * exactly this to its own builder. With no key, no {@code Authorization} header is set; the adapter
     * then reports not configured and is never called anyway.
     */
    static void configure(RestClient.Builder builder, LlmProperties.OpenAi settings) {
        builder.baseUrl(settings.baseUrl());
        if (settings.apiKey() != null && !settings.apiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + settings.apiKey());
        }
    }

    /** The adapter {@link LlmConfiguration} hands to {@link LlmProxy}. */
    @Bean
    LlmClient openAiLlmClient(OpenAiApi api, LlmProperties properties) {
        return new OpenAiLlmClient(api, properties.openai());
    }

}
