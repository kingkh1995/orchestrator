package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON-RPC 2.0 error object.
 *
 * @param code    error code integer
 * @param message error message string
 * @param data    optional error data (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcError(
        int code,
        String message,
        Object data
) {
}
