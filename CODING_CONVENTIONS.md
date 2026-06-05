# Coding Conventions

> 面向 AI Agent 和开发者。新代码必须遵守。例外须在 PR 中明确说明。
>
> **LRU 排序**：最近违反的规则在分类顶部。review 时若发现违反，参见 `AGENTS.md` 维护流程。
---

## 架构

<!-- 最近违反：ReActOrchestrationEngine 依赖了 OrchLLMProperties；OpenCodeZenProvider 依赖了 OrchLLMProperties -->
- Engine 的 `.block()` timeout 通过构造参数 `Duration` 传入，不从属性类读取
- LLM Provider 只依赖 `Model`，不依赖 `OrchLLMProperties`
- `getModelName()` 委托给 `model.getModelName()`
- `llm/` 永不导入 `orchestration/`，依赖方向：`a2a` → `orchestration` → `llm`
- LLM 结构化输出用 XML，Prompt 要求 "Output ONLY the XML"：

```java
DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
Document doc = builder.parse(new InputSource(new StringReader(body)));
NodeList stepNodes = doc.getElementsByTagName("step");
```

## 命名

<!-- 最近违反：OpenCodeZenStartupValidator → LLMEndpointValidator -->
- 端点验证器类名不含 provider 名：`LLMEndpointValidator` ✅
- 实现类用具体描述命名：`DefaultLLMProvider` ✅，`LLMProviderImpl` ❌
- 接口以能力命名，不加 `I` 前缀：`OrchestrationEngine` ✅
- 常量 UPPER_SNAKE，配置属性 kebab-case
- 测试方法：`should[ExpectedBehavior]When[Condition]`

## Spring

<!-- 最近违反：OrchLLMProperties 设了 Java 默认值、用了 Provider 枚举；LLMEndpointValidator 多构造器少了 @Autowired -->
- 多构造器时 Spring 使用的那个必须加 `@Autowired`
- 构造器注入，禁止 `@Autowired` field 注入
- 配置属性用 `@ConfigurationProperties` + `@EnableConfigurationProperties`，**不在属性类上加 `@Component`**
- 不设 Java 默认值（安全 fallback 除外），全部由 `application.yml` 控制
- `provider` 用 String（只标识，不做路由）
- RocketMQ 条件 bean：`@ConditionalOnProperty("orch.messaging.rocketmq.enabled")`，默认 false
- `@Configuration` 只扫描隔离子包

## 代码风格

<!-- 最近违反：PLAN_SYS_PROMPT 用了 + 拼接；extractText 用了命令式 for 循环 -->
- 多行 `static final String` 用 JDK 17 text block，不用 `+` 拼接
- 集合处理优先用 Stream API：

```java
// ✔ stream
return resp.getContent().stream()
        .filter(TextBlock.class::isInstance)
        .map(TextBlock.class::cast)
        .map(TextBlock::getText)
        .filter(Objects::nonNull)
        .collect(Collectors.joining());
```

- 所有 `if`/`for`/`while` 必须用 `{}`
- `var` 仅当右侧类型明显时使用
- 用 `@Slf4j`，参数化占位符，异常作为最后一个参数
- 用 `@NonNull`/`@Nullable`，public 方法参数+返回值（或包级 `@NonNull`）必须标注

## 测试

<!-- 最近违反：Mockito 泛型方法用了 when().thenReturn -->
- Mockito 对泛型方法用 `doReturn`/`doThrow` 而非 `when().thenReturn`（避免编译报错）
- 显式 import 每个断言方法，禁止 `import static org.junit.jupiter.api.Assertions.*`
- JUnit 5，测试类 package-private
- `@SpringBootTest` 仅用于集成测试

```java
// ✔
doReturn(response).when(sender).send(any(), any());
```

## 构建 & 安全

- Gradle Groovy DSL + `libs.versions.toml` 版本目录
- 启用 `dependencyLocking.lockAllConfigurations()`
- Java 17 源码/目标兼容性
- 禁止在 commit 中包含 API key、token、密码、`.env`
- commit message: `type: 简短描述`

## API & 数据

<!-- 长期未违反，沉底等待清理 -->
- 纯载体用 `record`；需 Jackson 自定义构造时用不可变类 + `@JsonCreator` 静态工厂
- `@Accessors(fluent = true)`，getter 无 `get` 前缀
- `@JsonInclude(NON_NULL)` 默认
- 全局 JSON-RPC 错误码映射在 `JsonRpcAdvice`，Controller 不 try-catch
- 所有 endpoint 接收/返回完整 JSON-RPC 2.0 信封
- A2A 实体复用 `a2a-java-sdk-spec` 的类型
