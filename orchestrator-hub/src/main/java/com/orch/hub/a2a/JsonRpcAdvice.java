package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcError;
import com.orch.hub.a2a.rpc.JsonRpcRequest;
import com.orch.hub.a2a.rpc.JsonRpcResponse;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONParseError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;

@Slf4j
@RestControllerAdvice("com.orch.hub.a2a")
public class JsonRpcAdvice implements RequestBodyAdvice {

    public static final String ATTR_ID = "orch.jsonrpc.id";

    // ---- RequestBodyAdvice ----

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return JsonRpcRequest.class.equals(methodParameter.getParameterType());
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (body instanceof JsonRpcRequest<?> req && req.id() != null) {
            var httpRequest = currentServletRequest();
            if (httpRequest != null) {
                httpRequest.setAttribute(ATTR_ID, req.id());
                log.debug("Cached JSON-RPC request id={} for method={}", req.id(), req.method());
            }
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                   Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private static HttpServletRequest currentServletRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    // ---- Exception handlers ----
    // Codes/messages reference io.a2a.spec.*Error classes

    /** @see io.a2a.spec.JSONParseError */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonRpcResponse<Void> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("JSON-RPC parse error: {}", ex.getMessage());
        return JsonRpcResponse.error("2.0", null, JsonRpcError.of(JSONParseError.DEFAULT_CODE, "Invalid JSON payload", null));
    }

    /** @see io.a2a.spec.InvalidRequestError */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public JsonRpcResponse<Void> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("Unsupported media type: {} for A2A endpoint", ex.getContentType());
        return JsonRpcResponse.error("2.0", null, JsonRpcError.of(InvalidRequestError.DEFAULT_CODE, "Request payload validation error", null));
    }

    /** @see io.a2a.spec.InvalidParamsError */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public JsonRpcResponse<Void> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Method argument type mismatch: parameter={}, value={}", ex.getName(), ex.getValue());
        var id = extractCachedId(request);
        return JsonRpcResponse.error("2.0", id, JsonRpcError.of(InvalidParamsError.DEFAULT_CODE, "Invalid parameters", null));
    }

    /** @see io.a2a.spec.InternalError */
    @ExceptionHandler(Exception.class)
    public JsonRpcResponse<Void> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception in A2A endpoint", ex);
        var id = extractCachedId(request);
        return JsonRpcResponse.error("2.0", id, JsonRpcError.of(InternalError.DEFAULT_CODE, "Internal Error", null));
    }

    private static String extractCachedId(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            var id = swr.getRequest().getAttribute(ATTR_ID);
            return id instanceof String s ? s : null;
        }
        return null;
    }
}
