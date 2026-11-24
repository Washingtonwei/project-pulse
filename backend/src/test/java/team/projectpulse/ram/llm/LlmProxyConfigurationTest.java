package team.projectpulse.ram.llm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.JdkClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.imperative.ImperativeHttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.service.HttpServiceClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The AI proxy's Spring wiring, in a small context: {@link LlmProxyConfiguration}, {@link OpenAiConfiguration},
 * a {@link JsonMapper}, and Spring Boot's own HTTP-client auto-configurations, the same ones the
 * application loads. Design test rows "Provider selection" and "CI-llm-https, plain HTTP refused"
 * (docs/design/ai.md).
 */
class LlmProxyConfigurationTest {

    final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HttpClientAutoConfiguration.class,
                    ImperativeHttpClientAutoConfiguration.class,
                    HttpServiceClientPropertiesAutoConfiguration.class,
                    RestClientAutoConfiguration.class,
                    HttpServiceClientAutoConfiguration.class))
            .withUserConfiguration(LlmProxyConfiguration.class, OpenAiConfiguration.class)
            .withBean(JsonMapper.class, () -> JsonMapper.builder().build());

    @Test
    @DisplayName("With no provider, the context holds the proxy but no LLM client, and every call is NOT_CONFIGURED")
    void noProvider() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LlmProxy.class)
                    .doesNotHaveBean(LlmClient.class)
                    .doesNotHaveBean(OpenAiService.class);

            LlmResult result = context.getBean(LlmProxy.class).complete(LlmProxyTest.REQUEST);

            assertThat(result).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        });
    }

    @Test
    @DisplayName("With provider openai, the context holds exactly one LLM client, an OpenAiLlmClient, over the generated OpenAiService")
    void openAiProvider() {
        runner.withPropertyValues("ai.llm.provider=openai").run(context -> {
            assertThat(context).hasSingleBean(LlmClient.class).hasSingleBean(OpenAiService.class);
            assertThat(context.getBean(LlmClient.class)).isInstanceOf(OpenAiLlmClient.class);
        });
    }

    @Test
    @DisplayName("With provider openai but no key or model (a vault without the secret), the call is NOT_CONFIGURED")
    void openAiWithoutKey() {
        runner.withPropertyValues("ai.llm.provider=openai").run(context -> {
            LlmResult result = context.getBean(LlmProxy.class).complete(LlmProxyTest.REQUEST);

            assertThat(result).isEqualTo(new LlmResult.Unavailable(UnavailableReason.NOT_CONFIGURED));
        });
    }

    @Test
    @DisplayName("spring.http.clients.imperative.factory=jdk makes Boot build its clients on the JDK HttpClient")
    void jdkClient() {
        runner.withPropertyValues("spring.http.clients.imperative.factory=jdk").run(context ->
                assertThat(context.getBean(ClientHttpRequestFactoryBuilder.class))
                        .isInstanceOf(JdkClientHttpRequestFactoryBuilder.class));
    }

    @Test
    @DisplayName("An unknown provider fails startup")
    void unknownProvider() {
        runner.withPropertyValues("ai.llm.provider=llama").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("llama");
        });
    }

    @Test
    @DisplayName("A base URL starting with http:// fails startup (CI-llm-https)")
    void plainHttpRefused() {
        runner.withPropertyValues("ai.llm.provider=openai", "ai.llm.openai.base-url=http://api.openai.com/v1").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("must start with https://");
        });
    }

    @Test
    @DisplayName("The ai.llm defaults bind: 15 s default timeout, OpenAI's public URL")
    void defaults() {
        runner.run(context -> {
            LlmProperties properties = context.getBean(LlmProperties.class);

            assertThat(properties.defaultTimeout()).isEqualTo(Duration.ofSeconds(15));
            assertThat(properties.openai().baseUrl()).isEqualTo("https://api.openai.com/v1");
        });
    }

    @Test
    @DisplayName("A default timeout over 15 s fails startup")
    void defaultTimeoutTooLong() {
        runner.withPropertyValues("ai.llm.default-timeout=20s").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("defaultTimeout");
        });
    }

}
