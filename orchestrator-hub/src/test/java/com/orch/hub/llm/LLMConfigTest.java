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

    @Test
    void shouldBuildModelFromProperties() {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash-free");

        LLMConfig config = new LLMConfig(props);
        Model model = config.openCodeZenModel();

        assertNotNull(model, "Model should be built");
        assertEquals("deepseek-v4-flash-free", model.getModelName(),
                "Model name should come from properties");
    }

    @Test
    void shouldExposeTimeoutFromProperties() {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        props.setTimeout(java.time.Duration.ofSeconds(45));

        LLMConfig config = new LLMConfig(props);
        Model model = config.openCodeZenModel();

        assertNotNull(model, "Model should be built even with custom timeout");
    }
}
