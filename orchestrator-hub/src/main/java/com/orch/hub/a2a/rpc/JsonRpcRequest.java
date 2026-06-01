package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON-RPC 2.0 request envelope.
 *
 * @param <T> the type of the params field
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcRequest<T>(
        String jsonrpc,
        String id,
        String method,
        T params
) {
}
