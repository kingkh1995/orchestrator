package com.orch.hub.llm;

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
 * Unit tests for {@link DefaultLLMProvider} — verifies delegation to
 * AgentScope-Java's {@link Model} and the XML step parser.
 */
class DefaultLLMProviderTest {

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

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        provider.generatePlan(new SessionContext("s1"));

        verify(model, times(1)).stream(anyList(), any(), any());
    }

    @Test
    void shouldExposeConfiguredModelName() {
        Model model = mock(Model.class);
        when(model.getModelName()).thenReturn("deepseek-v4-flash-free");

        DefaultLLMProvider provider = new DefaultLLMProvider(model);

        assertEquals("deepseek-v4-flash-free", provider.getModelName());
    }

    @Test
    void shouldParseValidXmlSteps() {
        Model model = mock(Model.class);
        String xml = """
                <steps>
                  <step>query user profile</step>
                  <step>validate permissions</step>
                  <step>execute transfer</step>
                </steps>""";
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock(xml))));

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertEquals(List.of("query user profile", "validate permissions", "execute transfer"),
                plan.steps());
    }

    @Test
    void shouldHandleEmptyStepsElement() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock("<steps></steps>"))));

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldReturnEmptyStepsWhenResponseIsBlank() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock(""))));

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldReturnEmptyStepsWhenResponseIsNotXml() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response(textBlock("no xml here at all"))));

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan.steps());
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldHandleNullContentBlocksGracefully() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any()))
                .thenReturn(Flux.just(response((ContentBlock) null)));

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        OrchestrationPlan plan = provider.generatePlan(new SessionContext("s1"));

        assertNotNull(plan);
        assertTrue(plan.steps().isEmpty());
    }

    @Test
    void shouldPassStreamFalseInGenerateOptions() {
        Model model = mock(Model.class);
        when(model.stream(anyList(), any(), any())).thenReturn(Flux.empty());

        DefaultLLMProvider provider = new DefaultLLMProvider(model);
        provider.generatePlan(new SessionContext("s1"));

        org.mockito.ArgumentCaptor<GenerateOptions> captor =
                org.mockito.ArgumentCaptor.forClass(GenerateOptions.class);
        verify(model).stream(anyList(), any(), captor.capture());
        GenerateOptions options = captor.getValue();

        assertEquals(Boolean.FALSE, options.getStream(),
                "Plan generation should not stream");
    }
}
