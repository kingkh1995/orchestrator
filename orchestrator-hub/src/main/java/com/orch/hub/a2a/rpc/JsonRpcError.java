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
 * JSON-RPC 2.0 error object.
 */
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonRpcError {

    @JsonProperty("code")
    private final int code;

    @NonNull
    @JsonProperty("message")
    private final String message;

    @Nullable
    @JsonProperty("data")
    private final Object data;

    /**
     * Creates a JSON-RPC 2.0 error.
     *
     * @param code    error code integer
     * @param message error message string
     * @param data    optional error data (may be null)
     */
    @JsonCreator
    public static JsonRpcError of(
            @JsonProperty("code") int code,
            @JsonProperty("message") @NonNull String message,
            @JsonProperty("data") @Nullable Object data) {
        Objects.requireNonNull(message, "message");
        return new JsonRpcError(code, message, data);
    }
}
