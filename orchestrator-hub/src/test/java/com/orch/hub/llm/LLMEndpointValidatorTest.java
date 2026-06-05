package com.orch.hub.llm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link LLMEndpointValidator}.
 */
class LLMEndpointValidatorTest {

    private static final URI PROBE_URI = URI.create("https://opencode.ai/zen/v1/chat/completions");

    @Test
    void shouldNotProbeWhenApiKeyMissing() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, null, sender);

        boolean reachable = validator.probe();

        assertFalse(reachable, "Probe must fail when API key is missing");
        verifyNoInteractions(sender);
    }

    @Test
    void shouldProbeEndpointWhenApiKeyPresent() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(200).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "200 should be treated as reachable");
    }

    @Test
    void shouldTreat401AsReachable() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(401).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "401 should still count as endpoint reachable");
    }

    @Test
    void shouldTreat403AsReachable() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(403).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        boolean reachable = validator.probe();

        assertTrue(reachable, "403 should still count as endpoint reachable");
    }

    @Test
    void shouldTreat5xxAsNotReachable() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(502).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        boolean reachable = validator.probe();

        assertFalse(reachable, "5xx should be treated as not reachable");
    }

    @Test
    void shouldTreat404AsNotReachable() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(404).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        boolean reachable = validator.probe();

        assertFalse(reachable, "404 should be treated as not reachable");
    }

    @Test
    void shouldNotThrowOnProbeFailure() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        doThrow(new IOException("connection refused")).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);

        assertDoesNotThrow(validator::probe,
                "Probe must not throw on IOException");
    }

    @Test
    void shouldRestoreInterruptFlagOnInterruptedProbe() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        doThrow(new InterruptedException("interrupted")).when(sender).send(any(), any());

        LLMEndpointValidator validator = new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        Thread.interrupted();
        boolean reachable = validator.probe();

        assertFalse(reachable, "Probe should return false on interrupt");
        assertTrue(Thread.currentThread().isInterrupted(),
                "Interrupt flag must be restored after InterruptedException");
        Thread.interrupted();
    }

    @Test
    void shouldProbeTheConfiguredEndpoint() throws Exception {
        LLMEndpointValidator.HttpSender sender = mock(LLMEndpointValidator.HttpSender.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        doReturn(401).when(response).statusCode();
        doReturn(response).when(sender).send(any(), any());

        LLMEndpointValidator validator =
                new LLMEndpointValidator(PROBE_URI, "test-key", sender);
        validator.probe();

        org.mockito.ArgumentCaptor<HttpRequest> captor =
                org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(sender).send(captor.capture(), any());
        URI uri = captor.getValue().uri();
        assertTrue(uri.toString().contains("opencode.ai"),
                "Probe should hit the configured LLM endpoint, got: " + uri);
    }
}
