## Context

The Orchestrator project is a design-only repository containing a 212-line Chinese README.md that describes an A2A-compliant multi-agent orchestration hub using RocketMQ-A2A and ReAct-style loop orchestration. The project has zero source code, zero build files, and no active OpenSpec changes. The goal is to bootstrap a working foundation that subsequent features can build upon.

Key constraints:
- JDK 25 is available in the environment but dependencies target Java 17 bytecode
- Gradle (not Maven) is preferred with Groovy DSL
- Tencent mirror for Gradle distribution, Aliyun for Maven repositories
- Spring Boot 4.0.6 as the web framework
- rocketmq-a2a 1.0.9 as the messaging transport (verified via GitHub releases)

## Goals / Non-Goals

**Goals:**
1. Create a compilable Gradle multi-module project structure (simplified for MVP: parent + hub only)
2. Implement mock A2A protocol endpoints (AgentCard + tasks/send)
3. Establish the package namespace `com.orch` with hub module
4. Configure all core dependencies with verified versions (verified via Task 0 gate)
5. Provide unit tests (config binding, serialization, JSON-RPC, controllers) and integration tests (startup, endpoints). Total: 81 tests across `orchestrator-hub` (all green)
6. Include local development environment (Docker Compose for RocketMQ 5.5.0 — namesrv + broker-with-proxy + dashboard)
7. No API documentation framework (JSON-RPC 2.0 + A2A spec are the contract; tests cover endpoint behavior)
8. Define LLM provider abstraction interface for provider portability

**Non-Goals:**
1. No A2A product-level test page in MVP (browser-driven manual QA only; CLI / integration tests cover the surface area)
2. No actual LLM invocation logic — `ReActOrchestrationEngine` returns the agent's text response, but no business-specific tool calls / multi-turn planning in MVP
3. No SDK module / sub-agent client library in MVP
4. No authentication, observability, or production hardening
5. No `InMemorySessionStore` / `SessionSnapshot` — out-of-process state is the broker's responsibility (RocketMQ offsets + sessionId as message key)

## Decisions

### Decision 1: Gradle Groovy DSL over Maven
**Rationale**: Gradle offers superior multi-module support, incremental builds, and Groovy DSL is more expressive for dependency management. The user explicitly requested Gradle with wrapper.

**Alternatives considered**: Maven (rejected — user preference for Gradle)

### Decision 2: Spring Boot 4.0.6 over 3.5.x
**Rationale**: Spring Boot 4.0.6 is the latest stable release (2026-04-23), natively supports Java 25, and provides modern features like API versioning and HTTP service clients. It builds on Spring Framework 7 with improved modularity.

**Alternatives considered**: Spring Boot 3.5.14 (rejected — 4.x is stable and preferred for greenfield projects)

### Decision 3: Java 17 bytecode compatibility with JDK 25 runtime
**Rationale**: Dependencies (rocketmq-a2a, AgentScope-Java) compile to Java 17. Setting sourceCompatibility/targetCompatibility to 17 ensures maximum ecosystem compatibility while running on JDK 25.

**Alternatives considered**: Java 21 or 25 bytecode (rejected — unnecessary and may cause runtime issues with dependencies)

### Decision 4: Package prefix `com.orch`
**Rationale**: Short, industry-standard abbreviation for "orchestrator" (used in Kubernetes/Docker communities). Clear and memorable.

**Alternatives considered**: `com.orchestrator` (too long), `com.orc` (confusing — "orc" means "ogre"), `com.orchs` (unnecessary plural)

### Decision 5: Simplified module structure for MVP
**Rationale**: Five hub sub-modules (hub-api, hub-core, hub-messaging, hub-bootstrap) each contain fewer than 5 classes in MVP. The Gradle overhead (configuration, dependency management, version alignment) exceeds the benefit. Consolidate into a single `orchestrator-hub` module for MVP, splitting later when class count justifies it.

**Alternatives considered**: Original 5-module hub split (rejected — premature optimization for MVP)

### Decision 6: Opencode Zen DeepSeek V4 Flash Free as LLM
**Rationale**: Free tier, OpenAI-compatible API (`/chat/completions`), well-documented. AgentScope-Java's `OpenAIChatModel` supports custom base URLs via `.baseUrl("...")`, making integration straightforward.

