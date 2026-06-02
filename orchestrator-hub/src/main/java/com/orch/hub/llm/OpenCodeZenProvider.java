package com.orch.hub.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orch.hub.config.OrchLLMProperties;
import com.orch.hub.session.SessionContext;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link OrchestrationLLM} implementation backed by AgentScope-Java's
 * {@link Model} abstraction. Plan generation is delegated to the injected
 * model (typically an {@code OpenAIChatModel} configured for the OpenCode Zen
 * endpoint). The model response is parsed as a JSON array of step strings.
 */
@Slf4j
@Component
public class OpenCodeZenProvider implements OrchestrationLLM {

    static final String PLAN_SYS_PROMPT =
            "You are an orchestration planner. Given a session context, output a "
                    + "JSON array of strings, each string being a single execution step. "
                    + "Output ONLY the JSON array, no other text. Example: [\"step1\",\"step2\"]";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Model model;
    private final OrchLLMProperties properties;

    public OpenCodeZenProvider(Model model, OrchLLMProperties properties) {
        this.model = model;
        this.properties = properties;
    }

    public String getModelName() {
        return properties.getModel();
    }

    @Override
    public OrchestrationPlan generatePlan(SessionContext context) {
        List<Msg> messages = List.of(
                Msg.builder()
                        .role(MsgRole.SYSTEM)
                        .content(TextBlock.builder().text(PLAN_SYS_PROMPT).build())
                        .build(),
                Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder()
                                .text("session=" + context.getSessionId()
                                        + " tasks=" + context.getTasks()
                                        + " states=" + context.getStates())
                                .build())
                        .build());

        GenerateOptions options = buildGenerateOptions();
        String body = model.stream(messages, Collections.emptyList(), options)
                .map(this::extractText)
                .filter(s -> !s.isEmpty())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + b)
                .block(Duration.ofSeconds(properties.getTimeout().toSeconds()));

        return new OrchestrationPlan(parseSteps(body));
    }

    private GenerateOptions buildGenerateOptions() {
        return GenerateOptions.builder()
                .modelName(properties.getModel())
                .apiKey(properties.getApiKey())
                .stream(false)
                .build();
    }

    private String extractText(io.agentscope.core.model.ChatResponse resp) {
        if (resp == null) {
            return "";
        }
        List<ContentBlock> blocks = resp.getContent();
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : blocks) {
            if (block instanceof TextBlock tb) {
                sb.append(Objects.requireNonNullElse(tb.getText(), ""));
            }
        }
        return sb.toString();
    }

    /**
     * Parses the model response body as a JSON array of strings. Returns an
     * empty list if the body is blank, contains no array, or fails to parse.
     * Visible for testing.
     */
    static List<String> parseSteps(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        // Locate the first '[' ... last ']' substring; tolerate prose around it.
        int start = body.indexOf('[');
        int end = body.lastIndexOf(']');
        if (start < 0 || end < 0 || end <= start) {
            log.warn("LLM response did not contain a JSON array; raw: {}", body);
            return List.of();
        }
        String array = body.substring(start, end + 1);
        try {
            String[] arr = MAPPER.readValue(array, String[].class);
            if (arr == null) {
                return List.of();
            }
            return java.util.Arrays.stream(arr)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } catch (JsonProcessingException e) {
            log.warn("LLM response was not a valid JSON string array: {}", array);
            return List.of();
        }
    }
}
