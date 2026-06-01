package com.orch.hub.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest {

    private TaskController controller;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        controller = new TaskController();
        mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldReturnCompletedTaskForTasksSendMethod() {
        var request = new JsonRpcRequest<MessageSendParams>(
                "2.0", "req-1", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null
                )
        );

        JsonRpcResponse<?> response = controller.handleTasksSend(request);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("req-1", response.id());
        assertNull(response.error());
        assertNotNull(response.result());
    }

    @Test
    void shouldReturnTaskWithCompletedStatus() {
        var request = new JsonRpcRequest<MessageSendParams>(
                "2.0", "req-2", "tasks/send", null
        );

        JsonRpcResponse<?> response = controller.handleTasksSend(request);
        Object result = response.result();

        assertNotNull(result);
        assertInstanceOf(Task.class, result);
        Task task = (Task) result;
        assertNotNull(task.getId());
        TaskStatus status = task.getStatus();
        assertNotNull(status);
        assertEquals(TaskState.COMPLETED, status.state());
        assertNotNull(task.getArtifacts());
        assertEquals(1, task.getArtifacts().size());
    }

    @Test
    void shouldReturnMethodNotFoundForUnknownMethod() {
        var request = new JsonRpcRequest<MessageSendParams>(
                "2.0", "req-3", "unknown.method", null
        );

        JsonRpcResponse<?> response = controller.handleTasksSend(request);

        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32601, response.error().code());
        assertEquals("Method not found", response.error().message());
    }

    @Test
    void shouldReturnInvalidRequestWhenJsonRpcFieldIsMissing() {
        var request = new JsonRpcRequest<MessageSendParams>(
                null, "req-4", "tasks/send", null
        );

        JsonRpcResponse<?> response = controller.handleTasksSend(request);

        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32600, response.error().code());
        assertEquals("Invalid Request", response.error().message());
        assertEquals("req-4", response.id());
    }

    @Test
    void shouldReturnInvalidRequestWhenJsonRpcVersionIsNot2Point0() {
        var request = new JsonRpcRequest<MessageSendParams>(
                "1.0", "req-5", "tasks/send", null
        );

        JsonRpcResponse<?> response = controller.handleTasksSend(request);

        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32600, response.error().code());
        assertEquals("Invalid Request", response.error().message());
    }
}
