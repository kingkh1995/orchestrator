package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * JSON-RPC 2.0 response envelope.
 *
 * <p>Use {@link #result} for successful responses and {@link #error} for failure responses.
 * Per the JSON-RPC 2.0 spec, a response must contain exactly one of {@code result} or
 * {@code error}. The two factory methods enforce this at the type level.
 *
 * @param <T> the type of the result field
 */
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonRpcResponse<T> {

    @NonNull
    @JsonProperty("jsonrpc")
    private final String jsonrpc;

    @Nullable
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("id")
    private final String id;

    @Nullable
    @JsonProperty("result")
    private final T result;

    @Nullable
    @JsonProperty("error")
    private final JsonRpcError error;

    /**
     * Creates a successful JSON-RPC 2.0 response.
     *
     * @param jsonrpc protocol version, always "2.0"
     * @param id      request identifier echoed from the request
     * @param result  the method-specific result value
     */
    public static <T> JsonRpcResponse<T> result(
            @NonNull String jsonrpc,
            @NonNull String id,
            @NonNull T result) {
        Objects.requireNonNull(jsonrpc, "jsonrpc");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(result, "result");
        return new JsonRpcResponse<>(jsonrpc, id, result, null);
    }

    /**
     * Creates a JSON-RPC 2.0 error response.
     *
     * @param jsonrpc protocol version, always "2.0"
     * @param id      request identifier echoed from the request; may be null only for
     *                parse errors and invalid-request errors raised before the body is parsed
     *                (per JSON-RPC 2.0 spec section 5.1)
     * @param error   the error object
     */
    public static <T> JsonRpcResponse<T> error(
            @NonNull String jsonrpc,
            @Nullable String id,
            @NonNull JsonRpcError error) {
        Objects.requireNonNull(jsonrpc, "jsonrpc");
        Objects.requireNonNull(error, "error");
        return new JsonRpcResponse<>(jsonrpc, id, null, error);
    }

    /**
     * Jackson deserialization constructor. Prefer {@link #result} or {@link #error} for
     * runtime construction.
     */
    @JsonCreator
    static <T> JsonRpcResponse<T> of(
            @JsonProperty("jsonrpc") @NonNull String jsonrpc,
            @JsonProperty("id") @Nullable String id,
            @JsonProperty("result") @Nullable T result,
            @JsonProperty("error") @Nullable JsonRpcError error) {
        return new JsonRpcResponse<>(jsonrpc, id, result, error);
    }
}
