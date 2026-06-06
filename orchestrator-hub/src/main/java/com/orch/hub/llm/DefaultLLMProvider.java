package com.orch.hub.llm;

import com.orch.hub.session.SessionContext;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default {@link OrchestrationLLM} implementation backed by AgentScope-Java's
 * {@link Model} abstraction. Plan generation is delegated to the injected
 * model (typically an {@code OpenAIChatModel}) whose endpoint, api key, model
 * name, timeout, and retry settings are already configured at bean construction
 * time by {@link LLMConfig}.
 *
 * <p>The provider is stateless with respect to the LLM connection — it relies
 * entirely on the injected {@link Model} for configuration, so callers do not
 * need to pass redundant property objects.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLLMProvider implements OrchestrationLLM {

    /**
     * Cached {@link DocumentBuilderFactory} with XXE-protective features
     * pre-applied. ServiceLoader resolution and feature setup are one-time
     * costs, so the factory is shared; the per-call
     * {@link DocumentBuilderFactory#newDocumentBuilder() DocumentBuilder}
     * is still allocated on each parseSteps() invocation because
     * DocumentBuilder is not thread-safe.
     */
    private static final DocumentBuilderFactory DOC_BUILDER_FACTORY = createSafeDocumentBuilderFactory();

    static final String PLAN_SYS_PROMPT = """
            You are an orchestration planner. Given a session context (session id, \
            pending tasks, and current state), break the work into a sequence of \
            discrete execution steps. Each step must be a single concrete action — \
            decompose complex work until no step requires further breakdown.
            Output your plan as XML with a root <steps> element containing one \
            <step> element per action.
            Example:
            <steps>
              <step>query user profile</step>
              <step>validate permissions</step>
              <step>execute transfer</step>
            </steps>
            Output ONLY the XML, no other text.""";

    /** Safety-net timeout for the reactive {@code .block()} call. */
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);
    private static DocumentBuilderFactory createSafeDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Disable external entities — LLM output is not trusted XML
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception e) {
            // Defensive: a missing feature on an exotic JDK should not
            // crash the whole plan-generation path; parseSteps() will
            // still catch and log any subsequent parse error.
        }
        return factory;
    }

    private final Model model;

    public String getModelName() {
        return model.getModelName();
    }

    @Override
    public OrchestrationPlan generatePlan(SessionContext context) {
        List<Msg> messages = buildMessages(context);
        GenerateOptions options = GenerateOptions.builder()
                .stream(false)
                .build();

        String body = model.stream(messages, Collections.emptyList(), options)
                .map(DefaultLLMProvider::extractText)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining())
                .block(BLOCK_TIMEOUT);

        return new OrchestrationPlan(parseSteps(body));
    }

    private static List<Msg> buildMessages(SessionContext context) {
        return List.of(
                Msg.builder()
                        .role(MsgRole.SYSTEM)
                        .content(TextBlock.builder().text(PLAN_SYS_PROMPT).build())
                        .build(),
                Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder()
                                .text(context.toContextString())
                                .build())
                        .build());
    }

    private static String extractText(ChatResponse resp) {
        if (resp == null || resp.getContent() == null) {
            return "";
        }
        return resp.getContent().stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    /**
     * Parses the model response as XML {@code <steps>} containing
     * {@code <step>} elements. Returns an empty list on parse failure
     * or when no steps are found.
     * Visible for testing.
     */
    static List<String> parseSteps(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        try {
            DocumentBuilder builder = DOC_BUILDER_FACTORY.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(body)));
            NodeList stepNodes = doc.getElementsByTagName("step");
            List<String> steps = new ArrayList<>(stepNodes.getLength());
            for (int i = 0; i < stepNodes.getLength(); i++) {
                String text = stepNodes.item(i).getTextContent();
                if (text != null) {
                    String trimmed = text.trim();
                    if (!trimmed.isEmpty()) {
                        steps.add(trimmed);
                    }
                }
            }
            return Collections.unmodifiableList(steps);
        } catch (Exception e) {
            log.warn("Failed to parse LLM XML response: {}", body, e);
            return List.of();
        }
    }
}
