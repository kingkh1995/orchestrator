package com.orch.hub.a2a;

import io.a2a.spec.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: Verify that A2A SDK entity classes are accessible and work correctly.
 * AgentCard, AgentSkill, AgentCapabilities, AgentProvider are Java Records (accessor: name(), id(), etc.).
 * Task is a class with getters (getId(), getStatus()).
 * Artifact is a Record (accessor: name()).
 * TaskStatus wraps TaskState enum: new TaskStatus(TaskState.COMPLETED).
 */
class A2AEntityTest {

    @Test
    void shouldCreateAgentCardUsingSdkTypes() {
        var skill = new AgentSkill.Builder()
                .id("weather-agent")
                .name("Weather Agent")
                .description("Provides weather information")
                .tags(List.of("weather", "forecast"))
                .build();

        AgentCard card = new AgentCard.Builder()
                .name("OrchestratorHub")
                .description("A2A Multi-Agent Orchestration Hub")
                .url("http://localhost:8080")
                .provider(new AgentProvider("Orch", "https://github.com/orch"))
                .version("0.0.1")
                .skills(List.of(skill))
                .capabilities(new AgentCapabilities(false, false, false, null))
                .defaultInputModes(List.of())
                .defaultOutputModes(List.of())
                .build();

        assertEquals("OrchestratorHub", card.name());
        assertEquals("A2A Multi-Agent Orchestration Hub", card.description());
        assertEquals("http://localhost:8080", card.url());
        assertEquals(1, card.skills().size());
        assertEquals("weather-agent", card.skills().get(0).id());
    }

    @Test
    void shouldCreateTaskWithStatusCompleted() {
        var artifact = new Artifact.Builder()
                .artifactId("art-1")
                .name("result.txt")
                .parts(new TextPart("Hello from A2A"))
                .build();

        Task task = new Task.Builder()
                .id("task-1")
                .contextId("ctx-1")
                .status(new TaskStatus(TaskState.COMPLETED))
                .artifacts(List.of(artifact))
                .build();

        assertEquals("task-1", task.getId());
        assertEquals(TaskState.COMPLETED, task.getStatus().state());
        assertEquals(1, task.getArtifacts().size());
        assertEquals("result.txt", task.getArtifacts().get(0).name());
    }

    @Test
    void shouldCreateTaskWithStatusFailed() {
        Task task = new Task.Builder()
                .id("task-2")
                .contextId("ctx-2")
                .status(new TaskStatus(TaskState.FAILED))
                .build();

        assertEquals("task-2", task.getId());
        assertEquals(TaskState.FAILED, task.getStatus().state());
        assertTrue(task.getArtifacts().isEmpty());
    }

    @Test
    void shouldCreateMessageWithTextContent() {
        Message message = new Message.Builder()
                .role(Message.Role.AGENT)
                .parts(List.of(new TextPart("Processing your request")))
                .build();

        assertEquals(Message.Role.AGENT, message.getRole());
        assertEquals(1, message.getParts().size());
        assertInstanceOf(TextPart.class, message.getParts().get(0));
        assertEquals("Processing your request", ((TextPart) message.getParts().get(0)).getText());
    }

    @Test
    void shouldSerializeTaskWithJackson() throws Exception {
        var artifact = new Artifact.Builder()
                .artifactId("art-2")
                .name("output.json")
                .parts(new TextPart("{\"result\":\"ok\"}"))
                .build();

        Task original = new Task.Builder()
                .id("task-3")
                .contextId("ctx-3")
                .status(new TaskStatus(TaskState.COMPLETED))
                .artifacts(List.of(artifact))
                .build();

        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String json = mapper.writeValueAsString(original);
        Task roundTripped = mapper.readValue(json, Task.class);

        assertEquals("task-3", roundTripped.getId());
        assertEquals(TaskState.COMPLETED, roundTripped.getStatus().state());
        assertEquals(1, roundTripped.getArtifacts().size());
        assertEquals("output.json", roundTripped.getArtifacts().get(0).name());
    }
}
