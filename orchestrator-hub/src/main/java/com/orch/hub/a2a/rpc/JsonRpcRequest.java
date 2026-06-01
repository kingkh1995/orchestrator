package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * JSON-RPC 2.0 request envelope.
 *
 * @param <T> the type of the params field
 */
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonRpcRequest<T> {

    @NonNull
    @JsonProperty("jsonrpc")
    private final String jsonrpc;

    @NonNull
    @JsonProperty("id")
    private final String id;

    @NonNull
    @JsonProperty("method")
    private final String method;

    @Nullable
    @JsonProperty("params")
    private final T params;

    /**
     * Creates a new JSON-RPC 2.0 request.
     *
     * @param jsonrpc protocol version, always "2.0"
     * @param id      request identifier
     * @param method  RPC method name
     * @param params  method-specific parameters (may be null for methods without params)
     */
    @JsonCreator
    public static <T> JsonRpcRequest<T> of(
            @JsonProperty("jsonrpc") @NonNull String jsonrpc,
            @JsonProperty("id") @NonNull String id,
            @JsonProperty("method") @NonNull String method,
            @JsonProperty("params") @Nullable T params) {
        Objects.requireNonNull(jsonrpc, "jsonrpc");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(method, "method");
        return new JsonRpcRequest<>(jsonrpc, id, method, params);
    }
}
