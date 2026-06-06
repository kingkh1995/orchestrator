package com.orch.hub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * LLM provider configuration.
 *
 * <p>All values come from {@code application.yml} (or environment/nested
 * sources). No Java-level defaults — missing required fields fail fast
 * during Spring Boot's property binding or at bean construction time.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "orch.llm")
public class OrchLLMProperties {

    /** Provider name (informational — not used for dispatch). */
    private String provider;

    private String model;
    private String apiKey;

    /** Base URL of the LLM API (e.g. "https://opencode.ai/zen/v1"). */
    private String baseUrl;

    /** API endpoint path (e.g. "/chat/completions"). */
    private String endpointPath;

    /** Request timeout. When null, agentscope's ExecutionConfig default applies. */
    private Duration timeout;

    /** Maximum retry count. null = use agentscope default; 0 means no retries. */
    private Integer maxRetries;
}
