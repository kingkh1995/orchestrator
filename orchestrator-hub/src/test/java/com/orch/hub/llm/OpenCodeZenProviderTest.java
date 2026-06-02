package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import com.orch.hub.session.SessionContext;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OpenCodeZenProvider} — verifies delegation to
 * AgentScope-Java's {@link Model} abstraction and the JSON-array parser.
 */
class OpenCodeZenProviderTest {

    private static ContentBlock textBlock(String text) {
        return TextBlock.builder().text(text).build();
    }

    /** Varargs helper that allows null elements in the list. */
    private static ChatResponse response(ContentBlock... blocks) {
        ArrayList<ContentBlock> list = new ArrayList<>();
        Collections.addAll(list, blocks);
        return ChatResponse.builder()
                .id("resp-1")
                .content(list)
                .build();
    }

    @Test
    void shouldDelegatePlanGenerationToAgentscopeModel() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any())).thenReturn(Flux.empty());

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        provider.generatePlan(new SessionContext("s1"));

        verify(model, times(1)).stream(anyList(), any(), any());
    }

    @Test
    void shouldExposeConfiguredModelName() {
        Model model = mock(Model.class);
        OrchLLMProperties props = new OrchLLMProperties();
        props.setModel("deepseek-v4-flash-free");

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, props);

        assertEquals("deepseek-v4-flash-free", provider.getModelName());
    }

    @Test
    void shouldParseValidJsonArrayIntoSteps() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock("[\"step-a\",\"step-b\",\"step-c\"]"))));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertEquals(List.of("step-a", "step-b", "step-c"), plan.steps());
    }

    @Test
    void shouldParseJsonArraySurroundedByProse() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock(
                        "Here is the plan:\n[\"first\", \"second\"]\nDone."))));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertEquals(List.of("first", "second"), plan.steps());
    }

    @Test
    void shouldHandleEmptyArray() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock("[]"))));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldReturnEmptyStepsWhenResponseIsBlank() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock(""))));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldReturnEmptyStepsWhenResponseIsNotJson() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock("no json here at all"))));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldHandleNullContentBlocksGracefully() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response((ContentBlock) null)));

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan);
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldApplyModelNameAndApiKeyToGenerateOptions() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any())).thenReturn(Flux.empty());

        OrchLLMProperties props = new OrchLLMProperties();
        props.setModel("custom-model");
        props.setApiKey("custom-key");
        props.setTimeout(java.time.Duration.ofSeconds(45));
        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, props);
        provider.generatePlan(new SessionContext("s1"));

        org.mockito.ArgumentCaptor<GenerateOptions> captor =
                org.mockito.ArgumentCaptor.forClass(GenerateOptions.class);
        verify(model).stream(anyList(), any(), captor.capture());
        GenerateOptions options = captor.getValue();

        assertEquals("custom-model", options.getModelName(),
                "GenerateOptions.modelName must come from properties");
        assertEquals("custom-key", options.getApiKey(),
                "GenerateOptions.apiKey must come from properties");
    }

    @Test
    void shouldStreamFalseByDefaultForPlanGeneration() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any())).thenReturn(Flux.empty());

        OpenCodeZenProvider provider = new OpenCodeZenProvider(model, new OrchLLMProperties());
        provider.generatePlan(new SessionContext("s1"));

        org.mockito.ArgumentCaptor<GenerateOptions> captor =
                org.mockito.ArgumentCaptor.forClass(GenerateOptions.class);
        verify(model).stream(anyList(), any(), captor.capture());
        GenerateOptions options = captor.getValue();

        assertEquals(Boolean.FALSE, options.getStream(),
                "Plan generation should not stream — it needs the full response");
    }
}
