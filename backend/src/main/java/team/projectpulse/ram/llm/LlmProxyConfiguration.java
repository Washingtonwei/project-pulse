package team.projectpulse.ram.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * Wires the AI proxy. {@link LlmProxy} always exists, so a caller can always inject it; whether it can
 * reach a provider depends on whether an LLM client ({@link LlmClient}) exists, which is the
 * provider configuration's business, not this class's.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LlmProperties.class)
class LlmProxyConfiguration {

    /**
     * The proxy, with an empty {@code client} when no provider is selected: then every call answers
     * {@link UnavailableReason#NOT_CONFIGURED}, which is what dev and the test suite get.
     *
     * <p>Spring calls {@link LlmProxy#close()} on shutdown, because the bean is {@link AutoCloseable}.
     */
    @Bean
    LlmProxy llmProxy(Optional<LlmClient> client, LlmProperties properties, JsonMapper jsonMapper) {
        return new LlmProxy(client, properties.defaultTimeout(), jsonMapper);
    }

}
