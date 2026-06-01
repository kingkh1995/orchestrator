package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcError;
import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import io.a2a.spec.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TaskController {

    @PostMapping(value = "/tasks/send", consumes = "application/json")
    public JsonRpcResponse<?> handleTasksSend(@RequestBody JsonRpcRequest<MessageSendParams> request) {
        if (!"2.0".equals(request.jsonrpc())) {
            return new JsonRpcResponse<>(
                    "2.0", request.id(), null,
                    new JsonRpcError(-32600, "Invalid Request", null)
            );
        }

        if (!"tasks/send".equals(request.method())) {
            return new JsonRpcResponse<>(
                    "2.0", request.id(), null,
                    new JsonRpcError(-32601, "Method not found", null)
            );
        }

        // Build mock Task with COMPLETED status
        var artifact = new Artifact.Builder()
                .artifactId(UUID.randomUUID().toString())
                .name("response.txt")
                .parts(new TextPart("Mock response from OrchestratorHub"))
                .build();

        Task task = new Task.Builder()
                .id(UUID.randomUUID().toString())
                .contextId("ctx-" + System.currentTimeMillis())
                .status(new TaskStatus(TaskState.COMPLETED))
                .artifacts(List.of(artifact))
                .build();

        return new JsonRpcResponse<>("2.0", request.id(), task, null);
    }
}
