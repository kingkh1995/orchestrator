# Coding Conventions

> 本文档面向 AI Agent 和开发者。所有新代码必须遵守。例外须在 PR 中明确说明。

---

## 1. Package & Module

- Root package: `com.orch.hub.*`
- 子包按领域划分: `a2a`, `orchestration`, `llm`, `session`, `config`, `messaging`
- RocketMQ 相关代码必须在 `messaging.rocketmq` 子包内，由 `@ConditionalOnProperty` + `@ComponentScan` 隔离加载
- 禁止循环依赖：`a2a` → `orchestration` → `llm` 单向调用

## 2. Dependency Injection

- **构造器注入**。禁止 `@Autowired` field 注入（`OpenCodeZenStartupValidator` 是唯一已知例外 — 不改）
- Spring Bean 用 `@Component` / `@Configuration`，非 Spring 类用纯构造器

## 3. Immutable DTOs

```java
// ✔ 不可变记录（纯载体）
public record OrchestrationPlan(List<String> steps) {}

// ✔ 不可变类 + 工厂方法（有 Jackson 反序列化需求时）
@Accessors(fluent = true)
@Getter @EqualsAndHashCode @ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonRpcResponse<T> {
    @JsonProperty("jsonrpc") private final String jsonrpc;
    @JsonProperty("id")      private final String id;
    @JsonProperty("result")  private final T result;
    @JsonProperty("error")   private final JsonRpcError error;

    public static <T> JsonRpcResponse<T> result(...) { ... }
    public static <T> JsonRpcResponse<T> error(...)  { ... }

    @JsonCreator
    static <T> JsonRpcResponse<T> of(...) { ... }
}
```

规则：
- 用 `record` 除非需要 Jackson 自定义构造逻辑
- 用 `@Accessors(fluent = true)` — getter 无 `get` 前缀
- 用 `@JsonCreator` 静态工厂方法，而非 `@ConstructorProperties`
- 对外暴露静态工厂方法而非 public 构造器
- `@JsonInclude(NON_NULL)` 默认

## 4. Null Safety

- 用 `org.jspecify.annotations.@NonNull` / `@Nullable`
- `@NonNull` 是包/参数默认，仅在需要覆盖或显式标记时使用
- 所有 public 方法参数 + 返回值必须标注（或包级 `@NonNull`）
- `Objects.requireNonNull()` 在工厂方法中校验非空参数

```java
public static <T> JsonRpcResponse<T> result(@NonNull String jsonrpc, @NonNull T result) {
    Objects.requireNonNull(jsonrpc);
    Objects.requireNonNull(result);
    return new JsonRpcResponse<>(jsonrpc, null, result, null);
}
```

## 5. Logging

- 用 `@Slf4j`
- 使用参数化占位符，不用字符串拼接: `log.warn("key {} not set", key)`
- 异常作为最后一个参数传入: `log.error("failed to process {}", id, exception)`

## 6. Configuration Properties

- 统一前缀 `orch.*`（不是 `hub.*` 或 `a2a.*`）
- 用 `@ConfigurationProperties(prefix = "orch.xxx")` + `@Component`
- 提供合理的默认值，使 MVP 配置尽可能零配置启动

```java
@Component
@ConfigurationProperties(prefix = "orch.llm")
public class OrchLLMProperties {
    private String provider = "opencode-zen";
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    // getter / setter ...
}
```

## 7. Conditional Beans

- RocketMQ 相关 bean 必须加 `@ConditionalOnProperty(name = "orch.messaging.rocketmq.enabled", havingValue = "true")`
- 默认 `enabled: false` 使 bootRun 无需 RocketMQ 即可启动
- Mock/Real 实现用 `@ConditionalOnMissingBean` 做优雅回退
- 注册条件 bean 的 `@Configuration` 类必须只扫描隔离子包，防止 RocketMQ 类的 ClassLoader 加载

## 8. Naming

| 元素 | 风格 | 示例 |
|------|------|------|
| 类/接口 | PascalCase | `OrchestrationEngine`, `JsonRpcRequest` |
| 方法 | camelCase | `generatePlan`, `executePlan` |
| 常量 | UPPER_SNAKE | `PLAN_SYS_PROMPT`, `OPENCODE_ZEN_BASE_URL` |
| 字段 | camelCase | `sessionId`, `maxRetries` |
| 配置属性 | kebab-case | `orch.messaging.rocketmq.enabled` |
| 测试方法 | `shouldXxxWhenYyy` | `shouldReturnCompletedTaskForTasksSendMethod` |

- 接口以能力命名，不加 `I` 前缀：`OrchestrationEngine` ✅，`IEngine` ❌
- 实现类以具体描述命名：`ReActOrchestrationEngine`，`MockOrchestrationEngine`

## 9. Testing

- **JUnit 5**（无 JUnit 4 注解）
- 测试类默认不加 `public` — package-private

```java
class TaskControllerTest {
    @BeforeEach
    void setUp() { ... }

    @Test
    void shouldReturnCompletedTaskForTasksSendMethod() {
        // Arrange
        var request = ...;
        // Act
        var response = controller.handleTasksSend(request);
        // Assert
        assertEquals("2.0", response.jsonrpc());
    }
}
```

- 方法命名: `should[ExpectedBehavior]When[Condition]`
- **禁止 `import static org.junit.jupiter.api.Assertions.*`**（通配符静态导入）— 必须显式 import 每个断言方法
- 优先真实对象而非 mock（项目目前无 Mockito 依赖）
- `@WebMvcTest` 用于 controller 错误路径测试
- `@SpringBootTest` 仅用于集成测试

## 10. Error Handling

- 全局 JSON-RPC 错误码映射集中在 `JsonRpcAdvice`
- 遵循 A2A 协议错误码:
  - `-32700` Parse error
  - `-32600` Invalid Request
  - `-32601` Method not found
  - `-32000` Server error
- Controller 内不 try-catch，抛给 `@RestControllerAdvice`
- JSON-RPC error 用标准错误类名引用：`/** @see io.a2a.spec.InvalidRequestError */`

## 11. API Design (A2A / JSON-RPC)

- 所有 endpoint 接收和返回完整的 JSON-RPC 2.0 信封
- Controller 方法签名直接使用 `JsonRpcRequest<T>`，不拆包
- A2A 实体复用 `a2a-java-sdk-spec` 的类型（`Task`，`Message`，`Artifact` 等），不自己定义
- `@JsonProperty("xxx")` 显式标注，不依赖命名策略

## 12. Build & Deps

- Gradle Groovy DSL + `libs.versions.toml` 版本目录
- 所有模块启用 `dependencyLocking.lockAllConfigurations()` — 锁定传递依赖版本
- 禁止引入 `a2a-java-sdk-reference-jsonrpc`（Quarkus，与 Spring Boot 冲突）
- Aliyun Maven 仓库（阿里云加速）在根 project 配置
- Java 源码/目标兼容性 17（JDK 25 运行时）

## 13. Git & Commit

- 禁止在 commit 中包含 API key、token、密码、`.env` 文件
- commit message 格式: `type: 简短中文/英文描述`（type: feat/fix/refactor/docs/test/chore）

## 14. `var` 使用

- 允许 `var` 当右侧类型明显时：`var request = JsonRpcRequest.of(...)` ✅
- 禁止 `var` 当右侧类型含混时：`var result = someMethod()` ❌
