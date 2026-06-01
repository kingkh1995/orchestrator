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
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-1", "tasks/send",
                new MessageSendParams(
                        new Message.Builder()
                                .role(Message.Role.USER)
                                .parts(List.of(new TextPart("Hello")))
                                .build(),
                        null, null
                ));

        var response = controller.handleTasksSend(request);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("req-1", response.id());
        assertNull(response.error());
        assertNotNull(response.result());
    }

    @Test
    void shouldReturnTaskWithCompletedStatus() {
        var request = JsonRpcRequest.<MessageSendParams>of("2.0", "req-2", "tasks/send", null);

        var response = controller.handleTasksSend(request);
        var result = response.result();

        assertNotNull(result);
        assertInstanceOf(Task.class, result);
        var task = (Task) result;
        assertNotNull(task.getId());
        var status = task.getStatus();
        assertNotNull(status);
        assertEquals(TaskState.COMPLETED, status.state());
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
}
