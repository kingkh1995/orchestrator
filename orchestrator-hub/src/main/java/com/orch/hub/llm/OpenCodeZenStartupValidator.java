package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Startup-time reachability probe for the OpenCode Zen endpoint.
 *
 * <p>Runs once after bean construction. A missing API key is logged as an
 * error; a network probe is attempted when the key is present. The probe
 * never throws — startup must not fail-fast on transient network errors
 * so the application remains operable in offline / dev contexts.
 *
 * <p>The probe-time {@link HttpClient} is a one-shot. It holds no
 * long-lived resources beyond internal daemon threads that die with
 * the JVM, so explicit shutdown is unnecessary on Java 17 (which does
 * not yet expose {@code HttpClient#close()}).
 */
@Slf4j
@Component
public class OpenCodeZenStartupValidator {

    private static final URI PROBE_URI = URI.create(
            LLMConfig.OPENCODE_ZEN_BASE_URL + LLMConfig.OPENCODE_ZEN_CHAT_PATH);

    private final OrchLLMProperties properties;
    private final HttpSender sender;

    @Autowired
    public OpenCodeZenStartupValidator(OrchLLMProperties properties) {
        this(properties, defaultSender());
    }

    OpenCodeZenStartupValidator(OrchLLMProperties properties, HttpSender sender) {
        this.properties = properties;
        this.sender = sender;
    }

    @PostConstruct
    public void validate() {
        boolean reachable = probe();
        if (reachable) {
            log.info("OpenCode Zen endpoint reachable: {}", PROBE_URI);
        } else {
            log.warn("OpenCode Zen endpoint NOT reachable — LLM calls will fail at runtime. URI={}",
                    PROBE_URI);
        }
    }

    boolean probe() {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OPENCODE_API_KEY is not set. LLM calls will fail at runtime.");
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(PROBE_URI)
                .timeout(Duration.ofSeconds(5))
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
            log.warn("OpenCode Zen probe returned status {} — endpoint not ready", status);
            return false;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("OpenCode Zen probe interrupted");
            return false;
        } catch (IOException | RuntimeException e) {
            log.warn("OpenCode Zen probe failed: {}", e.getMessage());
            return false;
        }
    }

    private static HttpSender defaultSender() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        return client::send;
    }

    /** SAM mirroring {@link HttpClient#send} so checked exceptions are first-class. */
    @FunctionalInterface
    interface HttpSender {
        HttpResponse<String> send(HttpRequest request, HttpResponse.BodyHandler<String> handler)
                throws IOException, InterruptedException;
    }
}