**Alternatives considered**: DeepSeek direct API (rejected — Opencode Zen provides unified gateway and free tier)

### Decision 7: Docker Compose for local RocketMQ (Local mode)
**Rationale**: The hub connects to RocketMQ in MVP via the `RocketMQClientFactory` outbound channel. The `apache/rocketmq:5.5.0` image runs namesrv + broker (with in-process gRPC proxy via `--enable-proxy`) + dashboard; the broker bind-mounts `broker.conf` and `rmq-proxy.json` so users can tweak config without rebuilding. Local mode is the supported deployment per the official RocketMQ 5.5 quick-start; cluster-mode proxy (separate container) has a client-5.2.0 stream-protocol incompatibility.

**Alternatives considered**: Cluster-mode proxy (rejected — `Stream is already completed` in `ClientImpl.fetchTopicRoute` against client 5.2.0); manual installation guide (rejected — Docker Compose is one-command setup)

### Decision 8: No API documentation framework (JSON-RPC 2.0 is the contract)
**Rationale**: A2A uses JSON-RPC 2.0, not REST. OpenAPI/Swagger cannot represent JSON-RPC semantics (method + params envelope, dynamic method dispatch). SpringDoc 2.x is incompatible with Spring Boot 4.x, and SpringDoc 3.x remains REST-oriented. The 81-test suite covers endpoint behavior, and the JSON-RPC 2.0 + A2A protocol specs are the contract.

**Alternatives considered**: SpringDoc OpenAPI (rejected — v2 incompatible with SB4, v3 still REST-oriented; JSON-RPC generates meaningless OpenAPI schemas), Spring REST Docs (rejected — adds Asciidoctor build complexity for zero protocol documentation benefit)

### Decision 9: Unified configuration namespace `orch.*`
**Rationale**: Review identified inconsistency from original proposal (mixed `orch.*`, `hub.*`, `a2a.*` prefixes). All application configuration follows `orch.*` to ensure discoverability and prevent namespace collision. RocketMQ has no `enabled` flag — the product is built unconditionally ON RocketMQ-A2A.

**Configuration layout**:
```yaml
orch:
  a2a:
    agent-name: "OrchestratorHub"
    description: "A2A Multi-Agent Orchestration Hub"
  messaging:
    rocketmq:
      endpoint: "localhost:8081"     # in-process proxy gRPC
      namespace: "orchestrator-dev"
  llm:
    model: "deepseek-v4-flash-free"
    api-key: "${OPENCODE_API_KEY}"
```
**Alternatives considered**: `hub.*` prefix (rejected — overlaps with messaging/LLM config), `a2a.*` (rejected — too narrow)

### Decision 10: LLM provider abstraction interface
**Rationale**: CEO review identified free-model vendor lock-in as a HIGH risk. Define an `OrchestrationLLM` interface; the `DefaultLLMProvider` implementation wraps AgentScope-Java's `OpenAIChatModel`. Provider swapping is a 50-line interface change.

**Alternatives considered**: Direct `OpenAIChatModel` usage (rejected — couples orchestration to a single provider), abstract factory (rejected — over-engineering for MVP)

### Decision 12: Dependency verification gate (Task 0)
**Rationale**: Spring Boot 4.0.6, Gradle 9.5.1, agentscope 1.0.12, and rocketmq-a2a 1.0.9 are all very recent releases. Their transitive compatibility is unverified. Task 0 validates `./gradlew dependencies` against a minimal build file and checks a2a-java-sdk entity Jackson serialization before any code is written, preventing late-discovery of broken dependencies.

**Hard gate**: All subsequent tasks are blocked until Task 0 passes. A `gradle.lockfile` is generated after the gate passes to freeze transitive versions, preventing silent dependency drift during implementation.

