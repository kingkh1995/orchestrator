package com.orch.hub.orchestration;

import com.orch.hub.session.SessionContext;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test: exercises a real {@link ReActAgent} built via the
 * public builder, backed by a mock {@link Model}. Verifies the
 * Reason + Act loop wires through to the LLM and the engine returns
 * the assistant text. Catches configuration mistakes (missing sysPrompt,
 * wrong maxIters, missing modelName) that the unit test for
 * {@link ReActOrchestrationEngine} cannot, because the unit test mocks
 * the {@link io.agentscope.core.agent.Agent} interface directly.
 */
class ReActEngineIntegrationTest {

    /** Builds a mock model that returns a fixed single-text ChatResponse. */
    private static Model stubModel(String replyText) {
        Model model = mock(Model.class);
        when(model.getModelName()).thenReturn("test-model");
        ChatResponse response = ChatResponse.builder()
                .id("resp-1")
                .content(List.<ContentBlock>of(
                        TextBlock.builder().text(replyText).build()))
                .build();
        when(model.stream(anyList(), any(), any(GenerateOptions.class)))
                .thenReturn(Flux.just(response));
        return model;
    }

    private static ReActAgent buildTestAgent(Model model) {
        return ReActAgent.builder()
                .name("test-react-agent")
                .sysPrompt("You are a test agent.")
                .model(model)
                .generateOptions(GenerateOptions.builder()
                        .modelName("test-model")
                        .build())
                .maxIters(1)
                .build();
    }

    @Test
    void shouldRunReActAgentWithMockModel() {
        Model model = stubModel("answer from react");
        ReActOrchestrationEngine engine =
                new ReActOrchestrationEngine(buildTestAgent(model));
        SessionContext context = new SessionContext("integration-session");
        context.getTasks().add("integration-task-1");

        String result = engine.executePlan(context);

        assertNotNull(result, "Engine should return a non-null result");
        assertTrue(result.contains("answer from react"),
                "Result should carry the model output, got: " + result);
    }

    @Test
    void shouldPassSessionContextThroughReActAgentToModel() {
        Model model = stubModel("ctx echoed");
        ReActOrchestrationEngine engine =
                new ReActOrchestrationEngine(buildTestAgent(model));
        SessionContext context = new SessionContext("session-XYZ");
        engine.executePlan(context);

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<io.agentscope.core.message.Msg>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        verify(model, times(1)).stream(captor.capture(), any(), any(GenerateOptions.class));
        List<io.agentscope.core.message.Msg> sent = captor.getValue();
        assertTrue(sent.stream().anyMatch(m -> m.getTextContent() != null
                        && m.getTextContent().contains("session-XYZ")),
                "Model should have received a message containing the session id, got: "
                        + sent.stream().map(io.agentscope.core.message.Msg::getTextContent).toList());
    }
}
