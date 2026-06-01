package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcError;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonRpcResponse<Void> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        // Per JSON-RPC 2.0 spec: id MUST be Null for parse errors (id could not be read).
        return new JsonRpcResponse<>("2.0", null, null, new JsonRpcError(-32700, "Parse error", null));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public JsonRpcResponse<Void> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        // Per JSON-RPC 2.0 spec: id MUST be Null for invalid-request errors raised before the
        // body is parsed (e.g., wrong Content-Type).
        return new JsonRpcResponse<>("2.0", null, null, new JsonRpcError(-32600, "Invalid Request", null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public JsonRpcResponse<Void> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        // Per JSON-RPC 2.0 spec: id MUST be Null when params fail type binding.
        log.warn("Method argument type mismatch: parameter={}, value={}",
                ex.getName(), ex.getValue());
        return new JsonRpcResponse<>("2.0", null, null, new JsonRpcError(-32602, "Invalid params", null));
    }

    @ExceptionHandler(Exception.class)
    public JsonRpcResponse<Void> handleGenericException(
            Exception ex, WebRequest request) {
        // Generic catch-all: try to echo the cached id from the parsed JSON-RPC body
        // so the client can correlate the error with the original request.
        log.error("Unhandled exception in A2A endpoint", ex);
        String id = extractCachedId(request);
        return new JsonRpcResponse<>("2.0", id, null, new JsonRpcError(-32000, "Server error", null));
    }

    private static String extractCachedId(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            Object id = swr.getRequest().getAttribute(JsonRpcBodyCacheAdvice.ATTR_ID);
            return id instanceof String s ? s : null;
        }
        return null;
    }
}
