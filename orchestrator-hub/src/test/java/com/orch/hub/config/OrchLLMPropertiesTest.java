package com.orch.hub.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.llm.provider=opencode-zen",
    "orch.llm.model=deepseek-v4-flash-free",
    "orch.llm.api-key=test-key",
    "orch.llm.timeout=30s",
    "orch.llm.max-retries=3"
})
class OrchLLMPropertiesTest {

    @Autowired
    private OrchLLMProperties properties;

    @Test
    void shouldBindTimeoutFromYaml() {
        assertNotNull(properties.getTimeout(), "timeout should be bound from configuration");
        assertEquals(30, properties.getTimeout().getSeconds(), "timeout should be 30 seconds");
    }

    @Test
    void shouldBindMaxRetriesFromYaml() {
        assertEquals(3, properties.getMaxRetries(), "max-retries should be bound from configuration");
    }

}
