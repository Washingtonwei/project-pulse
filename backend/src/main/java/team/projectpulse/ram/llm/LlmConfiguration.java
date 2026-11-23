package team.projectpulse.ram.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.time.InstantSource;
import java.util.Optional;

/**
 * Wires the AI proxy. {@link LlmProxy} always exists, so a caller can always inject it; whether it can
 * reach a provider depends on whether a provider adapter ({@link LlmClient}) exists, which is the
 * provider configuration's business, not this class's.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LlmProperties.class)
class LlmConfiguration {

    /**
     * The proxy, with an empty {@code client} when no provider is selected: then every call answers
     * {@link UnavailableReason#NOT_CONFIGURED}, which is what dev and the test suite get.
     *
     * <p>The breaker gets {@link InstantSource#system()} directly, never an injected bean: the dev
     * profile's fixed {@code Clock} bean is also an {@code InstantSource}, and a breaker on a frozen
     * clock would stay open forever (Architectural conventions, Time).
     *
     * <p>Spring calls {@link LlmProxy#close()} on shutdown, because the bean is {@link AutoCloseable}.
     */
    @Bean
    LlmProxy llmProxy(Optional<LlmClient> client, LlmProperties properties, JsonMapper jsonMapper) {
        LlmCircuitBreaker breaker = new LlmCircuitBreaker(
                InstantSource.system(), properties.breaker().failures(), properties.breaker().openFor());
        return new LlmProxy(client, breaker, properties.defaultTimeout(), jsonMapper);
    }

}
