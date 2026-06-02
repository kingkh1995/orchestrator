# Code Style Violations

> 只记录已发现的违规。正确做法以仓库已有代码为准。

---

## 1. 日志消息禁止句号结尾

```java
// ✘ 违规（新代码）
log.warn("OPENCODE_API_KEY is not set. LLM calls will fail at runtime.");

// ✔ 正确（原代码风格）
log.warn("OPENCODE_API_KEY is not set, LLM calls will fail at runtime");
```

## 2. 测试静态导入禁止通配符

```java
// ✘ 违规（a2a 包 4 个测试文件）
import static org.junit.jupiter.api.Assertions.*;

// ✔ 正确（其余 10 个测试文件）
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
```
