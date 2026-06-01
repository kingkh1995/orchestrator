package com.orch.hub.a2a.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcDtoTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    // --- JsonRpcRequest tests ---

    @Test
    void shouldCreateJsonRpcRequestWithAllFields() {
        var request = JsonRpcRequest.of("2.0", "req-1", "tasks/send", "hello");

        assertEquals("2.0", request.jsonrpc());
        assertEquals("req-1", request.id());
        assertEquals("tasks/send", request.method());
        assertEquals("hello", request.params());
    }

    @Test
    void shouldSerializeJsonRpcRequestToJson() throws Exception {
        var request = JsonRpcRequest.of("2.0", "1", "tasks/send", "test-params");
        var json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":\"1\""));
        assertTrue(json.contains("\"method\":\"tasks/send\""));
        assertTrue(json.contains("\"params\":\"test-params\""));
    }

    @Test
    void shouldDeserializeJsonRpcRequestFromJson() throws Exception {
        var json = """
                {"jsonrpc":"2.0","id":"2","method":"tasks/send","params":"hello"}
                """;
        @SuppressWarnings("unchecked")
        var request = mapper.readValue(json, JsonRpcRequest.class);

        assertEquals("2.0", request.jsonrpc());
        assertEquals("2", request.id());
        assertEquals("tasks/send", request.method());
        assertEquals("hello", request.params());
    }

    @Test
    void shouldHandleNullParamsInJsonRpcRequest() {
        var request = JsonRpcRequest.of("2.0", "3", "tasks/cancel", null);

        assertEquals("2.0", request.jsonrpc());
        assertEquals("3", request.id());
        assertEquals("tasks/cancel", request.method());
        assertNull(request.params());
    }

    // --- JsonRpcResponse tests ---

    @Test
    void shouldCreateJsonRpcResponseWithResult() {
        var response = JsonRpcResponse.result("2.0", "res-1", "success");

        assertEquals("2.0", response.jsonrpc());
        assertEquals("res-1", response.id());
        assertEquals("success", response.result());
        assertNull(response.error());
    }

    @Test
    void shouldCreateJsonRpcResponseWithError() {
        var error = JsonRpcError.of(-32700, "Parse error", null);
        var response = JsonRpcResponse.error("2.0", "res-2", error);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("res-2", response.id());
        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32700, response.error().code());
        assertEquals("Parse error", response.error().message());
    }

    @Test
    void shouldSerializeJsonRpcResponseWithResult() throws Exception {
        var response = JsonRpcResponse.result("2.0", "1", "done");
        var json = mapper.writeValueAsString(response);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":\"1\""));
        assertTrue(json.contains("\"result\":\"done\""));
        assertFalse(json.contains("\"error\""));
    }

    @Test
    void shouldSerializeJsonRpcResponseWithError() throws Exception {
        var error = JsonRpcError.of(-32601, "Method not found", null);
        var response = JsonRpcResponse.error("2.0", "2", error);
        var json = mapper.writeValueAsString(response);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":\"2\""));
        assertTrue(json.contains("\"code\":-32601"));
        assertTrue(json.contains("\"message\":\"Method not found\""));
    }

    @Test
    void shouldDeserializeJsonRpcResponseWithResult() throws Exception {
        var json = """
                {"jsonrpc":"2.0","id":"3","result":"completed","error":null}
                """;
        @SuppressWarnings("unchecked")
        var response = mapper.readValue(json, JsonRpcResponse.class);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("3", response.id());
        assertEquals("completed", response.result());
        assertNull(response.error());
    }

    @Test
    void shouldDeserializeJsonRpcResponseWithError() throws Exception {
        var json = """
                {"jsonrpc":"2.0","id":"4","result":null,"error":{"code":-32000,"message":"Server error","data":null}}
                """;
        @SuppressWarnings("unchecked")
        var response = mapper.readValue(json, JsonRpcResponse.class);

        assertEquals("2.0", response.jsonrpc());
        assertEquals("4", response.id());
        assertNull(response.result());
        assertNotNull(response.error());
        assertEquals(-32000, response.error().code());
        assertEquals("Server error", response.error().message());
    }

    // --- JsonRpcError tests ---

    @Test
    void shouldCreateJsonRpcErrorWithAllFields() {
        var error = JsonRpcError.of(-32601, "Method not found", Map.of("method", "unknown"));

        assertEquals(-32601, error.code());
        assertEquals("Method not found", error.message());
        assertNotNull(error.data());
    }

    @Test
    void shouldSerializeJsonRpcError() throws Exception {
        var error = JsonRpcError.of(-32700, "Parse error", null);
        var json = mapper.writeValueAsString(error);

        assertTrue(json.contains("\"code\":-32700"));
        assertTrue(json.contains("\"message\":\"Parse error\""));
    }
}
