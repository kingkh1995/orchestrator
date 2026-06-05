package com.orch.hub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * LLM provider configuration.
 *
 * <p>All values come from {@code application.yml} (or environment/nested
 * sources). No Java-level defaults — missing required fields fail fast
 * during Spring Boot's property binding or at bean construction time.
 */
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
