package team.projectpulse.ram.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Builds the OpenAI LLM client, and only when {@code ai.llm.provider=openai}. Without that property
 * (dev, the test suite) Spring skips this whole class: no {@link OpenAiService}, no {@link OpenAiLlmClient},
 * and {@link LlmProxy} answers {@link UnavailableReason#NOT_CONFIGURED}.
 *
 * <h2>Who writes what</h2>
 * A call to OpenAI needs setup (the HTTP library, the timeouts, the base URL, the API key) and the call
 * itself (POST a body to {@code /responses}, read JSON back). We write four things: the body
 * ({@link OpenAiRequest}), the call's signature ({@link OpenAiService}), the URL and key (the configurer
 * below), and the translation ({@link OpenAiLlmClient}). Spring Boot writes the rest, from these lines
 * in {@code application.yml}:
 * <pre>
 * spring.http.clients.imperative.factory: jdk          the HTTP library: Java's own HttpClient
 * spring.http.serviceclient.openai.connect-timeout: 5s
 * spring.http.serviceclient.openai.read-timeout: 20s   a backstop above the 15 s longest deadline; LlmProxy enforces each call's
 * </pre>
 * {@code jdk} is pinned because Boot would otherwise pick Reactor Netty, which is on the classpath only
 * through the Azure Key Vault starter. The setting is app-wide, for every {@link RestClient} Boot builds.
 *
 * <h2>The order at startup</h2>
 * Spring first reads this class as recipes, with no objects yet: {@code @ImportHttpServices} registers a
 * group named {@code openai} that holds {@link OpenAiService}. Then it builds beans in the order their
 * dependencies pull them, so {@link OpenAiService} comes almost last:
 * <pre>
 * LlmProxy needs an LlmClient -&gt; openAiLlmClient needs an OpenAiService -&gt; the group registry:
 *   1. makes an empty RestClient.Builder for the "openai" group
 *   2. runs every group configurer on that builder, in order:
 *        Boot's PropertiesRestClientHttpServiceGroupConfigurer   the JDK client, 5 s and 15 s timeouts
 *        ours, openAiGroupConfigurer (the default order: last)   base URL, Authorization: Bearer &lt;key&gt;
 *   3. builds the RestClient, then generates OpenAiService on top of it
 * </pre>
 * So the configurer shapes the builder before {@link OpenAiService} exists, and nothing is set on
 * {@link OpenAiService} afterwards. {@code OpenAiLlmClientTest.client(settings)} does steps 1 to 3 by hand, which
 * is the quickest way to see them as code.
 *
 * <p>The URL and key come from {@link LlmProperties}, not from Boot's own {@code base-url} and
 * {@code default-header} properties, so the HTTPS check holds (CI-llm-https) and the key stays in one
 * validated place that never prints it (SI-llm-credentials).
 *
 * <p>Checked against the Spring Boot 4.0.2 and Spring Framework 7.0.3 jars on 2026-11-23: the property
 * names, {@code JDK} as an allowed factory value, and the order above.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ai.llm", name = "provider", havingValue = "openai")
@ImportHttpServices(group = OpenAiConfiguration.GROUP, types = OpenAiService.class)
class OpenAiConfiguration {

    /**
     * The HTTP service group's name; also the key under {@code spring.http.serviceclient}.
     */
    static final String GROUP = "openai";

    /**
     * Adds the base URL and the bearer key to the {@code openai} group's client, and to no other group.
     *
     * <p>By the time this configurer runs, Boot's own configurer has already shaped the group's
     * builder from {@code application.yml}: the JDK {@code HttpClient}
     * ({@code spring.http.clients.imperative.factory: jdk}), the 5 s connect timeout, and the 20 s read
     * timeout ({@code spring.http.serviceclient.openai.*}). Boot's runs first because it is ordered
     * early, and ours has the default order, last. What is left is the part Boot cannot know: our own
     * settings from {@code ai.llm.openai}, already validated in {@link LlmProperties}. So the builder
     * goes from
     * <pre>
     * after Boot:  JDK HttpClient, connect 5 s, read 20 s
     * after ours:  JDK HttpClient, connect 5 s, read 20 s,
     *              base URL https://api.openai.com/v1, Authorization: Bearer sk-... (only when the key is set)
     * </pre>
     * and only then does Spring build the {@code RestClient} and generate {@link OpenAiService} on it.
     *
     * <p>Two lambdas, one inside the other. The returned lambda is the configurer itself: the body of
     * its one method, {@code configureGroups(groups)}, which Spring calls in step 2 of the class
     * comment, after this method has returned. Inside it, {@code forEachClient} takes a second lambda,
     * {@code (group, builder) -> ...}, called once for each client builder left after the filter.
     */
    @Bean
    RestClientHttpServiceGroupConfigurer openAiGroupConfigurer(LlmProperties properties) {
        return groups -> groups.filterByName(GROUP)
                .forEachClient((group, builder) -> configure(builder, properties.openai()));
    }

    /**
     * What the configurer does to the group's client builder. Static so the request-shape test applies
     * exactly this to its own builder. With no key, no {@code Authorization} header is set; the LLM client
     * then reports not configured and is never called anyway.
     */
    static void configure(RestClient.Builder builder, LlmProperties.OpenAi settings) {
        builder.baseUrl(settings.baseUrl());
        if (settings.apiKey() != null && !settings.apiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + settings.apiKey());
        }
    }

    /**
     * The LLM client {@link LlmProxyConfiguration} hands to {@link LlmProxy}.
     */
    @Bean
    LlmClient openAiLlmClient(OpenAiService api, LlmProperties properties) {
        return new OpenAiLlmClient(api, properties.openai());
    }

}
