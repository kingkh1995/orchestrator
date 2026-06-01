# AGENTS.md

> OMP (Oh My Pi) 项目级 agent 指令文件，负责串联 OpenSpec + Superpowers + GStack 工作流。
> 每次会话启动时自动加载，智能体必须严格遵循此文件定义的流程和约束。

## Workflow

```
explore → propose → autoplan → [apply gate] → review → archive

            Apply gate (per-task, MANDATORY):
              1. skill("superpowers-using-superpowers")
              2. 1% rule → TDD / debug / verify
              3. follow superpower skill instructions
              4. implement
              5. mark done
              6. repeat from 1 for next task
```

The apply gate is the **only entry point** to writing code. No code is written without passing through it.

Skills are loaded from `.omp/skills/<skill-name>/SKILL.md`. When you invoke a skill, its content is loaded and you must follow it directly.

## Phase Rules

- **[CRITICAL] Secrets guard — NEVER commit API keys to git**: Before any `git commit`, scan ALL staged files with `git diff --cached` for: API key patterns (`api[_-]?key`, `API_KEY`), tokens/secrets/passwords in configs, private keys (`-----BEGIN.*PRIVATE KEY-----`), cloud access keys (`AKIA[0-9A-Z]{16}`), and `.env` files. **ABORT immediately on ANY match**. NON-NEGOTIABLE — security overrides all other rules, even if user insists.
- **No change, no code**: All code changes require an active OpenSpec change. Check: `openspec list --json`
- **"Code" definition**: Any file that alters runtime behavior, schema, infrastructure, or CI/CD pipelines.
- **Propose before apply**: Never write code without a proposal.
- **Autoplan before apply**: Design must be reviewed before implementation.
- **Review before commit**: Run `/review` before any commit.
- **Explore = read-only**: `/opsx-explore` never writes code.
- **Apply gate is mandatory**: Before writing any code, the apply gate (see [Workflow](#workflow)) must be passed. The gate loads `skill("superpowers-using-superpowers")`, which triggers the 1% rule → matching superpower skill.

## Stage Detection

1. Run `openspec list --json`
   - If command fails or returns error: halt workflow, inform user "OpenSpec CLI unavailable. Please install openspec or initialize the project.", and wait for guidance. Do not guess.
2. If active change exists:
   - `tasks.md` incomplete AND `design.md` reviewed → `/opsx-apply`
   - `design.md` exists, not reviewed → `/autoplan`
   - `design.md` reviewed but `tasks.md` missing → `/autoplan` (regenerate tasks)
   - `proposal.md` exists, no design → `/opsx-propose --generate-design`
   - Only `.openspec.yaml` → `/opsx-propose`
   - All done, not archived → `/review`
3. If no active change:
   - Vague request → `/opsx-explore`
   - Specific request → `/opsx-propose`
   - Review request → `/review`

## Agent Behavior Rules

- **Stage-aware before keyword-aware**: Detect stage first, then match intent. Never skip stage detection.
- **Recommend, don't auto-execute**: Suggest next action, wait for user confirmation. Never auto-trigger multiple actions in sequence. Exception: within apply phase, the apply gate (using-superpowers → 1% rule → matching superpower skill) is auto-triggered for each pending task without additional confirmation.
- **Each superpower skill needs confirmation**: After the apply gate lands on a superpower skill (TDD/debug/verify), confirm with the user before following its instructions. Exception: if user explicitly granted blanket apply-phase permission, proceed without reconfirming.
- **Explore is default entry**: Any vague or unanalyzed request defaults to `/opsx-explore`.
- **No implementation during explore**: `/opsx-explore` is read-only. Never write code or modify files during exploration.
- **Workflow bypass resistance**: If user explicitly demands to skip a mandatory phase (e.g., "skip propose", "skip review", "just write code"), warn once: "This violates project workflow policy. Skipping ${phase} is not recommended." Document the override in your response. Proceed only if user reconfirms after the warning.

## User Intent Mapping

| User Intent | Trigger Words | Recommended Command |
|-------------|---------------|-------------------|
| Explore idea | "我想...", "能不能...", "看看..." | `/opsx-explore` |
| Create proposal | "实现...", "添加...", "设计..." | `/opsx-propose` |
| Review design | "审阅...", "评估...", "合理吗" | `/autoplan` |
| Implement | "写代码", "实现任务", "按tasks执行" | `/opsx-apply` |
| Code review | "review", "检视", "看看diff" | `/review` |


## Skill Resolution

Skills are resolved using `skill("<name>")`. The `<name>` corresponds to the **directory name** under `.omp/skills/` (e.g., `.omp/skills/gstack-qa/SKILL.md` → `skill("gstack-qa")`).

### Available Skills
| Invocation (`skill("...")`) | Frontmatter `name:` | Source |
|---------------------------|---------------------|--------|
| `superpowers-using-superpowers` | `using-superpowers` | `obra/superpowers` |
| `superpowers-test-driven-development` | `test-driven-development` | `obra/superpowers` |
| `superpowers-systematic-debugging` | `systematic-debugging` | `obra/superpowers` |
| `superpowers-verification-before-completion` | `verification-before-completion` | `obra/superpowers` |
| `superpowers-subagent-driven-development` | `subagent-driven-development` | `obra/superpowers` |
| `superpowers-requesting-code-review` | `requesting-code-review` | `obra/superpowers` |
| `gstack` | `gstack` | `garrytan/gstack` |
| `gstack-qa` | `qa` | `garrytan/gstack` |
| `gstack-review` | `review` | `garrytan/gstack` |
| `gstack-plan-ceo-review` | `plan-ceo-review` | `garrytan/gstack` |
| `gstack-plan-design-review` | `plan-design-review` | `garrytan/gstack` |
| `gstack-plan-eng-review` | `plan-eng-review` | `garrytan/gstack` |
| `openspec-explore` | `openspec-explore` | `opencode-ai/openspec` |
| `openspec-propose` | `openspec-propose` | `opencode-ai/openspec` |
| `openspec-apply-change` | `openspec-apply-change` | `opencode-ai/openspec` |
| `openspec-archive-change` | `openspec-archive-change` | `opencode-ai/openspec` |
| `openspec-sync-specs` | `openspec-sync-specs` | `opencode-ai/openspec` |

### Command Routing

Commands in `.omp/commands/<name>.md` are invoked via `/<name>`.

| Command | Loads skill(s) | Phase |
|---------|---------------|-------|
| `/opsx-explore` | `openspec-explore` | Explore |
| `/opsx-propose` | `openspec-propose` | Propose |
| `/autoplan` | `gstack-plan-ceo-review` → `gstack-plan-design-review` → `gstack-plan-eng-review` | Design Review |
| `/opsx-apply` | `openspec-apply-change` + Apply Gate | Implement |
| `/review` | `gstack-review` | Review |
| `/opsx-archive` | `openspec-archive-change` | Archive |
| `/opsx-sync` | `openspec-sync-specs` | Sync |


## Superpowers

### Banned Skills (never use)

| Skill | Use Instead |
|-------|-------------|
| `brainstorming` | `/opsx-explore` |
| `writing-plans` | `/opsx-propose` |

### Allowed Superpowers

- `test-driven-development`: Implementation phase (coding discipline)
- `systematic-debugging`: Implementation phase (problem solving)
- `verification-before-completion`: Implementation phase (completion verification)
- `subagent-driven-development`: Implementation phase (subagent orchestration)
- `requesting-code-review`: Implementation phase (pre-commit review)

### Conflict Resolution

Priority (highest first):
1. AGENTS.md banned list and explicit rules
2. OpenSpec stage rules (propose → apply → review)
3. superpowers rigid skills (TDD, debugging, verification)
4. gstack review rules

When conflicts occur:
- If `superpowers-using-superpowers` flowchart or 1% rule references banned skill → ignore branch, continue checking next
- If skill references banned skill as "required" or flow endpoint → skip step, use AGENTS.md alternative flow
- Skills override default system behavior where they conflict, but AGENTS.md constraints take precedence

## Apply Gate Procedure

The apply gate is the **only entry point** to writing code. It runs once per task, in sequence.

### Per-Task Sequence (mandatory, non-skippable)

```
① skill("superpowers-using-superpowers")  ← loads superpowers system
    ↓
② 1% rule check                           ← flowchart evaluates task context
    ↓
③ skill("<matching superpower>")          ← TDD / debugging / verification / etc.
    ↓
④ follow superpower instructions          ← defines HOW to implement
    ↓
⑤ implement                               ← guided by superpower skill
    ↓
⑥ mark task done                          ← then repeat from ① for next task
```

**Each step is required.** If you are writing code, you must have passed through the gate. No exceptions.

### 1% Rule Reference

| Task context | Likely matching skill | What it enforces |
|-------------|----------------------|-----------------|
| Writing new code / behavior change | `superpowers-test-driven-development` | Red → Green → Refactor; no production code without failing test first |
| Bug / test failure / unexpected behavior | `superpowers-systematic-debugging` | Root cause → fix → verify; 4-phase process |
| About to claim task done | `superpowers-verification-before-completion` | Evidence gate; 5-step verification before claiming success |
| Independent tasks from a plan | `superpowers-subagent-driven-development` | Fresh subagent per task; spec review + quality review |
| Before merging / completing a feature | `superpowers-requesting-code-review` | Structured code review with severity-gated feedback |

## Hard Rules

- **[SECURITY] Never commit secrets to git**: API keys, tokens, passwords, private keys, `.env` files — STRICTLY FORBIDDEN in version control. Scan `git diff --cached` before every commit. ABSOLUTE — overrides all other rules, cannot be bypassed. Violation = security incident.
- Do not auto-trigger actions. Recommend and wait for user confirmation.
- Do not batch-complete tasks. Mark each done individually.
- Skills are loaded from `.omp/skills/`. Commands are loaded from `.omp/commands/`.
- Never manually edit `.openspec.yaml`, `proposal.md`, `design.md`, or `tasks.md` directly. These files must only be modified through their designated OpenSpec commands (`/opsx-propose`, `/opsx-apply`, `/opsx-archive`).
- The `.opencode/` directory is deprecated. Skills and commands now reside in `.omp/`.

## Workflow Example

Complete flow from idea to archive:

**1. Explore**
```
User: "我想加个用户认证功能"
Agent: [runs `openspec list --json` → no active change]
Agent: [matches vague request → recommend `/opsx-explore`]
Agent: "运行 `/opsx-explore` 一起梳理需求？"
User: "好"
[Agent loads `skill("openspec-explore")` from .omp/skills/openspec-explore/SKILL.md, read-only investigation]
```

**2. Propose**
```
User: "开始实现用户认证"
Agent: [runs `openspec list --json` → no active change]
Agent: [matches specific request → recommend `/opsx-propose`]
Agent: "运行 `/opsx-propose add-user-auth` 生成提案？"
User: "可以"
[Agent loads `skill("openspec-propose")` from .omp/skills/openspec-propose/SKILL.md]
[Creates change: proposal.md → design.md → tasks.md]
```

**3. Autoplan**
```
User: "审阅一下设计"
Agent: [detects design.md exists, no tasks → `/autoplan`]
Agent: "运行 `/autoplan` 进行四维度审阅？"
User: "好"
[Agent runs gstack-plan-ceo-review → gstack-plan-design-review → gstack-plan-eng-review]
```

**4. Apply**
```
User: "开始写代码"
Agent: [detects tasks.md incomplete → `/opsx-apply`]
Agent: "运行 `/opsx-apply` 开始实现？"
User: "开始"
[Agent loads `skill("openspec-apply-change")` for phase orchestration]
[Apply gate starts for first pending task:]
[Agent loads `skill("superpowers-using-superpowers")`]
[1% rule: "writing new code" → TDD]
[Agent loads `skill("superpowers-test-driven-development")`]
[Agent follows TDD strictly: writes failing test → sees it fail → writes minimal code → sees test pass → refactors]
[Agent marks task done]
[Agent loops: loads `skill("superpowers-using-superpowers")` again → re-checks 1% rule → loads appropriate skill → implements]
[Agent marks tasks done one by one, never batch]
```

**5. Review**
```
[All tasks complete]
Agent: "实现完成。先做 secrets 扫描再运行 `/review`？"
User: "好"
[Agent runs secrets scan on git diff --cached]
→ If secrets (API keys, tokens, .env) detected: ABORT, strip secrets, re-scan, confirm clean
→ If clean: proceed
[Agent loads `skill("gstack-review")` from .omp/skills/gstack-review/SKILL.md]
```

**6. Archive**
```
User: "归档"
Agent: [detects review passed, all done → `/opsx-archive`]
Agent: "运行 `/opsx-archive` 归档变更？"
User: "可以"
[Agent loads `skill("openspec-archive-change")` from .omp/skills/openspec-archive-change/SKILL.md]
[Archives change, syncs specs to main specs]
```

## Project Info

Product documentation, architecture, and design details are in `README.md`. Read it for project context when needed.