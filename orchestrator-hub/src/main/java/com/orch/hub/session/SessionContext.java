package com.orch.hub.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class SessionContext {

    private final String sessionId;
    private final List<String> tasks = new ArrayList<>();
    private final Map<String, Object> states = new HashMap<>();

    /**
     * Serializes the session identity and accumulated state into a single
     * line for use in LLM prompts. Centralized so the format cannot drift
     * between callers (plan generation, ReAct engine).
     */
    public String toContextString() {
        return "session=" + sessionId
                + " tasks=" + tasks
                + " states=" + states;
    }
}
