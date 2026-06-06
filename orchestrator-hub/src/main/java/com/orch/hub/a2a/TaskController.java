package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcError;
import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import com.orch.hub.orchestration.OrchestrationEngine;
import com.orch.hub.session.SessionContext;
import io.a2a.spec.Artifact;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * A2A {@code tasks/send} endpoint. Receives an external request and
 * delegates to the {@link OrchestrationEngine} — the controller itself
 * has no knowledge of sub-agents, transports, or messaging backends.
 *
 * <p>External callers see only the orchestrator (this hub); sub-agent
 * routing is an internal implementation detail of the engine. If and
 * when the engine decides a sub-agent is needed, it dispatches through
 * the RocketMQ transport on its own.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final OrchestrationEngine engine;

    @PostMapping("/tasks/send")
    public JsonRpcResponse<?> handleTasksSend(@RequestBody JsonRpcRequest<MessageSendParams> request) {
        if (!"2.0".equals(request.jsonrpc())) {
            return JsonRpcResponse.error("2.0", request.id(),
                    JsonRpcError.of(InvalidRequestError.DEFAULT_CODE, "Request payload validation error", null));
        }

        if (!"tasks/send".equals(request.method())) {
            return JsonRpcResponse.error("2.0", request.id(),
                    JsonRpcError.of(MethodNotFoundError.DEFAULT_CODE, "Method not found", null));
        }

        MessageSendParams params = request.params();
        if (params == null || params.message() == null) {
            return JsonRpcResponse.error("2.0", request.id(),
                    JsonRpcError.of(InvalidRequestError.DEFAULT_CODE, "Invalid parameters: message is required", null));
        }

        String sessionId = params.message().getContextId() != null
                ? params.message().getContextId()
                : request.id();
        SessionContext context = new SessionContext(sessionId);
        String responseText = engine.executePlan(context);

        var artifact = new Artifact.Builder()
                .artifactId(UUID.randomUUID().toString())
                .name("response.txt")
                .parts(new TextPart(responseText))
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
