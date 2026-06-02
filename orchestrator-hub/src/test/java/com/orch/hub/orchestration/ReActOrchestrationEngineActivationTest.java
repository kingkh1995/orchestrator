package com.orch.hub.orchestration;

import io.agentscope.core.agent.Agent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the ReAct-based orchestration engine is wired when
 * {@code orch.orchestration.react.enabled=true}, replacing the default
 * {@link MockOrchestrationEngine}.
 */
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "orch.orchestration.react.enabled=true",
        "orch.llm.api-key=test-key"
})
class ReActOrchestrationEngineActivationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldRegisterReActAgentBean() {
        assertNotNull(context.getBean(Agent.class),
                "ReActAgent (exposed as Agent) should be registered when react is enabled");
    }

    @Test
    void shouldRegisterReActOrchestrationEngine() {
        OrchestrationEngine engine = context.getBean(OrchestrationEngine.class);
        assertNotNull(engine, "OrchestrationEngine should be registered");
        assertTrue(engine instanceof ReActOrchestrationEngine,
                "Default engine should be ReActOrchestrationEngine when react is enabled, got: "
                        + engine.getClass().getName());
    }
}
