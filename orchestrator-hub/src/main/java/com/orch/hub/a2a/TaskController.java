package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcError;
import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import io.a2a.spec.Artifact;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** A2A tasks/send endpoint. */
@RestController
public class TaskController {

    @PostMapping("/tasks/send")
    public JsonRpcResponse<?> handleTasksSend(@RequestBody JsonRpcRequest<MessageSendParams> request) {
        /** @see io.a2a.spec.InvalidRequestError */
        if (!"2.0".equals(request.jsonrpc())) {
            return JsonRpcResponse.error("2.0", request.id(), JsonRpcError.of(InvalidRequestError.DEFAULT_CODE, "Request payload validation error", null));
        }

        /** @see io.a2a.spec.MethodNotFoundError */
        if (!"tasks/send".equals(request.method())) {
            return JsonRpcResponse.error("2.0", request.id(), JsonRpcError.of(MethodNotFoundError.DEFAULT_CODE, "Method not found", null));
        }

        // Build mock Task with COMPLETED status
        var artifact = new Artifact.Builder()
                .artifactId(UUID.randomUUID().toString())
                .name("response.txt")
                .parts(new TextPart("Mock response from OrchestratorHub"))
                .build();

        var task = new Task.Builder()
                .id(UUID.randomUUID().toString())
                .contextId("ctx-" + System.currentTimeMillis())
                .status(new TaskStatus(TaskState.COMPLETED))
                .artifacts(List.of(artifact))
                .build();

        return JsonRpcResponse.result("2.0", request.id(), task);
    }
}
