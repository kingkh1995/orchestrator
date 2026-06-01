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
5. Provide unit tests (config binding, serialization, session store) and integration tests (startup, endpoints)
6. Include local development environment (Docker Compose for RocketMQ)
7. No API documentation framework (A2A test page provides interaction testing; A2A protocol is an external standard)
8. Define LLM provider abstraction interface for future provider swapping
9. Provide A2A protocol product-level test page — single HTML file for browser-based AgentCard discovery and Task submission

**Non-Goals:**
1. No actual LLM invocation in MVP (mock responses only)
2. No RocketMQ connection in MVP (only dependency declaration; Docker Compose provided for local dev)
3. No ReAct orchestration logic in MVP (static mock responses)
4. No Client SDK implementation in MVP (only module skeleton)
5. No authentication, observability, or production hardening

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

### Decision 7: Docker Compose for local RocketMQ
**Rationale**: Even though MVP doesn't connect to RocketMQ, providing a `docker-compose.yml` upfront prevents day-2 environment setup blockers when messaging features arrive.

**Alternatives considered**: Manual installation guide (rejected — Docker Compose is one-command setup)

### Decision 8: No API documentation framework (A2A test page is sufficient)
**Rationale**: A2A uses JSON-RPC 2.0, not REST. OpenAPI/Swagger cannot represent JSON-RPC semantics (method + params envelope, dynamic method dispatch). SpringDoc 2.x is incompatible with Spring Boot 4.x, and SpringDoc 3.x remains REST-oriented. The A2A test page (Decision 14) provides interactive endpoint testing, and the A2A protocol is an external standard — the reference specification IS the documentation.

**Alternatives considered**: SpringDoc OpenAPI (rejected — v2 incompatible with SB4, v3 still REST-oriented; JSON-RPC generates meaningless OpenAPI schemas), Spring REST Docs (rejected — adds Asciidoctor build complexity for zero protocol documentation benefit), custom API docs (rejected — duplicates the standard spec)

### Decision 9: Unified configuration namespace `orch.*`
**Rationale**: Review identified inconsistency from original proposal (mixed `orch.*`, `hub.*`, `a2a.*` prefixes). All application configuration should follow `orch.*` to ensure discoverability and prevent namespace collision.

