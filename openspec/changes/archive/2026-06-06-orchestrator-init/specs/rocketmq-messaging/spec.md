## ADDED Requirements

### Requirement: RocketMQ-A2A dependencies are declared
The system SHALL declare dependencies on `rocketmq-a2a` 1.0.9 and `rocketmq-client-java` 5.2.0 in the Gradle build configuration.

#### Scenario: Dependencies resolve
- **WHEN** Gradle resolves dependencies
- **THEN** `org.apache.rocketmq:rocketmq-a2a:1.0.9` and `rocketmq-client-java:5.2.0` MUST be present in the classpath

### Requirement: RocketMQ configuration is externalized
The system SHALL externalize RocketMQ connection settings (endpoint, namespace, access key, secret key, work-agent-response-topic, work-agent-response-group-id, agent-topic, agent-url, use-default-recover-mode) via `application.yml` and environment variables.

#### Scenario: Configuration loading
- **WHEN** the application starts
- **THEN** it reads RocketMQ configuration from properties prefixed with `orch.messaging.rocketmq`

### Requirement: RocketMQ class-loading is isolated
RocketMQ-A2A transport classes (`org.apache.rocketmq.*`) SHALL be isolated in the `com.orch.hub.messaging.rocketmq` sub-package and wired by an unconditional `@Configuration` (`RocketMQMessagingConfiguration`). The product is built ON RocketMQ-A2A — no `enabled` flag is exposed.

#### Scenario: Transport config bean is registered
- **WHEN** the Spring context initializes
- **THEN** a `RocketMQTransportConfig` bean MUST be registered, built from the 9 properties 1:1 with the library's `RocketMQTransportConfig.builder()` (no transformation or default override beyond `httpClient` + `liteTopic`)

### Requirement: Outbound A2A Client factory is available
The system SHALL provide a `RocketMQClientFactory` (`@Component`) that creates A2A `Client` instances using the RocketMQ transport.

#### Scenario: Factory is unconditionally present
- **WHEN** the Spring context initializes
- **THEN** a `RocketMQClientFactory` bean MUST be registered (no conditional)
- **THEN** the factory exposes `createClient(AgentCard card)` returning `Client.builder(card).withTransport(RocketMQTransport.class, config).build()`

### Requirement: TaskController is transport-agnostic
The `TaskController` (the public A2A entrypoint) SHALL NOT inject `RocketMQClientFactory` or any RocketMQ type. The controller calls `OrchestrationEngine.executePlan(SessionContext)` only.

#### Scenario: Controller surface
- **WHEN** the developer inspects `TaskController` source
- **THEN** it MUST NOT contain any `import org.apache.rocketmq.*` statement
- **THEN** it MUST NOT inject `RocketMQClientFactory`, `RocketMQTransportConfig`, or `RocketMQTransport`

### Requirement: Docker Compose runs RocketMQ in Local mode
The repository SHALL provide a `docker-compose.yml` that brings up RocketMQ 5.5.0 in Local mode (broker with in-process gRPC proxy) plus dashboard.

#### Scenario: Three containers come up
- **WHEN** `docker compose up -d` runs
- **THEN** `orch-rocketmq-namesrv` listens on host port 9876
- **THEN** `orch-rocketmq-broker` listens on host ports 10911 (Remoting) + 8081 (in-process gRPC proxy) with LiteTopic + RocksDB store
- **THEN** `orch-rocketmq-dashboard` serves the web UI on host port 9090

#### Scenario: Proxy config is bind-mounted
- **WHEN** the broker container starts
- **THEN** `broker.conf` and `rmq-proxy.json` are bind-mounted from `docker/rocketmq/`
- **THEN** `rmq-proxy.json` is passed to `mqbroker` via the `-pc` flag so the in-process proxy picks up `rocketMQClusterName` and `namesrvAddr` from the bind-mounted file
