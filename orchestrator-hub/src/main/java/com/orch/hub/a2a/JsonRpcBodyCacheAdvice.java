package com.orch.hub.a2a;

import com.orch.hub.a2a.rpc.JsonRpcRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;

/**
 * Caches the JSON-RPC request id in a request attribute so the
 * {@link GlobalExceptionHandler} can echo it back in error responses.
 *
 * <p>Per JSON-RPC 2.0 spec, the id MUST be the same as the request's id
 * when one is available. This advice captures it as soon as the body is
 * successfully deserialized, before any controller logic runs.</p>
 */
@RestControllerAdvice
public class JsonRpcBodyCacheAdvice implements RequestBodyAdvice {

    public static final String ATTR_ID = "orch.jsonrpc.id";

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return JsonRpcRequest.class.isAssignableFrom(methodParameter.getParameterType());
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
            HttpServletRequest httpRequest = currentServletRequest();
            if (httpRequest != null) {
                httpRequest.setAttribute(ATTR_ID, req.id());
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
}
