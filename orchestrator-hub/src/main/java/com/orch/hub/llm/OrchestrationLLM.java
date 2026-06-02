package com.orch.hub.llm;

import com.orch.hub.session.SessionContext;

public interface OrchestrationLLM {

    /**
     * Generate an orchestration plan based on the session context.
     *
     * @param context the current session context
     * @return an OrchestrationPlan containing the execution steps
     */
    OrchestrationPlan generatePlan(SessionContext context);
}
