package com.orch.hub.llm;

import com.orch.hub.session.SessionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrchestrationLLMTest {

    @Test
    void shouldDefineGeneratePlanMethod() {
        // Verify interface exists and has the expected method signature
        assertTrue(OrchestrationLLM.class.isInterface(),
                "OrchestrationLLM should be an interface");

        // Verify generatePlan method exists
        try {
            var method = OrchestrationLLM.class.getMethod("generatePlan", SessionContext.class);
            assertNotNull(method, "generatePlan method should exist");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("OrchestrationLLM should have generatePlan(SessionContext) method", e);
        }
    }
}
