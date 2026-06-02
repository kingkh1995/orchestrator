package com.orch.hub.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionContext {

    private final String sessionId;
    private final List<String> tasks = new ArrayList<>();
    private final Map<String, Object> states = new HashMap<>();

    public SessionContext(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public Map<String, Object> getStates() {
        return states;
    }
}
