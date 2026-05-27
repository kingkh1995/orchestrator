## 0. Dependency Verification Gate

- [ ] 0.1 Create minimal `settings.gradle` and `build.gradle` with all declared dependencies (Spring Boot 4.0.6 BOM, rocketmq-a2a 1.0.9, a2a-java-sdk-client 0.3.3.Final, agentscope 1.0.12)
- [ ] 0.2 Run `./gradlew dependencies` to verify all dependencies resolve on JDK 25
- [ ] 0.3 If resolution fails, adjust versions to known-compatible releases before proceeding
- [ ] 0.4 Validate a2a-java-sdk entity Jackson serialization: write a small Java snippet using `ObjectMapper.writeValueAsString(new Task())` and verify JSON output conforms to A2A spec shape (field names, status enum values, envelope structure)
- [ ] 0.5 Verify `a2a-java-sdk-core` does NOT transitively pull `a2a-java-sdk-reference-jsonrpc` (Quarkus-based, incompatible with Spring Boot). If it does, add Gradle exclusion rule.
- [ ] 0.6 Generate Gradle lockfile (`./gradlew dependencies --write-locks`) to freeze transitive versions after gate passes
- [ ] 0.7 **Hard gate**: mark that all subsequent tasks are BLOCKED until 0.1-0.6 all pass

## 1. Project Bootstrap

- [ ] 1.1 Create Gradle multi-module project structure (`orchestrator-parent`, `orchestrator-hub`)
- [ ] 1.2 Configure Gradle Wrapper with Tencent mirror (`https://mirrors.cloud.tencent.com/gradle/gradle-9.5.0-bin.zip`)
- [ ] 1.3 Configure Aliyun Maven Central repository in root `build.gradle`
- [ ] 1.4 Import Gradle version catalog (`libs.versions.toml`) for centralized version management
- [ ] 1.5 Set Java source/target compatibility to 17 with JDK 25 runtime
- [ ] 1.6 Add `.gitignore` for Gradle (`build/`, `.gradle/`, `!gradle/wrapper/gradle-wrapper.jar`), IntelliJ (`*.iml`, `.idea/`), environment (`.env`, `.env.local`), build outputs (`*.class`, `*.jar`, `*.war`), and OS files (`.DS_Store`, `Thumbs.db`)
- [ ] 1.7 Add `.gitattributes` with `* text=auto` and `*.java eol=lf`, `*.yml eol=lf`, `*.gradle eol=lf`, `*.md eol=lf` for cross-platform line ending consistency
- [ ] 1.8 Create `.env.example` with `OPENCODE_API_KEY=sk-your-api-key-here` and a comment directing to Opencode Zen console. Add `.env` to `.gitignore` (so devs can `cp .env.example .env`).
- [ ] 1.9 Create `settings.gradle` with module inclusion (include `orchestrator-hub`)
- [ ] 1.10 Early compile gate: run `./gradlew :orchestrator-hub:compileJava` to verify empty project compiles successfully before proceeding

## 2. Hub Server Dependencies

