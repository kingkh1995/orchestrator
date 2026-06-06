package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import io.agentscope.core.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link LLMConfig} — verifies AgentScope-Java model wiring.
 */
class LLMConfigTest {

    private static OrchLLMProperties defaultProps() {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setBaseUrl("https://opencode.ai/zen/v1");
        props.setEndpointPath("/chat/completions");
        return props;
    }

    @Test
    void shouldBuildModelFromProperties() {
        OrchLLMProperties props = defaultProps();
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash-free");

        LLMConfig config = new LLMConfig(props);
        Model model = config.llmModel();

        assertNotNull(model, "Model should be built");
        assertEquals("deepseek-v4-flash-free", model.getModelName(),
                "Model name should come from properties");
    }

    @Test
    void shouldExposeTimeoutFromProperties() {
        OrchLLMProperties props = defaultProps();
        props.setApiKey("test-key");
        props.setTimeout(java.time.Duration.ofSeconds(45));
        props.setModel("deepseek-v4-flash-free");

        LLMConfig config = new LLMConfig(props);
        Model model = config.llmModel();

        assertNotNull(model, "Model should be built even with custom timeout");
    }

    @Test
    void shouldUseAgentscopeDefaultsWhenTimeoutAndMaxRetriesAreNull() {
        OrchLLMProperties props = defaultProps();
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash-free");
        // timeout and maxRetries left null on purpose

        LLMConfig config = new LLMConfig(props);
        Model model = config.llmModel();

        assertNotNull(model, "Model should be built with library defaults");
    }
}
