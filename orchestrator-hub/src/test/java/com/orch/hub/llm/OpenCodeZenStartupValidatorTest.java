package com.orch.hub.llm;

import com.orch.hub.config.OrchLLMProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OpenCodeZenStartupValidator}.
 */
class OpenCodeZenStartupValidatorTest {

    @Test
    void shouldNotProbeWhenApiKeyMissing() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey(null);
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertFalse(reachable, "Probe should return false when API key is missing");
        verifyNoInteractions(sender);
    }

    @Test
    void shouldProbeOpenCodeZenEndpointWhenApiKeyPresent() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash-free");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "Probe should return true on 2xx response");
        verify(sender).send(any(), any());
    }

    @Test
    void shouldTreat401AsReachable() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("bad-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(401);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "401 means endpoint is up, key is invalid — still reachable");
    }

    @Test
    void shouldTreat403AsReachable() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(403);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "403 means endpoint is up, key is forbidden — still reachable");
    }

    @Test
    void shouldTreat5xxAsNotReachable() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(503);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertFalse(reachable, "5xx means endpoint is down");
    }

    @Test
    void shouldTreat404AsNotReachable() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        boolean reachable = validator.probe();

        assertFalse(reachable, "404 means path is wrong — endpoint not at expected URI");
    }

    @Test
    void shouldNotThrowOnProbeFailure() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        when(sender.send(any(), any())).thenThrow(new RuntimeException("network down"));

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        assertDoesNotThrow(validator::probe,
                "Probe must not throw — startup must not fail-fast on network errors");
    }

    @Test
    void shouldRestoreInterruptFlagOnInterruptedProbe() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        when(sender.send(any(), any())).thenThrow(new InterruptedException("interrupted"));

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        // Clear any inherited interrupt state
        Thread.interrupted();
        boolean reachable = validator.probe();

        assertFalse(reachable, "Probe should return false on interrupt");
        assertTrue(Thread.currentThread().isInterrupted(),
                "Interrupt flag must be restored after InterruptedException");
        // Clear the flag we just set so we don't poison the test JVM
        Thread.interrupted();
    }

    @Test
    void shouldProbeTheConfiguredOpenCodeZenEndpoint() throws Exception {
        OrchLLMProperties props = new OrchLLMProperties();
        props.setApiKey("test-key");
        OpenCodeZenStartupValidator.HttpSender sender = mock(OpenCodeZenStartupValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(401);
        when(sender.send(any(), any())).thenReturn(response);

        OpenCodeZenStartupValidator validator = new OpenCodeZenStartupValidator(props, sender);
        validator.probe();

        org.mockito.ArgumentCaptor<HttpRequest> captor =
                org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(sender).send(captor.capture(), any());
        URI uri = captor.getValue().uri();
        assertTrue(uri.toString().contains("opencode.ai"),
                "Probe should hit the OpenCode Zen endpoint, got: " + uri);
    }
}