- [ ] 2.1 Add Spring Boot 4.0.6 starter dependencies (`spring-boot-starter-web`, `spring-boot-starter-actuator`)
- [ ] 2.2 Add `rocketmq-a2a` 1.0.9 and `rocketmq-client-java` 5.2.0
- [ ] 2.3 Add `a2a-java-sdk-client` 0.3.3.Final only (do NOT add `a2a-java-sdk-reference-jsonrpc` — it's Quarkus-based, incompatible with Spring Boot)
- [ ] 2.4 Add `agentscope` 1.0.12 (do NOT add `agentscope-a2a-spring-boot-starter` — we implement A2A endpoints manually to avoid coupling)
- [ ] 2.5 Verify all dependencies resolve with `./gradlew :orchestrator-hub:dependencies`
- [ ] 2.6 Early compile gate: run `./gradlew :orchestrator-hub:compileJava` to verify controllers and entity classes compile before proceeding to later groups

## 3. A2A Protocol Endpoints (MVP)

- [ ] 3.1 Create `JsonRpcRequest<T>` and `JsonRpcResponse<T>` generic records in `com.orch.hub.a2a.rpc` package. Include `jsonrpc` field, `id` field, `method` (request only), `params` (request only), `result` (response only), `error` (response only, with `code`, `message`, `data`)
- [ ] 3.2 Define A2A entity classes (reuse a2a-java-sdk entity classes directly) in `com.orch.hub.a2a` package
- [ ] 3.3 Create `AgentCardController` with GET `/.well-known/agent.json` returning static mock AgentCard
- [ ] 3.4 Create `TaskController` with POST `/tasks/send` — annotated with `consumes = "application/json"`, accepts `@RequestBody JsonRpcRequest<TaskInput>`
- [ ] 3.5 Implement mock `tasks/send` response returning `JsonRpcResponse<Task>` with status `COMPLETED` and a mock `Artifact`. Parse `method` field and return JSON-RPC error `-32601` (Method not found) for unknown methods.
- [ ] 3.6 Create `@RestControllerAdvice` global exception handler in `com.orch.hub.a2a`:
  - `HttpMessageNotReadableException` → JSON-RPC error `-32700` (Parse error)
  - `HttpMediaTypeNotSupportedException` → JSON-RPC error `-32600` (Invalid Request)
  - Generic `Exception` → JSON-RPC error `-32000` (Server error)
- [ ] 3.7 Create `OrchestratorHubApplication` Spring Boot main class

## 4. ReAct Orchestration & LLM Abstraction

- [ ] 4.1 Add `timeout` and `max-retries` fields to `OrchLLMProperties` (defaults: 30s, 3 retries) so config shape is stable before real LLM wiring
- [ ] 4.2 Create `OrchestrationLLM` interface with `generatePlan(SessionContext)` method in `com.orch.hub.llm` package
- [ ] 4.3 Implement `OpenCodeZenProvider` using AgentScope-Java's `OpenAIChatModel` (baseUrl=`https://opencode.ai/zen/v1/chat/completions`, modelName=`deepseek-v4-flash-free`)
- [ ] 4.4 Externalize API key via `OPENCODE_API_KEY` environment variable
- [ ] 4.5 Add `@PostConstruct` startup validation in `OpenCodeZenProvider`: verify `OPENCODE_API_KEY` is set and reachable (lightweight GET to Opencode Zen endpoint). Log clear error message if missing and fail fast.
- [ ] 4.6 Create single `OrchestrationEngine` interface with `executePlan(SessionContext)` method in `com.orch.hub.orchestration` package
- [ ] 4.7 Implement mock `OrchestrationEngine` returning static single-node DAG
- [ ] 4.8 Register `OrchestrationEngine` as a Spring bean with `@ConditionalOnMissingBean` for easy overrides
- [ ] 4.9 Add LLM connectivity smoke test: verify `OPENCODE_API_KEY` env var is set, `OrchLLMProperties` loads correctly from `application.yml`, and startup validation passes

## 5. RocketMQ Messaging Setup

- [ ] 5.1 Define `SessionEventStream` abstraction in `com.orch.hub.messaging` package (no RocketMQ imports — pure interface)
- [ ] 5.2 Define functional topic naming convention (`orch/agents/{capability}`)
- [ ] 5.3 Create `application.yml` with unified `orch.*` configuration namespace (use `orch.messaging.rocketmq.name-server` instead of `endpoint`). Include `orch.llm.timeout: 30s` and `orch.llm.max-retries: 3`
- [ ] 5.4 Add configuration properties classes:
  - `OrchMessagingProperties` — with `rocketmq.enabled: false` default + `rocketmq.name-server` + `rocketmq.namespace`
  - `OrchLLMProperties` — with `provider` (enum), `model`, `api-key`, `timeout` (Duration), `max-retries` (int)
  - `OrchA2AProperties` — with `agent-name`, `description`
- [ ] 5.5 **RocketMQ class-loading isolation audit**: audit every class that imports `org.apache.rocketmq.*`. Move all RocketMQ-dependent code into `com.orch.hub.messaging.rocketmq` sub-package. Define a `RocketMQMessagingConfiguration` class annotated with `@ConditionalOnProperty(name = "orch.messaging.rocketmq.enabled", havingValue = "true")` and `@ComponentScan("com.orch.hub.messaging.rocketmq")`. Add startup log: `log.info("RocketMQ disabled — orch.messaging.rocketmq.enabled=false")`.
- [ ] 5.6 Add `docker-compose.yml` for local RocketMQ development environment (namesrv + broker)

## 6. Session Management Foundation

- [ ] 6.1 Define five event type records: `OrchestrationDecisionEvent`, `TaskCommandEvent`, `TaskProgressEvent`, `TaskSummaryEvent`, `SessionSnapshotEvent`
- [ ] 6.2 Create `SessionContext` class tracking tasks and states (NOTE: this must be completed before or alongside Group 3, since controllers reference it)
- [ ] 6.3 Define `SessionSnapshot` record for checkpointing
- [ ] 6.4 Create `InMemorySessionStore` for MVP session persistence with:
  - `ConcurrentHashMap<String, SessionContext>` for thread-safe concurrent access
  - Configurable max sessions (default: 1000) to prevent memory leaks
  - Optional session TTL / expiry (default: no TTL for MVP)
  - Document: for development only, not for production

## 7. Integration & Verification

- [ ] 7.1 Unit test: `ConfigurationPropertiesBindingTest` — all three `@ConfigurationProperties` classes (`OrchMessagingProperties`, `OrchLLMProperties`, `OrchA2AProperties`) bind correctly from YAML. Test `orch.*` prefix is used consistently. Test missing `api-key` returns null/empty (not crash).
- [ ] 7.2 Unit test: `A2AEntitySerializationTest` — create `Task` object, serialize with `ObjectMapper`, verify JSON shape matches A2A spec (field names, status enum, envelope structure)
- [ ] 7.3 Controller error path tests (`@WebMvcTest(TaskController.class)`):
  - Invalid JSON body → HTTP 200 with JSON-RPC error `-32700` (Parse error)
  - Wrong Content-Type (`text/plain`) → HTTP 200 with JSON-RPC error `-32600`
  - Unknown method name → HTTP 200 with JSON-RPC error `-32601` (Method not found)
  - Valid `tasks/send` request → HTTP 200 with `JsonRpcResponse` containing `COMPLETED` Task
  - Missing `jsonrpc` field → HTTP 200 with JSON-RPC error `-32600`
- [ ] 7.4 Unit test: `InMemorySessionStoreTest` — session CRUD operations, snapshot save/load, concurrent access (10 threads, 100 ops each, no exceptions)
- [ ] 7.5 Unit test: `ConditionalBeanTest` — verify RocketMQ beans are NOT created when `orch.messaging.rocketmq.enabled=false`
- [ ] 7.6 Integration test: `ApplicationStartupTest` — context loads with all beans
- [ ] 7.7 Integration test: `AgentCardEndpointTest` — GET `/.well-known/agent.json` returns 200 with valid JSON
- [ ] 7.8 Integration test: `TaskSendEndpointTest` — POST `/tasks/send` with valid JSON-RPC envelope returns 200 with `COMPLETED` status
- [ ] 7.9 Verify `./gradlew build` passes all tests
- [ ] 7.10 Verify `./gradlew bootRun` starts successfully (port 8080, actuator health OK, no RocketMQ connection attempts)
- [ ] 7.11 Update README.md quick-start section with exact commands to build, run, set env vars, and test both endpoints

## 8. A2A Product Test Page

- [ ] 8.1 Create `a2a-test.html` in `src/main/resources/static/` with HTML5 structure and 3-panel flexbox layout (AgentCard | Task Composer | History)
- [ ] 8.2 Implement JSON-RPC service layer: `rpcCall()` envelope wrapper, `fetchAgentCard()`, `sendTask()`, auto-incrementing `id`
- [ ] 8.3 Implement AgentCard panel: "获取卡片" button, structured table view (name, description, url, capabilities), raw JSON display, loading/error states
- [ ] 8.4 Implement Task Composer panel: session-id input, message textarea, "发送任务" button with loading state, JSON-RPC response display (result/error distinction)
- [ ] 8.5 Implement History panel: in-memory + localStorage persistence, click-to-restore, empty placeholder, copy response
- [ ] 8.6 Add Spring Boot `GET /` redirect to `/a2a-test.html` controller
- [ ] 8.7 Verify page loads via `bootRun` and both endpoints work from the browser

## 9. Post-MVP (deferred from MVP)

- [ ] 9.1 Create `orchestrator-sdk` module with `@OrchAgent`, `@TaskHandler` annotations and `OrchAgentService` base class
- [ ] 9.2 Add A2A protocol streaming support (`tasks/sendSubscribe`)
- [ ] 9.3 Add `tasks/get` and `tasks/cancel` endpoints
