package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableAutoConfiguration
class MockOrchestrationEngineTest {

    @Autowired
    private OrchestrationEngine orchestrationEngine;

    @Test
    void shouldInjectMockOrchestrationEngine() {
        assertNotNull(orchestrationEngine, "OrchestrationEngine should be injected");
        assertEquals(MockOrchestrationEngine.class, orchestrationEngine.getClass(),
                "OrchestrationEngine should be MockOrchestrationEngine");
    }

    @Test
    void shouldExecutePlan() {
        SessionContext context = new SessionContext("test-session");
        String result = orchestrationEngine.executePlan(context);
        assertNotNull(result, "Execution result should not be null");
    }
}
