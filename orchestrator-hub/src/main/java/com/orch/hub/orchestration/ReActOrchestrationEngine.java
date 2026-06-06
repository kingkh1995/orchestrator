package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

/**
 * {@link OrchestrationEngine} backed by AgentScope-Java's {@link Agent}
 * abstraction. The injected agent is typically a {@code ReActAgent}
 * (wired in {@code OrchestrationEngineConfig#reActAgent}), so execution
 * is driven by the Reason + Act loop rather than a static mock plan.
 *
 * <p>The call timeout is passed explicitly at construction time (typically
 * from {@link OrchestrationEngineConfig} which reads the value from
 * application configuration), keeping the engine free of redundant
 * property-object dependencies.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class ReActOrchestrationEngine implements OrchestrationEngine {

    private static final Duration DEFAULT_CALL_TIMEOUT = Duration.ofSeconds(60);

    private final Agent agent;
    private final Duration callTimeout;

    public ReActOrchestrationEngine(Agent agent) {
        this(agent, DEFAULT_CALL_TIMEOUT);
    }



    @Override
    public String executePlan(SessionContext context) {
        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text(buildPrompt(context))
                        .build())
                .build();

        Msg response = agent.call(List.of(userMsg))
                .block(callTimeout);

        if (response == null) {
            log.warn("ReAct agent returned null for session {}", context.getSessionId());
            return "";
        }
        return response.getTextContent();
    }

    private static String buildPrompt(SessionContext context) {
        return context.toContextString();
    }
}
