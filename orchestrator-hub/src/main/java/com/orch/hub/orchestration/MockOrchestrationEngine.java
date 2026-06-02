package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;

public class MockOrchestrationEngine implements OrchestrationEngine {

    @Override
    public String executePlan(SessionContext context) {
        // MVP: Return static result
        return "mock-execution-result";
    }
}
