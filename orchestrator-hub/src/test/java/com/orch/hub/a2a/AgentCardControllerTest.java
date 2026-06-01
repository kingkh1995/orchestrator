package com.orch.hub.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.spec.AgentCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentCardControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldReturnAgentCardWithExpectedFields() throws Exception {
        var controller = new AgentCardController();
        AgentCard card = controller.getAgentCard();

        assertEquals("OrchestratorHub", card.name());
        assertEquals("A2A Multi-Agent Orchestration Hub", card.description());
        assertEquals("http://localhost:8080", card.url());
        assertEquals("0.0.1", card.version());
    }

    @Test
    void shouldSerializeAgentCardToJsonRoundTrip() throws Exception {
        var controller = new AgentCardController();
        String json = mapper.writeValueAsString(controller.getAgentCard());
        AgentCard roundTripped = mapper.readValue(json, AgentCard.class);

        assertEquals("OrchestratorHub", roundTripped.name());
        assertEquals("A2A Multi-Agent Orchestration Hub", roundTripped.description());
        assertEquals("http://localhost:8080", roundTripped.url());
        assertEquals("0.0.1", roundTripped.version());
    }
}
