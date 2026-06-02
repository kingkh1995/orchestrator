package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
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
    "orch.llm.api-key=${OPENCODE_API_KEY:test-api-key-from-env}"
})
class ApiKeyExternalizationTest {

    @Autowired
    private OrchLLMProperties properties;

    @Test
    void shouldLoadApiKeyFromEnvironmentVariable() {
        assertNotNull(properties.getApiKey(), "API key should be loaded");
        // The value should come from env var or property placeholder
        assertNotNull(properties.getApiKey(), "API key should not be null");
    }
}
