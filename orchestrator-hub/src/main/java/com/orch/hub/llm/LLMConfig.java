package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires AgentScope-Java's {@link OpenAIChatModel} as the application's
 * {@link Model} bean, configured for the OpenCode Zen endpoint.
 */
@Configuration
public class LLMConfig {

    static final String OPENCODE_ZEN_BASE_URL = "https://opencode.ai/zen/v1";
    static final String OPENCODE_ZEN_CHAT_PATH = "/chat/completions";

    private final OrchLLMProperties properties;

    public LLMConfig(OrchLLMProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds an {@link OpenAIChatModel} targeting the OpenCode Zen endpoint.
     * Model is constructed eagerly so configuration errors surface during
     * application startup, not on the first LLM call.
     */
    @Bean
    public Model openCodeZenModel() {
        return OpenAIChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .baseUrl(OPENCODE_ZEN_BASE_URL)
                .endpointPath(OPENCODE_ZEN_CHAT_PATH)
                .stream(true)
                .build();
    }
}
