package com.orch.hub.orchestration;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OrchestrationEngineConfig {

    private static final String AGENT_NAME = "orchestrator-react-agent";
    private static final String SYS_PROMPT = """
            You are the OrchestratorHub ReAct agent. Given a session context \
            (session id, tasks, states), reason about the next step and \
            produce a concise textual answer.""";

    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public Agent reActAgent(Model model) {
        return ReActAgent.builder()
                .name(AGENT_NAME)
                .sysPrompt(SYS_PROMPT)
                .model(model)
                .build();
    }

    @Bean
    public OrchestrationEngine reActOrchestrationEngine(Agent agent) {
        return new ReActOrchestrationEngine(agent, CALL_TIMEOUT);
    }
}
