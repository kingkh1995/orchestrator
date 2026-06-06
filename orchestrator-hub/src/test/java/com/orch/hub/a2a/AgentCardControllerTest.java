package com.orch.hub.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.spec.AgentCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentCardControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private AgentCardController newController() {
        var props = new OrchA2AProperties();
        props.setAgentName("OrchestratorHub");
        props.setDescription("A2A Multi-Agent Orchestration Hub");
        props.setVersion("0.0.1");
        props.setPublicUrl("http://localhost:8080");
        props.getProvider().setOrganization("Orch");
        props.getProvider().setUrl("https://github.com/orch");
        return new AgentCardController(props);
    }

    @Test
    void shouldReturnAgentCardWithExpectedFields() throws Exception {
        var controller = newController();
        var card = controller.getAgentCard();

        assertEquals("OrchestratorHub", card.name());
        assertEquals("A2A Multi-Agent Orchestration Hub", card.description());
        assertEquals("http://localhost:8080", card.url());
        assertEquals("0.0.1", card.version());

        assertNotNull(card.provider());
        assertEquals("Orch", card.provider().organization());
        assertEquals("https://github.com/orch", card.provider().url());

        assertNotNull(card.capabilities());
        assertFalse(card.capabilities().streaming());
        assertFalse(card.capabilities().pushNotifications());
        assertFalse(card.capabilities().stateTransitionHistory());

        assertNotNull(card.defaultInputModes());
        assertTrue(card.defaultInputModes().isEmpty());
        assertNotNull(card.defaultOutputModes());
        assertTrue(card.defaultOutputModes().isEmpty());

        assertNull(card.documentationUrl());
    }

    @Test
    void shouldSerializeAgentCardToJsonRoundTrip() throws Exception {
        var controller = newController();
        var json = mapper.writeValueAsString(controller.getAgentCard());
        var roundTripped = mapper.readValue(json, AgentCard.class);

        assertEquals("OrchestratorHub", roundTripped.name());
        assertEquals("A2A Multi-Agent Orchestration Hub", roundTripped.description());
        assertEquals("http://localhost:8080", roundTripped.url());
        assertEquals("0.0.1", roundTripped.version());
    }

    @Test
    void shouldReflectConfiguredCapabilitiesInAgentCard() throws Exception {
        var props = new OrchA2AProperties();
        props.setAgentName("X");
        props.setDescription("x");
        props.setVersion("0.0.1");
        props.setPublicUrl("http://x");
        props.getProvider().setOrganization("X");
        props.getProvider().setUrl("http://x");
        props.getCapabilities().setStreaming(true);
        props.getCapabilities().setPushNotifications(true);
        props.getCapabilities().setStateTransitionHistory(true);

        var card = new AgentCardController(props).getAgentCard();

        assertTrue(card.capabilities().streaming());
        assertTrue(card.capabilities().pushNotifications());
        assertTrue(card.capabilities().stateTransitionHistory());
    }
}
