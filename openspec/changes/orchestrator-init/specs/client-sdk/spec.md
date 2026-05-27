## ADDED Requirements

> **Note**: SDK implementation is deferred to post-MVP (Task 9.x, Decision 13). These requirements describe the intended SDK design but are NOT in scope for MVP.

### Requirement: Client SDK module structure is established
The system SHALL create a separate Gradle module `orchestrator-sdk` with package `com.orch.sdk`. (deferred to post-MVP)

#### Scenario: Module compilation
- **WHEN** Gradle builds the project (post-MVP)
- **THEN** the `orchestrator-sdk` module produces a JAR artifact

### Requirement: SDK defines A2A agent annotation
The system SHALL define a `@A2AAgent` annotation for declarative agent registration.

#### Scenario: Annotation usage
- **WHEN** a developer annotates a class with `@A2AAgent(name="weather-agent", topic="hub/agents/weather")`
- **THEN** the SDK registers that class as an A2A-capable agent

### Requirement: SDK provides task handler abstraction
The system SHALL define a `TaskHandler` annotation or interface for marking methods that process incoming tasks.

#### Scenario: Handler registration
- **WHEN** a method is annotated with `@TaskHandler`
- **THEN** the SDK invokes that method when a matching task arrives on the agent's topic

### Requirement: SDK depends on A2A protocol and messaging
The `orchestrator-client-sdk` module SHALL declare dependencies on `a2a-java-sdk` and `rocketmq-a2a`.

#### Scenario: SDK dependency resolution
- **WHEN** Gradle resolves SDK dependencies
- **THEN** `a2a-java-sdk-client:0.3.3.Final` and `rocketmq-a2a:1.0.9` MUST be on the SDK classpath
