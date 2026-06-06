package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import com.orch.hub.orchestration.OrchestrationEngine;
import com.orch.hub.session.SessionContext;
import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    private OrchestrationEngine engine;
    private TaskController controller;

    @BeforeEach
    void setUp() {
        engine = mock(OrchestrationEngine.class);
        when(engine.executePlan(any(SessionContext.class))).thenReturn("engine response");
        controller = new TaskController(engine);
    }

    @Test
    void shouldReturnCompletedTaskForTasksSendMethod() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-1", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null));

        var response = controller.handleTasksSend(request);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("req-1", response.id());
        assertNull(response.error());
        assertNotNull(response.result());
    }

    @Test
    void shouldReturnInvalidParamsWhenMessageIsNull() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-2", "tasks/send", null);

        var response = controller.handleTasksSend(request);

        assertNotNull(response.error());
        assertEquals(-32600, response.error().code());
        assertTrue(response.error().message().contains("Invalid parameters"));
        assertEquals("req-2", response.id());
        verifyNoInteractions(engine);
    }

    @Test
    void shouldPreferMessageContextIdOverRequestId() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-2c", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .contextId("sess-from-context")
                                .build(),
                        null, null));

        ArgumentCaptor<SessionContext> captor = ArgumentCaptor.forClass(SessionContext.class);
        controller.handleTasksSend(request);
        verify(engine).executePlan(captor.capture());
        assertEquals("sess-from-context", captor.getValue().getSessionId());
    }
    @Test
    void shouldReturnTaskWithCompletedStatus() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-2d", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null));

        var response = controller.handleTasksSend(request);
        var task = (Task) response.result();

        assertNotNull(task);
        assertNotNull(task.getId());
        assertNotNull(task.getStatus());
        assertEquals(TaskState.COMPLETED, task.getStatus().state());
    }

    @Test
    void shouldEmbedEngineResponseAsTextPartArtifact() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-text", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null));

        var response = controller.handleTasksSend(request);
        var task = (Task) response.result();

        assertEquals(1, task.getArtifacts().size());
        Artifact artifact = task.getArtifacts().get(0);
        assertEquals(1, artifact.parts().size());
        Part<?> part = artifact.parts().get(0);
        assertInstanceOf(TextPart.class, part);
        assertEquals("engine response", ((TextPart) part).getText());
    }

    @Test
    void shouldReturnMethodNotFoundForUnknownMethod() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-3", "unknown.method", null);

        var response = controller.handleTasksSend(request);

        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32601, response.error().code());
        assertEquals("Method not found", response.error().message());
    }

    @Test
    void shouldReturnInvalidRequestWhenJsonRpcVersionIsNot2Point0() {
        var request = JsonRpcRequest.<MessageSendParams>of("1.0", "req-5", "tasks/send", null);

        var response = controller.handleTasksSend(request);

        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32600, response.error().code());
        assertEquals("Request payload validation error", response.error().message());
    }

    @Test
    void shouldDelegateToEngineOnceWithSessionContext() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "sess-xyz", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null));

        controller.handleTasksSend(request);

        ArgumentCaptor<SessionContext> captor = ArgumentCaptor.forClass(SessionContext.class);
        verify(engine, times(1)).executePlan(captor.capture());
        assertEquals("sess-xyz", captor.getValue().getSessionId());
    }
}
