package com.orch.hub.a2a;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.a2a.agent-name=TestAgent",
    "orch.a2a.description=Test A2A description",
    "orch.a2a.version=0.0.1-test",
    "orch.a2a.public-url=https://test.example.com:8443",
    "orch.a2a.provider.organization=TestOrg",
    "orch.a2a.provider.url=https://test.example.com/org",
    "orch.a2a.capabilities.streaming=true",
    "orch.a2a.capabilities.push-notifications=false",
    "orch.a2a.capabilities.state-transition-history=true",
    "orch.a2a.default-input-modes=text,data",
    "orch.a2a.default-output-modes=text",
    "orch.a2a.documentation-url=https://test.example.com/docs"
})
class OrchA2APropertiesTest {

    @Autowired
    private OrchA2AProperties properties;

    @Test
    void shouldBindAgentName() {
        assertEquals("TestAgent", properties.getAgentName());
    }

    @Test
    void shouldBindDescription() {
        assertEquals("Test A2A description", properties.getDescription());
    }

    @Test
    void shouldBindVersion() {
        assertEquals("0.0.1-test", properties.getVersion());
    }

    @Test
    void shouldBindPublicUrl() {
        assertEquals("https://test.example.com:8443", properties.getPublicUrl());
    }

    @Test
    void shouldBindProvider() {
        assertEquals("TestOrg", properties.getProvider().getOrganization());
        assertEquals("https://test.example.com/org", properties.getProvider().getUrl());
    }

    @Test
    void shouldBindCapabilities() {
        assertTrue(properties.getCapabilities().isStreaming());
        assertFalse(properties.getCapabilities().isPushNotifications());
        assertTrue(properties.getCapabilities().isStateTransitionHistory());
    }

    @Test
    void shouldBindDefaultModesAsList() {
        assertEquals(List.of("text", "data"), properties.getDefaultInputModes());
        assertEquals(List.of("text"), properties.getDefaultOutputModes());
    }

    @Test
    void shouldBindDocumentationUrl() {
        assertEquals("https://test.example.com/docs", properties.getDocumentationUrl());
    }
}
