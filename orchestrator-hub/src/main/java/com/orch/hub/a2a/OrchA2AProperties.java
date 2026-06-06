package com.orch.hub.a2a;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A2A protocol agent identity.
 *
 * <p>Values come from {@code application.yml} (or environment/nested
 * sources). No Java-level defaults except for safe fallbacks (boolean
 * capabilities default to {@code false}; mode lists default to empty).
 * Required string fields surface as binding errors during Spring Boot's
 * property binding when omitted.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "orch.a2a")
public class OrchA2AProperties {

    /** Agent name surfaced in the A2A {@code AgentCard}. */
    private String agentName;

    /** Human-readable description surfaced in the A2A {@code AgentCard}. */
    private String description;

    /** Agent version surfaced in the A2A {@code AgentCard}. */
    private String version;

    /** Public URL where external clients can reach the agent. */
    private String publicUrl;

    @NestedConfigurationProperty
    private Provider provider = new Provider();

    @NestedConfigurationProperty
    private Capabilities capabilities = new Capabilities();

    /** Default input content modes (e.g. {@code "text"}, {@code "data"}). */
    private List<String> defaultInputModes = new ArrayList<>();

    /** Default output content modes (e.g. {@code "text"}, {@code "data"}). */
    private List<String> defaultOutputModes = new ArrayList<>();

    /** Optional URL pointing to the agent's documentation. */
    private String documentationUrl;

    /** Provider identity surfaced in the A2A {@code AgentCard}. */
    @Getter
    @Setter
    public static class Provider {
        private String organization;
        private String url;
    }

    /** A2A protocol capabilities surfaced in the {@code AgentCard}. */
    @Getter
    @Setter
    public static class Capabilities {
        private boolean streaming;
        private boolean pushNotifications;
        private boolean stateTransitionHistory;
    }
}
