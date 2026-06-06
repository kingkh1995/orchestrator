## 0. Dependency Verification Gate

- [x] 0.1 Create minimal `settings.gradle` and `build.gradle` with all declared dependencies (Spring Boot 4.0.6 BOM, rocketmq-a2a 1.0.9, a2a-java-sdk-client 0.3.3.Final, agentscope 1.0.12)
- [x] 0.2 Run `./gradlew dependencies` to verify all dependencies resolve on JDK 25
- [x] 0.3 If resolution fails, adjust versions to known-compatible releases before proceeding
- [x] 0.4 Validate a2a-java-sdk entity Jackson serialization: write a small Java snippet using `ObjectMapper.writeValueAsString(new Task())` and verify JSON output conforms to A2A spec shape (field names, status enum values, envelope structure)
- [x] 0.5 Verify `a2a-java-sdk-core` does NOT transitively pull `a2a-java-sdk-reference-jsonrpc` (Quarkus-based, incompatible with Spring Boot). If it does, add Gradle exclusion rule.
- [x] 0.6 Generate Gradle lockfile (`./gradlew dependencies --write-locks`) to freeze transitive versions after gate passes
- [x] 0.7 **Hard gate**: mark that all subsequent tasks are BLOCKED until 0.1-0.6 all pass

## 1. Project Bootstrap

- [x] 1.1 Create Gradle multi-module project structure (`orchestrator-parent`, `orchestrator-hub`)
- [x] 1.2 Configure Gradle Wrapper with Tencent mirror (`https://mirrors.cloud.tencent.com/gradle/gradle-9.5.1-bin.zip`)
- [x] 1.3 Configure Aliyun Maven Central repository in root `build.gradle`
- [x] 1.4 Import Gradle version catalog (`libs.versions.toml`) for centralized version management
- [x] 1.5 Set Java source/target compatibility to 17 with JDK 25 runtime
- [x] 1.6 Add `.gitignore` for Gradle (`build/`, `.gradle/`, `!gradle/wrapper/gradle-wrapper.jar`), IntelliJ (`*.iml`, `.idea/`), environment (`.env`, `.env.local`), build outputs (`*.class`, `*.jar`, `*.war`), and OS files (`.DS_Store`, `Thumbs.db`)
- [x] 1.7 Add `.gitattributes` with `* text=auto` and `*.java eol=lf`, `*.yml eol=lf`, `*.gradle eol=lf`, `*.md eol=lf` for cross-platform line ending consistency
- [x] 1.8 Create `.env.example` with `OPENCODE_API_KEY=sk-your-api-key-here` and a comment directing to Opencode Zen console. Add `.env` to `.gitignore` (so devs can `cp .env.example .env`).
- [x] 1.9 Create `settings.gradle` with module inclusion (include `orchestrator-hub`)
- [x] 1.10 Early compile gate: run `./gradlew :orchestrator-hub:compileJava` to verify empty project compiles successfully before proceeding

## 2. Hub Server Dependencies

