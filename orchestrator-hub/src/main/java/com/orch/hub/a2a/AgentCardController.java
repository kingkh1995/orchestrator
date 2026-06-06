package com.orch.hub.a2a;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** A2A agent card discovery endpoint. */
@RestController
@RequiredArgsConstructor
public class AgentCardController {

    private final OrchA2AProperties properties;

    @PostConstruct
    void validateRequiredFields() {
        Assert.hasText(properties.getAgentName(), "orch.a2a.agent-name must be set");
        Assert.hasText(properties.getDescription(), "orch.a2a.description must be set");
        Assert.hasText(properties.getVersion(), "orch.a2a.version must be set");
        Assert.hasText(properties.getPublicUrl(), "orch.a2a.public-url must be set");
    }

    @GetMapping("/.well-known/agent.json")
    public AgentCard getAgentCard() {
        var caps = properties.getCapabilities();
        return new AgentCard.Builder()
                .name(properties.getAgentName())
                .description(properties.getDescription())
                .url(properties.getPublicUrl())
                .version(properties.getVersion())
                .provider(new AgentProvider(
                        properties.getProvider().getOrganization(),
                        properties.getProvider().getUrl()))
                .capabilities(new AgentCapabilities(
                        caps.isStreaming(),
                        caps.isPushNotifications(),
                        caps.isStateTransitionHistory(),
                        null))
                .defaultInputModes(properties.getDefaultInputModes())
                .defaultOutputModes(properties.getDefaultOutputModes())
                .documentationUrl(properties.getDocumentationUrl())
                .skills(List.of())
                .build();
    }
}
