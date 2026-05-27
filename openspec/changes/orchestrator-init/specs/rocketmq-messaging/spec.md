## ADDED Requirements

### Requirement: RocketMQ-A2A dependencies are declared
The system SHALL declare dependencies on `rocketmq-a2a` 1.0.9 and `rocketmq-client-java` 5.2.0 in the Gradle build configuration.

#### Scenario: Dependencies resolve
- **WHEN** Gradle resolves dependencies
- **THEN** `org.apache.rocketmq:rocketmq-a2a:1.0.9` and `rocketmq-client-java:5.2.0` MUST be present in the classpath

### Requirement: LiteTopic session event stream concept is defined
The system SHALL define the session event stream abstraction where each `sessionId` maps to a unique LiteTopic for event persistence.

#### Scenario: Session topic naming
- **WHEN** a new session is created with id `sess-123`
- **THEN** the system identifies the session event stream topic as `sess-123` (or a prefixed variant)

### Requirement: Functional topic routing is defined
The system SHALL define functional topic names for agent capability routing (e.g., `hub/agents/weather`, `hub/agents/hotel`).

#### Scenario: Topic naming convention
- **WHEN** an agent registers with capability `weather.query`
- **THEN** the system maps it to functional topic `hub/agents/weather`

### Requirement: RocketMQ configuration is externalized
The system SHALL externalize RocketMQ connection settings (endpoint, namespace, access key, secret key) via `application.yml` and environment variables.

#### Scenario: Configuration loading
- **WHEN** the application starts
- **THEN** it reads RocketMQ configuration from properties prefixed with `orch.messaging.rocketmq`
