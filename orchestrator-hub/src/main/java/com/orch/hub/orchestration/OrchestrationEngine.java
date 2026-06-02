package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;

public interface OrchestrationEngine {

    /**
     * Execute an orchestration plan for the given session.
     *
     * @param context the current session context
     * @return the execution result
     */
    String executePlan(SessionContext context);
}
