package team.projectpulse.ram.llm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The AI proxy's Spring wiring, in a small context that holds only {@link LlmConfiguration} and a
 * {@link JsonMapper} (the full application supplies its own). Design test row "Provider selection"
 * (docs/design/ai.md), the half that needs no provider.
 */
class LlmConfigurationTest {

    final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(LlmConfiguration.class)
            .withBean(JsonMapper.class, () -> JsonMapper.builder().build());

    @Test
    @DisplayName("With no provider, the context holds the proxy but no adapter, and every call is NOT_CONFIGURED")
    void noProvider() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LlmProxy.class).doesNotHaveBean(LlmClient.class);

            LlmResult result = context.getBean(LlmProxy.class).complete(LlmProxyTest.REQUEST);

            assertThat(result).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        });
    }

    @Test
    @DisplayName("The ai.llm defaults bind: 15 s default timeout, breaker at 3 failures for 30 s")
    void defaults() {
        runner.run(context -> {
            LlmProperties properties = context.getBean(LlmProperties.class);

            assertThat(properties.defaultTimeout()).isEqualTo(Duration.ofSeconds(15));
            assertThat(properties.breaker().failures()).isEqualTo(3);
            assertThat(properties.breaker().openFor()).isEqualTo(Duration.ofSeconds(30));
        });
    }

    @Test
    @DisplayName("A default timeout over 15 s fails startup")
    void defaultTimeoutTooLong() {
        runner.withPropertyValues("ai.llm.default-timeout=20s")
                .run(context -> assertThat(context).hasFailed());
    }

}
