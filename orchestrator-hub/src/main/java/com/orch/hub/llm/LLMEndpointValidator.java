package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Startup-time reachability probe for the configured LLM endpoint.
 *
 * <p>Runs once after bean construction. A missing API key is logged as an
 * error; a network probe is attempted when the key is present. The probe
 * never throws — startup must not fail-fast on transient network errors
 * so the application remains operable in offline / dev contexts.
 *
 * <p>The probe-time {@link HttpClient} is a one-shot. It holds no
 * long-lived resources beyond internal daemon threads that die with
 * the JVM, so explicit shutdown is unnecessary on Java 17+ (which does
 * not yet expose {@code HttpClient#close()}).
 */
@Slf4j
@Component
public class LLMEndpointValidator {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final URI probeUri;
    private final String apiKey;
    private final HttpSender sender;

    @Autowired
    public LLMEndpointValidator(OrchLLMProperties properties) {
        this(buildProbeUri(properties.getBaseUrl(), properties.getEndpointPath()),
                properties.getApiKey(),
                defaultSender());
    }

    /** Test-only constructor with all dependencies injected directly. */
    LLMEndpointValidator(URI probeUri, String apiKey, HttpSender sender) {
        this.probeUri = probeUri;
        this.apiKey = apiKey;
        this.sender = sender;
    }

    private static URI buildProbeUri(String baseUrl, String endpointPath) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("orch.llm.base-url must be configured");
        }
        String path = endpointPath != null ? endpointPath : "";
        return URI.create(baseUrl + path);
    }
    @PostConstruct
    void validate() {
        boolean reachable = probe();
        if (reachable) {
            log.info("LLM endpoint reachable: {}", probeUri);
        } else {
            log.warn("LLM endpoint NOT reachable — LLM calls will fail at runtime. URI={}", probeUri);
        }
    }

    boolean probe() {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("LLM API key is not set. LLM calls will fail at runtime.");
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(probeUri)
                .timeout(TIMEOUT)
                .GET()
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try {
            HttpResponse<String> response =
                    sender.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            // 2xx = reachable, 401/403 = reachable but key invalid (still counts as endpoint up).
            // 5xx / 4xx-other / 404 = endpoint not ready.
            if (status >= 200 && status < 500 && status != 404) {
                return true;
            }
            log.warn("LLM endpoint probe returned status {} — endpoint not ready", status);
            return false;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("LLM endpoint probe interrupted");
            return false;
        } catch (IOException | RuntimeException e) {
            log.warn("LLM endpoint probe failed: {}", e.getMessage());
            return false;
        }
    }

    private static HttpSender defaultSender() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        return client::send;
    }

    /** SAM mirroring {@link HttpClient#send} so checked exceptions are first-class. */
    @FunctionalInterface
    interface HttpSender {
        <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler)
                throws IOException, InterruptedException;
    }
}
