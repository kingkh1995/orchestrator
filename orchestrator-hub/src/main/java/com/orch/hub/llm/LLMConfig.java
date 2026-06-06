package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires AgentScope-Java's {@link OpenAIChatModel} as the application's
 * {@link Model} bean. Endpoint, model name, timeout and retry settings
 * are all sourced from {@link OrchLLMProperties}.
 *
 * <p>The built-in retry/backoff machinery ({@link ExecutionConfig}) is used
 * instead of ad-hoc wrapping. When a property is not configured, agentscope's
 * own defaults apply (5-minute timeout, 3 max attempts).</p>
 */
@Configuration
@EnableConfigurationProperties(OrchLLMProperties.class)
@RequiredArgsConstructor
public class LLMConfig {

    private final OrchLLMProperties properties;

    /**
     * Builds an {@link OpenAIChatModel} configured from {@link OrchLLMProperties}.
     * Model is constructed eagerly so configuration errors surface during
     * application startup, not on the first LLM call.
     */
    @Bean
    public Model llmModel() {
        var execCfg = ExecutionConfig.builder();
        if (properties.getTimeout() != null) {
            execCfg.timeout(properties.getTimeout());
        }
        if (properties.getMaxRetries() != null) {
            // maxRetries = retry count → maxAttempts = initial + retries
            execCfg.maxAttempts(properties.getMaxRetries() + 1);
        }

        return OpenAIChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .baseUrl(properties.getBaseUrl())
                .endpointPath(properties.getEndpointPath())
                .stream(true)
                .generateOptions(io.agentscope.core.model.GenerateOptions.builder()
                        .executionConfig(execCfg.build())
                        .build())
                .build();
    }
}
