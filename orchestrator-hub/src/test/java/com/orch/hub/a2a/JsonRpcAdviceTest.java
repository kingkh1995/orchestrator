package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcAdviceTest {

    private final JsonRpcAdvice handler = new JsonRpcAdvice();

    @BeforeEach
    void setUpRequestContext() {
        // Bind a request so extractCachedId() can find the attribute
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDownRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldHandleHttpMessageNotReadableAsParseError() {
        var ex = new HttpMessageNotReadableException("Invalid JSON", mockHttpInputMessage());
        var response = handler.handleHttpMessageNotReadable(ex, webRequest());

        assertNotNull(response.error());
        assertEquals(-32700, response.error().code());
        assertEquals("Invalid JSON payload", response.error().message());
        // Parse errors cannot extract id from the body -> null per JSON-RPC spec
        assertNull(response.id());
    }

    @Test
    void shouldHandleHttpMediaTypeNotSupportedAsInvalidRequest() {
        var ex = new HttpMediaTypeNotSupportedException("text/plain not supported");
        var response = handler.handleHttpMediaTypeNotSupported(ex, webRequest());

        assertNotNull(response.error());
        assertEquals(-32600, response.error().code());
        assertEquals("Request payload validation error", response.error().message());
        // Content-Type rejection happens before body parsing -> null per spec
        assertNull(response.id());
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchAsInvalidParams() throws Exception {
        var method = SampleController.class.getMethod("handle", String.class);
        var param = new MethodParameter(method, 0);
        var ex = new MethodArgumentTypeMismatchException(
                "not-an-int", Integer.class, "value", param, new NumberFormatException("bad"));

        var response = handler.handleMethodArgumentTypeMismatch(ex, webRequest());

        assertNotNull(response.error());
        assertEquals(-32602, response.error().code());
        assertEquals("Invalid parameters", response.error().message());
        // Type binding failure happens before controller body executes -> null per spec
        assertNull(response.id());
    }

    @Test
    void shouldHandleGenericExceptionAsServerError() {
        var ex = new RuntimeException("Internal failure");
        var response = handler.handleGenericException(ex, webRequest());

        assertNotNull(response.error());
        assertEquals(-32603, response.error().code());
        assertEquals("Internal Error", response.error().message());
        // No cached id -> null
        assertNull(response.id());
    }

    @Test
    void shouldEchoCachedIdForGenericException() {
        // Simulate JsonRpcAdvice having cached the id
        var request = new MockHttpServletRequest();
        request.setAttribute(JsonRpcAdvice.ATTR_ID, "req-cached");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var ex = new RuntimeException("boom");
        var response = handler.handleGenericException(
                ex, new ServletWebRequest(request));

        assertEquals("req-cached", response.id(),
                "Generic exception handler should echo the cached request id");
    }

    @Test
    void shouldSetCorrectJsonRpcVersion() {
        var ex = new HttpMessageNotReadableException("bad json", mockHttpInputMessage());
        var response = handler.handleHttpMessageNotReadable(ex, webRequest());

        assertEquals("2.0", response.jsonrpc());
    }

    private static WebRequest webRequest() {
        return new ServletWebRequest(new MockHttpServletRequest());
    }

    private static HttpInputMessage mockHttpInputMessage() {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }

    /** Test fixture for building MethodParameter in the type-mismatch test. */
    @SuppressWarnings("unused")
    static class SampleController {
        public void handle(String value) {}
    }
}
