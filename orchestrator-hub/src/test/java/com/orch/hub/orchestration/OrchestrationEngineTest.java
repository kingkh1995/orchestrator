package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrchestrationEngineTest {

    @Test
    void shouldDefineExecutePlanMethod() {
        assertTrue(OrchestrationEngine.class.isInterface(),
                "OrchestrationEngine should be an interface");

        try {
            var method = OrchestrationEngine.class.getMethod("executePlan", SessionContext.class);
            assertNotNull(method, "executePlan method should exist");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("OrchestrationEngine should have executePlan(SessionContext) method", e);
        }
    }
}
