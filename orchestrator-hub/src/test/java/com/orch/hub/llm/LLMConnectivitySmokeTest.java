package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.llm.provider=opencode-zen",
    "orch.llm.model=deepseek-v4-flash-free",
    "orch.llm.api-key=smoke-test-key",
    "orch.llm.timeout=30s",
    "orch.llm.max-retries=3"
})
class LLMConnectivitySmokeTest {

    @Autowired
    private OrchLLMProperties properties;

    @Autowired
    private OrchestrationLLM orchestrationLLM;

    @Test
    void shouldLoadLLMProperties() {
        assertNotNull(properties, "OrchLLMProperties should be loaded");
        assertNotNull(properties.getApiKey(), "API key should be set");
        assertNotNull(properties.getTimeout(), "Timeout should be set");
        assertTrue(properties.getMaxRetries() > 0, "Max retries should be positive");
    }

    @Test
    void shouldHaveLLMBeanRegistered() {
        assertNotNull(orchestrationLLM, "OrchestrationLLM bean should be registered");
        assertTrue(orchestrationLLM instanceof DefaultLLMProvider,
                "LLM bean should be DefaultLLMProvider");
    }
}