**Alternatives considered**: No gate (rejected — wasted work if deps don't resolve), downgrade to proven versions (rejected — user explicitly chose 4.0.6)

### Decision 13: RocketMQ class-loading isolation
**Rationale**: RocketMQ-A2A transport classes (`org.apache.rocketmq.*`) are isolated in `com.orch.hub.messaging.rocketmq` sub-package and wired by an unconditional `@Configuration` (`RocketMQMessagingConfiguration`). The product is built ON RocketMQ-A2A — there is no `enabled` toggle. Class-loading isolation keeps the dependency footprint cohesive and prevents the default Spring scan path from accidentally importing broker/proxy classes that would change class-loading semantics.

**Alternatives considered**: `@ConditionalOnProperty` toggle (rejected — adds a flag that always evaluates to `true` in production, hides the unconditional wiring intent); separate profile (rejected — adds operational complexity for no runtime benefit)

### Decision 14: JSON-RPC 2.0 envelope types as first-class DTOs
**Rationale**: A2A protocol is built on JSON-RPC 2.0. Every request/response must include `jsonrpc`, `id`, and either `result` or `error` fields. Without explicit `JsonRpcRequest<T>` and `JsonRpcResponse<T>` DTOs, the controller either (a) loses the JSON-RPC envelope (returns bare Task objects — protocol non-conformant) or (b) works with raw Maps (no type safety). Dedicated DTOs in `com.orch.hub.a2a.rpc` package ensure type safety, testability, and spec conformance from day one.

**Alternatives considered**: Reuse a2a-java-sdk-common JSON-RPC types (rejected — SDK types may have incompatible Jackson annotations or Gson dependency), raw Map handling (rejected — no type safety, untestable), no envelope (rejected — protocol non-conformant)

### Decision 15: LLM provider startup validation
**Rationale**: `LLMEndpointValidator` runs a lightweight HTTP probe in `@PostConstruct` and logs an error if the configured LLM endpoint is unreachable. The first real LLM call in `ReActOrchestrationEngine.executePlan` would otherwise fail with a cryptic transport error deep in the reactive chain. Early visibility lets the developer fix the endpoint or API key before the orchestration request path triggers it.

**Alternatives considered**: No validation (rejected — silent failure delayed to first LLM call), validate only in tests (rejected — tests pass but bootRun succeeds without the key)

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Spring Boot 4.x + Gradle 9.5 plugin compatibility** | **HIGH** | Task 0 gate verifies dependency resolution before coding. Agentscope 1.0.12 + a2a-java-sdk 0.3.3.Final compatibility verified via Task 0.4 (SDK serialization check). |
| RocketMQ class-loading isolation gap | **HIGH** | Decision 13: RocketMQ code isolated in `com.orch.hub.messaging.rocketmq` sub-package, wired by an unconditional `@Configuration`. No `enabled` toggle. |
| a2a-java-sdk-reference-jsonrpc brings Quarkus deps | **HIGH** | REMOVED from dependencies — Quarkus runtime + Vert.x conflict with Spring Boot. Only `a2a-java-sdk-client` kept. |
| JSON-RPC envelope missing → protocol non-conformant | **HIGH** | Decision 14: first-class `JsonRpcRequest/Response` DTOs defined before controller implementation. |
| OPENCODE_API_KEY missing → silent failure at first LLM call | **HIGH** | Decision 15: `@PostConstruct` startup validation in `LLMEndpointValidator`. |
| a2a-java-sdk 0.3.3 → 1.0.0 package rename | Medium | Lock to 0.3.3.Final for MVP; upgrade post-MVP aware of `io.a2a.*` → `org.a2aproject.sdk.*` migration. |

## Migration Plan

Not applicable — this is a greenfield bootstrap with no existing code to migrate.

## Open Questions

1. **LLM supplier diversification**: `OrchestrationLLM` interface defined (Decision 10). Next: add a `DeepSeekDirectProvider` as fallback?
2. **Authentication**: When should A2A authentication (API Key / OAuth2) be introduced? Currently the `/tasks/send` endpoint is unauthenticated; this is acceptable for local development only.
3. **AgentScope A2A Spring Boot Starter overlap**: AgentScope-Java 1.0.12 provides `AgentscopeA2aAutoConfiguration` with built-in A2A endpoints. Our hand-written controllers intentionally start without this starter to avoid coupling; the overlap should be revisited if AgentScope's auto-configuration matures.