**Configuration layout**:
```yaml
orch:
  a2a:
    agent-name: "OrchestratorHub"
    description: "A2A Multi-Agent Orchestration Hub"
  messaging:
    rocketmq:
      enabled: false          # MVP default: no RocketMQ connection
      name-server: "localhost:9876"  # Standard RocketMQ property name
      namespace: ""
  llm:
    provider: "opencode-zen"  # Supports swapping via enum
    model: "deepseek-v4-flash-free"
    api-key: "${OPENCODE_API_KEY}"
```
**Alternatives considered**: `hub.*` prefix (rejected — overlaps with SDK's shared configuration concern), `a2a.*` (rejected — too narrow for messaging and LLM config)

### Decision 10: LLM provider abstraction interface
**Rationale**: CEO review identified free-model vendor lock-in as a HIGH risk. Rather than wiring `OpenAIChatModel` directly into the orchestration engine, define an `OrchestrationLLM` interface with `generatePlan(...)`. The `OpenCodeZenProvider` implementation wraps AgentScope-Java. This de-risks provider swapping with ~50 lines of interface code.

```java
public interface OrchestrationLLM {
    OrchestrationPlan generatePlan(SessionContext context);
}
```

**Alternatives considered**: Direct `OpenAIChatModel` usage (rejected — couples orchestration to a single provider), abstract factory (rejected — over-engineering for MVP)

### Decision 11: Conditional RocketMQ bean registration
**Rationale**: MVP explicitly does not connect to RocketMQ (Non-goal 2). Yet `OrchMessagingProperties` declares connection config. Without a conditional guard, any `@Component` referencing RocketMQ will crash `bootRun` when the broker isn't running. All messaging beans use `@ConditionalOnProperty(name = "orch.messaging.rocketmq.enabled", havingValue = "true")` with default `enabled: false`.

**Alternatives considered**: No conditional (rejected — bootRun crashes), separate profile (rejected — conditional is simpler)

### Decision 12: Dependency verification gate (Task 0)
**Rationale**: Spring Boot 4.0.6, Gradle 9.5.1, agentscope 1.0.12, and rocketmq-a2a 1.0.9 are all very recent releases. Their transitive compatibility is unverified. Task 0 validates `./gradlew dependencies` against a minimal build file and checks a2a-java-sdk entity Jackson serialization before any code is written, preventing late-discovery of broken dependencies.

**Hard gate**: All subsequent tasks are blocked until Task 0 passes. A `gradle.lockfile` is generated after the gate passes to freeze transitive versions, preventing silent dependency drift during implementation.

**Alternatives considered**: No gate (rejected — wasted work if deps don't resolve), downgrade to proven versions (rejected — user explicitly chose 4.0.6)

### Decision 13: Defer SDK module to post-MVP
**Rationale**: The SDK module (annotations, base classes) adds Gradle build time and dependency resolution overhead for zero MVP benefit. The hub's A2A entities are self-contained in `com.orch.hub.a2a`. SDK is deferred to a post-MVP milestone (Task 9.x).

**Alternatives considered**: Ship SDK in MVP (rejected — hub doesn't depend on it, premature abstraction)

### Decision 14: A2A product-level test page (single HTML file)
**Rationale**: The orchestration hub is an A2A agent — it needs a product-level interaction tool, not just code tests. A single HTML file (`a2a-test.html`) served from Spring Boot static resources provides zero-build, browser-based A2A protocol testing: AgentCard discovery (GET `/.well-known/agent.json`) and Task submission (POST `/tasks/send`). Three-panel layout (AgentCard | Task Composer | History) mirrors the A2A protocol's discover → interact → track flow. `localStorage` persists session history across reloads. Inline `rpcCall()` wraps JSON-RPC 2.0 envelope transparently.

**Alternatives considered**: Swagger UI (rejected — REST-oriented, poor JSON-RPC support), curl-only (rejected — not product-accessible), custom React app (rejected — over-engineered for MVP)

### Decision 15: JSON-RPC 2.0 envelope types as first-class DTOs
**Rationale**: A2A protocol is built on JSON-RPC 2.0. Every request/response must include `jsonrpc`, `id`, and either `result` or `error` fields. Without explicit `JsonRpcRequest<T>` and `JsonRpcResponse<T>` DTOs, the controller either (a) loses the JSON-RPC envelope (returns bare Task objects — protocol non-conformant) or (b) works with raw Maps (no type safety). Dedicated DTOs in `com.orch.hub.a2a.rpc` package ensure type safety, testability, and spec conformance from day one.

**Alternatives considered**: Reuse a2a-java-sdk-common JSON-RPC types (rejected — SDK types may have incompatible Jackson annotations or Gson dependency), raw Map handling (rejected — no type safety, untestable), no envelope (rejected — protocol non-conformant)

### Decision 16: LLM provider startup validation
**Rationale**: `OpenCodeZenProvider` is constructed as a Spring bean but never called in MVP (mock engine returns static DAG). If `OPENCODE_API_KEY` is missing or invalid, the first real LLM call post-MVP fails with a cryptic error. A `@PostConstruct` method validates the API key format and endpoint reachability at startup, failing fast so the developer knows immediately.

**Alternatives considered**: No validation (rejected — silent failure delayed to first LLM call), validate only in tests (rejected — tests pass but bootRun succeeds without the key)

### Decision 17: RocketMQ class-loading isolation
**Rationale**: `@ConditionalOnProperty` guards bean *registration* but not class *loading*. If any class in the default scan path imports `org.apache.rocketmq.*`, the JVM throws `NoClassDefFoundError` at class-loading time even if the bean is conditional. All RocketMQ-dependent code is isolated in `com.orch.hub.messaging.rocketmq` sub-package, loaded only by a `@ConditionalOnProperty`-guarded `@Configuration` class.

**Alternatives considered**: No isolation (rejected — importing a RocketMQ utility class crashes bootRun), separate profile (rejected — conditional is simpler with proper isolation)

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Spring Boot 4.x + Gradle 9.5 plugin compatibility** | **HIGH** | Task 0 gate verifies dependency resolution before coding. Agentscope 1.0.12 + a2a-java-sdk 0.3.3.Final compatibility verified via Task 0.4 (SDK serialization check). |
| **No RocketMQ toggle → bootRun crashes** | **HIGH** | `@ConditionalOnProperty(name="orch.messaging.rocketmq.enabled", havingValue="true")` + class-loading isolation (Decision 17). |
| **rocketmq-a2a 1.0.9 availability** | ✅ Resolved | Verified on Maven Central (2026-02-09). Pin exact version. |
| **AgentScope-Java OpenAIChatModel compatibility** | ✅ Resolved | Documented `.baseUrl()` support confirmed for custom endpoints (DeepSeek, Opencode Zen). |
| **JDK 25 + Spring Boot 4.0.6 runtime compatibility** | Medium | Bytecode targets 17. Test `bootRun` on JDK 25 explicitly in CI. |
| **Opencode Zen API limits** | Low | Free tier has rate limits. LLM provider abstraction (Decision 10) enables swapping. |
| **Free model business continuity** | Medium | `OrchestrationLLM` interface (Decision 10) + `orch.llm.provider` enum enables zero-code provider swap. |
| **Module re-split effort (hub → api/core/messaging)** | Low | Package boundaries documented (`com.orch.hub.a2a`, `.orchestration`, `.messaging`). No circular dependencies allowed. |
| **Sub-agent ecosystem cold start** | Medium | MVP has no sub-agents. Post-MVP milestone defined in Task 9.x. |
| **Test coverage gap (no unit tests)** | Low | Added 3 unit tests (Task 7.1-7.3) covering config binding, serialization, session store. Expand post-MVP. |
| **A2A test page CORS** | Low | Served from Spring Boot static dir, same-origin. No CORS needed in MVP. |
| **a2a-java-sdk-reference-jsonrpc brings Quarkus deps** | **HIGH** | REMOVED from dependencies — Quarkus runtime + Vert.x conflict with Spring Boot. Only `a2a-java-sdk-client` kept. |
| **JSON-RPC envelope missing → protocol non-conformant** | **HIGH** | Decision 15: first-class `JsonRpcRequest/Response` DTOs defined before controller implementation. |
| **OPENCODE_API_KEY missing → silent failure at first LLM call** | **HIGH** | Decision 16: `@PostConstruct` startup validation. Mock engine masks absence in MVP. |
| **RocketMQ class-loading despite conditional bean** | **HIGH** | Decision 17: RocketMQ code isolated in sub-package behind conditional `@Configuration`. |
| **InMemorySessionStore thread safety (concurrent access)** | Medium | Use `ConcurrentHashMap` + `CopyOnWriteArrayList`; document max sessions (1000 default). |
| **a2a-java-sdk 0.3.3 → 1.0.0 package rename** | Medium | Lock to 0.3.3.Final for MVP; upgrade post-MVP aware of `io.a2a.*` → `org.a2aproject.sdk.*` migration. |

## Migration Plan

Not applicable — this is a greenfield bootstrap with no existing code to migrate.

## Open Questions

1. **LLM supplier diversification**: `OrchestrationLLM` interface defined (Decision 10). Next: add a `DeepSeekDirectProvider` as fallback?
2. **Sub-agent ecosystem**: First real sub-agent post-MVP should be... a weather agent? A demo agent? (Needs product decision)
3. **Authentication**: When should A2A authentication (API Key / OAuth2) be introduced?
4. **AgentScope A2A Spring Boot Starter overlap**: AgentScope-Java 1.0.12 provides `AgentscopeA2aAutoConfiguration` with built-in A2A endpoints. Our hand-written controllers (Decision 3.x) intentionally start without this starter to avoid coupling, but the overlap must be documented for future maintainers.
5. **Spring AI A2A integration**: Spring AI community has `spring-ai-a2a-server-autoconfigure 0.2.0` bridging A2A to Spring Boot. Evaluate post-MVP when ReAct orchestration is implemented.
