## Why

The Orchestrator project currently exists only as a comprehensive design document (README.md) with zero source code, build files, or active OpenSpec changes. To transform this architecture blueprint into a working A2A-compliant multi-agent orchestration hub, we need to bootstrap the project with a compilable foundation, a clear technical design, and an actionable task breakdown. This change establishes the minimal viable project structure that subsequent features can build upon.

## What Changes

- **Bootstrap Gradle multi-module project**: Create `orchestrator-parent` with Gradle 9.5.1 (Groovy DSL) and Gradle Wrapper, using Tencent mirror for distribution and Aliyun Maven Central for dependencies
- **Initialize Hub Server module**: Spring Boot 4.0.6 + JDK 25 (compiled to Java 17 bytecode compatibility)
- **Introduce core dependencies**: rocketmq-a2a 1.0.9, a2a-java-sdk 0.3.3.Final, agentscope-java 1.0.12
- **Define A2A protocol endpoints**: `AgentCard` (GET `/.well-known/agent.json`) and `tasks/send` (POST `/tasks/send`) returning A2A-compliant `Task` with `COMPLETED` status, driven by the `ReActOrchestrationEngine`
- **Configure Opencode Zen LLM**: DeepSeek V4 Flash Free via `OpenAIChatModel` (baseUrl: `https://opencode.ai/zen/v1/chat/completions`)
- **Establish package structure**: Root package `com.orch` with `com.orch.hub.*` namespace (a2a, config, llm, messaging, orchestration, session)
- **Add integration test**: Verify Spring Boot application starts and responds to AgentCard request
- **Add JSON-RPC + A2A spec coverage**: 81-test suite covers `Task`/`Message`/`Artifact` serialization, controller error paths, AgentCard endpoint, and `tasks/send` request/response shape
- **Create OpenSpec specs**: Define capability specs for `a2a-protocol`, `react-orchestration`, `rocketmq-messaging`

## Capabilities

### New Capabilities
- `a2a-protocol`: A2A protocol compliance — AgentCard, Task lifecycle, Message, Artifact entities and REST endpoints
- `react-orchestration`: ReAct-style loop orchestration — `OrchestrationEngine` interface backed by AgentScope-Java's `ReActAgent` (single implementation in MVP)
- `rocketmq-messaging`: RocketMQ-A2A integration — `RocketMQTransportConfig` bean + `RocketMQClientFactory` outbound channel + Docker Compose Local-mode deployment

### Modified Capabilities
<!-- No existing capabilities to modify -->

## Impact

- **New modules**: `orchestrator-hub` (single module for MVP, sub-modules deferred post-MVP)
- **Build system**: Gradle Groovy DSL replaces the current zero-build state
- **Runtime**: JDK 25 required; produces Java 17-compatible bytecode
- **External dependencies**: Apache RocketMQ 5.5.0 (Local mode: broker + in-process gRPC proxy + dashboard) as the A2A message transport, Opencode Zen API (for LLM)
- **API surface**: New HTTP endpoints `/.well-known/agent.json` and `/tasks/send`