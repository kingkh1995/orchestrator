package com.orch.hub.llm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.llm.api-key=valid-test-key"
})
class StartupValidationTest {

    @Test
    void shouldStartSuccessfullyWithValidApiKey() {
        // If context loads, startup validation passed
        assertDoesNotThrow(() -> {
            // Spring context already loaded in @SpringBootTest
        }, "Application should start with valid API key");
    }
}
