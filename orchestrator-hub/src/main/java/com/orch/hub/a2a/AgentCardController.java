package com.orch.hub.a2a;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** A2A agent card discovery endpoint. */
@RestController
public class AgentCardController {

    @GetMapping("/.well-known/agent.json")
    public AgentCard getAgentCard() {
        return new AgentCard.Builder()
                .name("OrchestratorHub")
                .description("A2A Multi-Agent Orchestration Hub")
                .url("http://localhost:8080")
                .version("0.0.1")
                .provider(new AgentProvider("Orch", "https://github.com/orch"))
                .capabilities(new AgentCapabilities(false, false, false, null))
                .defaultInputModes(List.of())
                .defaultOutputModes(List.of())
                .skills(List.of())
                .build();
    }
}
