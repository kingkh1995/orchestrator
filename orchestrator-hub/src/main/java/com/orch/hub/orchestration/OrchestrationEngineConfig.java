package com.orch.hub.orchestration;

import com.orch.hub.config.OrchLLMProperties;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.model.Model;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestrationEngineConfig {

    private static final String AGENT_NAME = "orchestrator-react-agent";
    private static final String SYS_PROMPT =
            "You are the OrchestratorHub ReAct agent. Given a session context "
                    + "(session id, tasks, states), reason about the next step and "
                    + "produce a concise textual answer.";

    /**
     * Real ReAct-based orchestration engine, exposed only when the property
     * is set. By default the {@link MockOrchestrationEngine} is wired
     * (see {@link #mockOrchestrationEngine}).
     */
    @Bean
    @ConditionalOnProperty(name = "orch.orchestration.react.enabled", havingValue = "true")
    public Agent reActAgent(Model model) {
        return ReActAgent.builder()
                .name(AGENT_NAME)
                .sysPrompt(SYS_PROMPT)
                .model(model)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "orch.orchestration.react.enabled", havingValue = "true")
    public OrchestrationEngine reActOrchestrationEngine(Agent agent, OrchLLMProperties properties) {
        return new ReActOrchestrationEngine(agent, properties);
    }

    /**
     * Default fallback engine. Active unless an explicit
     * {@link OrchestrationEngine} bean is registered (e.g. when
     * {@code orch.orchestration.react.enabled=true} activates the ReAct
     * engine).
     */
    @Bean
    @ConditionalOnMissingBean(OrchestrationEngine.class)
    public OrchestrationEngine mockOrchestrationEngine() {
        return new MockOrchestrationEngine();
    }
}
