package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReActOrchestrationEngine} — verifies delegation to
 * AgentScope-Java's {@link Agent} (typically a {@code ReActAgent}).
 */
class ReActOrchestrationEngineTest {

    @Test
    void shouldDelegateExecutionToAgentscopeAgent() {
        Agent agent = mock(Agent.class);
        Msg response = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .content(TextBlock.builder().text("executed").build())
                .build();
        when(agent.call(anyList())).thenReturn(Mono.just(response));

        ReActOrchestrationEngine engine = new ReActOrchestrationEngine(agent);
        SessionContext context = new SessionContext("test-session");

        String result = engine.executePlan(context);

        assertNotNull(result, "Result should not be null");
        verify(agent, times(1)).call(anyList());
    }

    @Test
    void shouldReturnAgentResponseTextAsResult() {
        Agent agent = mock(Agent.class);
        Msg response = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .content(TextBlock.builder().text("the answer is 42").build())
                .build();
        when(agent.call(anyList())).thenReturn(Mono.just(response));

        ReActOrchestrationEngine engine = new ReActOrchestrationEngine(agent);
        SessionContext context = new SessionContext("test-session");

        String result = engine.executePlan(context);

        assertEquals("the answer is 42", result);
    }

    @Test
    void shouldPassSessionContextToAgent() {
        Agent agent = mock(Agent.class);
        Msg response = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .content(TextBlock.builder().text("ok").build())
                .build();
        when(agent.call(anyList())).thenReturn(Mono.just(response));

        ReActOrchestrationEngine engine = new ReActOrchestrationEngine(agent);
        SessionContext context = new SessionContext("my-session-id");
        context.getTasks().add("task-1");

        engine.executePlan(context);

        org.mockito.ArgumentCaptor<List<Msg>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        verify(agent).call(captor.capture());
        List<Msg> sent = captor.getValue();
        assertEquals(1, sent.size(), "Should send a single user message to the agent");
        String text = sent.get(0).getTextContent();
        assertTrue(text.contains("my-session-id"),
                "User message should carry the session id, got: " + text);
        assertTrue(text.contains("task-1"),
                "User message should carry the session tasks, got: " + text);
    }
    @Test
    void shouldUseExplicitTimeoutConstructor() {
        Agent agent = mock(Agent.class);
        when(agent.call(anyList())).thenReturn(Mono.just(
                Msg.builder()
                        .role(MsgRole.ASSISTANT)
                        .content(TextBlock.builder().text("done").build())
                        .build()));

        ReActOrchestrationEngine engine =
                new ReActOrchestrationEngine(agent, Duration.ofSeconds(5));
        String result = engine.executePlan(new SessionContext("s"));

        assertEquals("done", result);
    }

    @Test
    void shouldReturnEmptyStringWhenAgentReturnsNull() {
        Agent agent = mock(Agent.class);
        when(agent.call(anyList())).thenReturn(Mono.empty());

        ReActOrchestrationEngine engine = new ReActOrchestrationEngine(agent);
        String result = engine.executePlan(new SessionContext("null-session"));

        assertEquals("", result);
        verify(agent, times(1)).call(anyList());
    }
}