- [x] 2.1 Add Spring Boot 4.0.6 starter dependencies (`spring-boot-starter-web`, `spring-boot-starter-actuator`)
- [x] 2.2 Add `rocketmq-a2a` 1.0.9 and `rocketmq-client-java` 5.2.0
- [x] 2.3 Add `a2a-java-sdk-client` 0.3.3.Final only (do NOT add `a2a-java-sdk-reference-jsonrpc` — it's Quarkus-based, incompatible with Spring Boot)
- [x] 2.4 Add `agentscope` 1.0.12 (do NOT add `agentscope-a2a-spring-boot-starter` — we implement A2A endpoints manually to avoid coupling)
- [x] 2.5 Verify all dependencies resolve with `./gradlew :orchestrator-hub:dependencies`
- [x] 2.6 Early compile gate: run `./gradlew :orchestrator-hub:compileJava` to verify controllers and entity classes compile before proceeding to later groups

## 3. A2A Protocol Endpoints (MVP)

- [x] 3.1 Create `JsonRpcRequest<T>` and `JsonRpcResponse<T>` generic records in `com.orch.hub.a2a.rpc` package. Include `jsonrpc` field, `id` field, `method` (request only), `params` (request only), `result` (response only), `error` (response only, with `code`, `message`, `data`)
- [x] 3.2 Define A2A entity classes (reuse a2a-java-sdk entity classes directly) in `com.orch.hub.a2a` package
- [x] 3.3 Create `AgentCardController` with GET `/.well-known/agent.json` returning static mock AgentCard
- [x] 3.4 Create `TaskController` with POST `/tasks/send` — annotated with `consumes = "application/json"`, accepts `@RequestBody JsonRpcRequest<TaskInput>`
- [x] 3.5 Implement mock `tasks/send` response returning `JsonRpcResponse<Task>` with status `COMPLETED` and a mock `Artifact`. Parse `method` field and return JSON-RPC error `-32601` (Method not found) for unknown methods.
- [x] 3.6 Create `@RestControllerAdvice` global exception handler in `com.orch.hub.a2a`:
  - `HttpMessageNotReadableException` → JSON-RPC error `-32700` (Parse error)
  - `HttpMediaTypeNotSupportedException` → JSON-RPC error `-32600` (Invalid Request)
  - Generic `Exception` → JSON-RPC error `-32000` (Server error)
- [x] 3.7 Create `OrchestratorHubApplication` Spring Boot main class

## 4. ReAct Orchestration & LLM Abstraction

- [x] 4.1 Add `timeout` and `max-retries` fields to `OrchLLMProperties` (values externalized in `application.yml`, no Java defaults)
- [x] 4.2 Create `OrchestrationLLM` interface with `generatePlan(SessionContext)` method in `com.orch.hub.llm` package
- [x] 4.3 Implement `DefaultLLMProvider` (was `OpenCodeZenProvider`) using AgentScope-Java's `OpenAIChatModel` with endpoint/config from `application.yml`
- [x] 4.4 Externalize API key via `OPENCODE_API_KEY` environment variable
- [x] 4.5 Add `@PostConstruct` startup validation in `LLMEndpointValidator` (was `OpenCodeZenProvider`): lightweight GET to LLM endpoint; log error if unreachable
- [x] 4.6 Create single `OrchestrationEngine` interface with `executePlan(SessionContext)` method in `com.orch.hub.orchestration` package
- [x] 4.7 Implement `ReActOrchestrationEngine` (replaces mock) using AgentScope-Java's `ReActAgent` for real reasoning loop
- [x] 4.8 Register `OrchestrationEngine` as an unconditional Spring bean (no `@ConditionalOnMissingBean`, single engine only)
- [x] 4.9 Add LLM connectivity smoke test: verify `OPENCODE_API_KEY` env var is set, `OrchLLMProperties` loads correctly from `application.yml`, and startup validation passes

## 5. RocketMQ Messaging Setup

- [x] 5.1 Define `SessionEventStream` abstraction in `com.orch.hub.messaging` package (no RocketMQ imports — pure interface)
- [x] 5.2 Define functional topic naming convention (`orch/agents/{capability}`)
- [x] 5.3 Create `application.yml` with unified `orch.*` configuration namespace

### 5.4 — Configuration properties (initial 2-field scaffold)
- [x] 5.4a Define `OrchMessagingProperties` with 2-field RocketMQ nested config (`name-server`, `namespace`, `enabled=false` default)
- [x] 5.4b Define `OrchLLMProperties` + `OrchA2AProperties` config beans

### 5.4 — Configuration expansion (9 fields, 1:1 with RocketMQTransportConfig)
- [x] 5.4.1 Expand `OrchMessagingProperties.RocketMQ` to 9 fields (endpoint, namespace, access-key, secret-key, work-agent-response-topic, work-agent-response-group-id, agent-topic, agent-url, use-default-recover-mode) — 1:1 with `RocketMQTransportConfig`. No `enabled` field: RocketMQ is the product foundation, unconditionally wired
- [x] 5.4.2 Rename `nameServer` → `endpoint` (the broker's gRPC host:port in Local mode is the in-process proxy on 8081)
- [x] 5.4.3 Add Lombok `@Getter @Setter` to `OrchMessagingProperties` + nested `RocketMQ` (project-wide Lombok-first rule)

### 5.5 — RocketMQ bean wiring
- [x] 5.5.0 RocketMQ class-loading isolation: every `org.apache.rocketmq.*` import lives under `com.orch.hub.messaging.rocketmq`; `RocketMQMessagingConfiguration` is unconditional `@Configuration` + `@ComponentScan("com.orch.hub.messaging.rocketmq")`; startup logs the resolved transport config
- [x] 5.5.2 Unconditional `RocketMQClientFactory` (`@Component @RequiredArgsConstructor`) — `createClient(AgentCard) → Client.builder(card).withTransport(RocketMQTransport.class, config).build()`. The factory bean is always present. `TaskController` does NOT inject the factory (per AGENTS.md "TaskController knows nothing about sub-agents" rule); the factory's consumer is a future addition to the orchestration layer (see design decision in design.md §3)

### 5.6 — docker-compose (separated 3-tier Local mode: namesrv + broker-with-proxy + dashboard)
- [x] 5.6.1 `apache/rocketmq:5.5.0` namesrv on host port 9876
- [x] 5.6.2 `apache/rocketmq:5.5.0` broker on host ports 10911 (Remoting) + 8081 (gRPC), with LiteTopic + RocksDB store + 1M queue ceiling (`enableLiteTopic=true`, `liteTopicMaxQueueNum=1000000`, `messageStoreType=ROCKSDB`); broker starts with `--enable-proxy` flag so the proxy runs in-process (Local mode) — host port 8081 maps to the in-process gRPC entry point. The broker also takes `-pc /home/rocketmq/rocketmq-5.5.0/conf/rmq-proxy.json` to load the proxy-specific JSON config (cluster name, namesrv list), bind-mounted from `./docker/rocketmq/rmq-proxy.json`
- [x] 5.6.3 `brokerIP1=127.0.0.1` is required: with bridge networking, the broker's gRPC endpoint is registered to namesrv as the container-internal IP; setting `brokerIP1` to the host loopback makes the SDK's gRPC channel resolve to `localhost:8081` correctly
- [x] 5.6.4 `apacherocketmq/rocketmq-dashboard:latest` on host port 9090 (mapped to container Tomcat 8082)
- [x] 5.6.5 `apache/rocketmq:5.5.0` `proxy` container was dropped: cluster-mode proxy (`mqproxy -pm cluster`) on the same host as the broker had a client-5.2.0/stream-protocol incompatibility (`Stream is already completed` in `ClientImpl.fetchTopicRoute`); Local-mode in-process proxy (`--enable-proxy`) is the supported deployment per the official quick-start
- [x] 5.6.6 Store data lives INSIDE the broker container (no named volume mount) — Windows named-volume mount caused the broker to crash mid-start; on clean restart, store is wiped (acceptable for local dev single-node)
- [x] 5.6.7 `rmq-proxy.json` (`{"namesrvAddr": "namesrv:9876", "rocketMQClusterName": "DefaultCluster"}`) is bind-mounted into the broker and loaded via `-pc`. Once the proxy reads the correct cluster name from this file, the proxy no longer tries to auto-create the system broadcast topics (`DefaultHeartBeatSyncerTopic`, `DefaultHeartBeatTopicOffsetMovedEvent`, `DefaultCluster`) and the "create system broadcast topic failed" warning disappears from the proxy log
### 5.7 — Outbound Client factory (TDD)
- [x] 5.7.1 RED: `RocketMQClientFactoryTest` (5 tests — bean existence, transport config field-by-field, `createClient(AgentCard)` signature, sample RocketMQ AgentCard shape)
- [x] 5.7.2 GREEN: `RocketMQClientFactory` (`@Component @RequiredArgsConstructor`) — `createClient(AgentCard) → Client.builder(card).withTransport(RocketMQTransport.class, config).build()`

### 5.8 — RocketMQ SessionEventStream (TDD)
- [x] 5.8.1 RED: `RocketMQSessionEventStreamTest` (3 tests — bean wiring + JSON roundtrip via Jackson)
- [x] 5.8.2 GREEN: `RocketMQSessionEventStream` (`@Component`) — real `append()` publishes via cached `Producer` with `setLiteTopic(sessionId)` attribute; `replay()` deferred (LiteTopic drain strategy is non-trivial)

### 5.9 — Wire TaskController to outbound (TDD)
- [x] 5.9.1 RED: `TaskControllerTest` (6 tests covering JSON-RPC envelope validation, method dispatch, response shape, and `engine.executePlan(SessionContext)` delegation) — TaskController is intentionally NOT aware of the RocketMQ client factory; sub-agent dispatch lives in the orchestration engine
- [x] 5.9.2 GREEN: `TaskController.handleTasksSend` calls `engine.executePlan(SessionContext)` only. The sub-agent dispatch via `RocketMQClientFactory.createClient()` is the future responsibility of `SubAgentDispatcher` in `com.orch.hub.orchestration` (Group 6), which is the right layer for transport-aware routing — not the public A2A entrypoint
- [x] 5.9.2 GREEN: `TaskController.handleTasksSend` calls `engine.executePlan(SessionContext)` only. `TaskController` is the public A2A entrypoint and intentionally transport-agnostic; any sub-agent dispatch happens inside `ReActOrchestrationEngine` or downstream orchestration-layer code, not in the controller

### 5.10 — End-to-end smoke test
- [x] 5.10.1 `docker compose up` brings up 3 containers healthy (namesrv 9876, broker 10911+8081, dashboard 9090) — verified via `docker ps` + `mqadmin sendMessage` round-trip
- [x] 5.10.2 `bootJar` builds the executable jar; `java -jar ...jar --server.port=8080` starts successfully
- [x] 5.10.3 `rocketmq-client-java 5.2.0` ↔ `apache/rocketmq:5.5.0` Local mode is fully working — producer-only smoke test against `localhost:8081` returns 4 successful `msgId`s (`01B025AA77D56F7FC40A35460B00000000` etc.). Two prerequisites were needed: (1) broker must start with `--enable-proxy` (Local mode) since cluster-mode proxy has a stream-protocol incompatibility with the 5.2.0 client, and (2) `brokerIP1=127.0.0.1` so the SDK resolves the gRPC endpoint to the host loopback instead of the container's bridge-network IP. `LitePushConsumer` startup with a stable group name still fails on `group not found` — that's a separate `LiteTopic` group-management API mismatch between client 5.2.0 and broker 5.5.0, not the same stream-protocol bug; not blocking the 5.10 verification
- [x] 5.10.4 `application.yml` updated: `endpoint: localhost:8081`, `namespace: orchestrator-dev`, `work-agent-response-group-id: orch_work_response_gid` (underscores only — regex `^[%a-zA-Z0-9_-]+$` rejects dots)
- [x] 5.10.5 `5.6` was revised: the prior cluster-mode proxy deployment (4 containers, port 8080) is replaced by Local-mode in-process proxy (3 containers, port 8081) — see 5.6 for the new shape

### 5.11 — Drop SessionEvent* dead code (the SDK already covers the abstractions)
- [x] 5.11.1 RED-equivalent: 85/85 tests green before deletion (baseline locked)
- [x] 5.11.2 Delete `SessionEvent`, `SessionEventStream`, `TopicNames`, `RocketMQSessionEventStream` in `messaging/` + their 3 test classes — verified production code has zero references (`search -E 'SessionEvent|TopicNames'` returns no matches in `src/main` or `src/test` after deletion)
- [x] 5.11.3 GREEN: 77/77 tests green after deletion (85 - 8 from the 3 deleted test classes); build still BUILD SUCCESSFUL. The behavior is unchanged because the deleted abstractions had no consumers in production code; the `RocketMQTransportConfig` bean in `RocketMQMessagingConfiguration` and the outbound `RocketMQClientFactory` are the real wiring that survives


## 7. Integration & Verification (DONE)

- [x] 7.1 Property binding tests: `OrchMessagingPropertiesTest` (2 tests, 9 RocketMQ fields 1:1 with `RocketMQTransportConfig`) + `OrchLLMPropertiesTest` (2 tests, timeout + max-retries; missing `api-key` returns null without crash via `@Nullable` setter) + `OrchA2APropertiesTest` (8 tests covering agent-name, description, version, public-url, default-modes, capabilities, provider, skills). All bind from YAML under the `orch.*` prefix
- [x] 7.2 A2A entity serialization: covered by `JsonRpcDtoTest` (12 tests) and `AgentCardControllerTest.shouldSerializeAgentCardToJsonRoundTrip` — `Task`/`Message`/`Artifact`/JSON-RPC envelope all serialize per A2A spec shape
- [x] 7.3 Controller error paths: `JsonRpcAdviceTest` (6 tests) covers `-32700` parse error, `-32600` invalid request, `-32601` method not found, `-32000` server error; `TaskControllerTest` (8 tests) covers valid `tasks/send` → `COMPLETED` and missing `jsonrpc` field → `-32600`
- [x] 7.4 `ApplicationStartupTest`: `OrchestratorHubApplicationTest.shouldLoadApplicationContext` — Spring context loads with all beans wired (including RocketMQ config + factory)
- [x] 7.5 `AgentCardEndpointTest`: `AgentCardControllerTest` (3 tests) — `shouldReflectConfiguredCapabilitiesInAgentCard` + `shouldSerializeAgentCardToJsonRoundTrip` validate GET `/.well-known/agent.json`
- [x] 7.6 `TaskSendEndpointTest`: `TaskControllerTest` (8 tests) — `shouldReturnTaskWithCompletedStatus` + `shouldEmbedEngineResponseAsTextPartArtifact` + 6 more cover POST `/tasks/send` with valid JSON-RPC envelope
- [x] 7.7 `./gradlew.bat :orchestrator-hub:test` → **81/81 tests pass**, 0 failures, 0 errors
- [x] 7.8 Container composition + bean wiring validated: `RocketMQTransportConfigBeanTest.shouldExposeRocketMQTransportConfigBean` and `LLMConnectivitySmokeTest.shouldHaveLLMBeanRegistered` confirm startup wiring; `docker compose up` brings up 3 containers healthy (namesrv 9876, broker 10911+8081, dashboard 9090) and the producer-only smoke test against `localhost:8081` returns 4 successful `msgId`s

