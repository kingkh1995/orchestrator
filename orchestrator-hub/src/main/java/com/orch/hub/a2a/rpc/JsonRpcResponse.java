package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON-RPC 2.0 response envelope.
 *
 * @param <T> the type of the result field
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse<T>(
        String jsonrpc,
        String id,
        T result,
        JsonRpcError error
) {
}
