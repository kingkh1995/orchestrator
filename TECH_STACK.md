# Tech Stack

> 面向 AI Agent 和开发者。在做技术决策前先读此文档。

---

## Runtime & Language

| 项目 | 值 |
|------|-----|
| JDK | 25（编译目标 **17**，字节码兼容性） |
| 语言 | Java 17（source/target） |
| 构建 | Gradle 9.5.1，Groovy DSL，Wrapper |

**关键约束**：
- `sourceCompatibility = targetCompatibility = 17` — 框架依赖要求此版本
- Gradle 发行版通过**腾讯镜像**分发（`gradle-wrapper.properties`）
- Maven 仓库通过**阿里云镜像**加速（根 `build.gradle`）
- 所有子模块启用 `dependencyLocking.lockAllConfigurations()` — 锁定传递依赖版本

---

## Frameworks

### Spring Boot 4.0.6

- Web 框架：`spring-boot-starter-web`（内嵌 Tomcat）
- 健康检查：`spring-boot-starter-actuator`
- 配置文件：`application.yml`，统一 `orch.*` 前缀
- Conditional bean：`@ConditionalOnProperty` / `@ConditionalOnMissingBean` 控制 bean 注册

### AgentScope-Java 1.0.12

- LLM 调用抽象（`Model`，`OpenAIChatModel`）
- ReAct Agent（`ReActAgent`）
- 不引入 `agentscope-a2a-spring-boot-starter` — A2A endpoint 手写，避免耦合

---

## A2A Protocol

### a2a-java-sdk 0.3.3.Final

- `a2a-java-sdk-spec` — A2A 实体类型（`Task`, `Message`, `Artifact`, `AgentCard`, `TaskState` 等）
- `a2a-java-sdk-client` — client 端工具
- **🛑 NEVER 引入** `a2a-java-sdk-reference-jsonrpc` — 基于 Quarkus + Vert.x，与 Spring Boot 冲突
- 实体用 Builder 模式构造：

```java
var task = new Task.Builder()
    .id(UUID.randomUUID().toString())
    .status(new TaskStatus(TaskState.COMPLETED))
    .artifacts(List.of(artifact))
    .build();
```

---

## Messaging

### Apache RocketMQ 5.x + rocketmq-a2a 1.0.9

- 消息中间件，仅在 `orch.messaging.rocketmq.enabled=true` 时激活
- MVP 默认禁用（`enabled: false`），无需启动 RocketMQ 即可开发
- RocketMQ 相关代码强制隔离在 `com.orch.hub.messaging.rocketmq` 包内
- 本地开发用 `docker-compose.yml`（namespace + broker）
- LiteTopic 用于会话事件流

---

## LLM

### Opencode Zen / DeepSeek V4 Flash Free

- 默认 LLM 供应商
- API 端点：`https://opencode.ai/zen/v1/chat/completions`
- API Key：`OPENCODE_API_KEY` 环境变量
- 通过 `OpenAIChatModel`（AgentScope-Java 的）配置 `.baseUrl()` + `.endpointPath()`
- LLM Provider 可切换 — `OrchestrationLLM` 接口抽象了供应商差异

```yaml
orch:
  llm:
    provider: "opencode-zen"
    model: "deepseek-v4-flash-free"
    api-key: "${OPENCODE_API_KEY}"
```

---

## Build Dependencies

| 库 | 用途 | 说明 |
|----|------|------|
| Lombok 1.18.36 | 样板代码生成 | `compileOnly` + `annotationProcessor` |
| JSpecify 1.0.0 | `@NonNull` / `@Nullable` | `compileOnly`，零运行时开销 |
| JUnit 5 (Platform) | 测试 | `testImplementation` |
| Spring Boot Starter Test | 集成测试 | 含 JUnit 5 + Mockito |
| Jackson (via Spring Boot) | JSON 序列化 | 默认引入 |

---

## 开发环境

- **JDK**: 25（sourceCompatibility=17 仅影响字节码生成）
- **IDE**: IntelliJ IDEA + google-java-format 插件
- **本地 RocketMQ**: `docker-compose up -d`（MVP 阶段非必需）
- **环境变量**: 复制 `.env.example` → `.env`，填入 `OPENCODE_API_KEY`
