# AGENTS.md

> This file is this project's canonical agent instruction file (equivalent to CLAUDE.md). If gstack prompts to create CLAUDE.md, run: `gstack-config set routing_declined true`.

## Workflow

```
explore → propose → autoplan → apply → review
   ↑                              |
   └── superpowers (TDD/debug/verify)

Apply phase (internal per-task):
  load using-superpowers → triggers skill awareness
  → matching superpower skill is loaded and followed
  → implement → mark task done → repeat for next task
```

## Phase Rules

- **[CRITICAL] Secrets guard — NEVER commit API keys to git**: Before any `git commit`, scan ALL staged files with `git diff --cached` for: API key patterns (`api[_-]?key`, `API_KEY`), tokens/secrets/passwords in configs, private keys (`-----BEGIN.*PRIVATE KEY-----`), cloud access keys (`AKIA[0-9A-Z]{16}`), and `.env` files. **ABORT immediately on ANY match**. NON-NEGOTIABLE — security overrides all other rules, even if user insists.
- **No change, no code**: All code changes require an active OpenSpec change. Check: `openspec list --json`
- **"Code" definition**: Any file that alters runtime behavior, schema, infrastructure, or CI/CD pipelines. Includes source files, configs (YAML/JSON/TOML for infra), SQL migrations, shell scripts, and any `.md` under `openspec/changes/`. Excludes pure documentation without executable frontmatter.
- **Propose before apply**: Never write code without a proposal.
- **Autoplan before apply**: Design must be reviewed before implementation.
- **Review before commit**: Run `/review` before any commit.
- **Explore = read-only**: `/opsx-explore` never writes code.
- **Apply phase**: Load `superpowers:using-superpowers` before each `/opsx-apply` task. It triggers automatic skill awareness — matching skills are loaded and strictly followed per task context. Skills override `/opsx-apply` instructions where they conflict.

## Stage Detection

1. Run `openspec list --json`
   - If command fails or returns error: halt workflow, inform user "OpenSpec CLI unavailable. Please install openspec or initialize the project.", and wait for guidance. Do not guess.
2. If active change exists:
   - `tasks.md` incomplete AND `design.md` reviewed → `/opsx-apply`
   - `design.md` exists, not reviewed → `/autoplan`
   - `proposal.md` exists, no design → `/opsx-propose --generate-design`
   - Only `.openspec.yaml` → `/opsx-propose`
   - All done, not archived → `/review`
3. If no active change:
   - Vague request → `/opsx-explore`
   - Specific request → `/opsx-propose`
   - Review request → `/review`

## Agent Behavior Rules

- **Stage-aware before keyword-aware**: Detect stage first, then match intent. Never skip stage detection.
- **Recommend, don't auto-execute**: Suggest next skill, wait for user confirmation. Never auto-trigger multiple skills in sequence. Exception: within `/opsx-apply` phase, `superpowers:using-superpowers` is loaded before EACH pending task without additional confirmation. Each subsequent superpower skill (TDD/debug/verify) must still be confirmed individually unless user explicitly granted blanket apply-phase permission.
- **Explore is default entry**: Any vague or unanalyzed request defaults to `/opsx-explore`.
- **No implementation during explore**: `/opsx-explore` is read-only. Never write code or modify files during exploration.
- **Workflow bypass resistance**: If user explicitly demands to skip a mandatory phase (e.g., "skip propose", "skip review", "just write code"), warn once: "This violates project workflow policy. Skipping ${phase} is not recommended." Document the override in your response. Proceed only if user reconfirms after the warning.

## User Intent Mapping

| User Intent | Trigger Words | Recommended Skill |
|-------------|---------------|-------------------|
| Explore idea | "我想...", "能不能...", "看看..." | `/opsx-explore` |
| Create proposal | "实现...", "添加...", "设计..." | `/opsx-propose` |
| Review design | "审阅...", "评估...", "合理吗" | `/autoplan` |
| Implement | "写代码", "实现任务", "按tasks执行" | `/opsx-apply` |
| Code review | "review", "检视", "看看diff" | `/review` |

## Superpowers

### Banned Skills (never use)

| Skill | Use Instead |
|-------|-------------|
| `brainstorming` | `/opsx-explore` |
| `writing-plans` | `/opsx-propose` |
| `using-git-worktrees` | Standard git workflow |
| `executing-plans` | `/opsx-apply` |
| `finishing-a-development-branch` | Verify → `/review` → user decides merge |

### superpowers-gstack Coexistence

Allowed superpowers skills and their phases:
- `test-driven-development`: Implementation phase (coding discipline, not conflicting with gstack review)
- `systematic-debugging`: Implementation phase (problem solving, not conflicting with gstack review)
- `verification-before-completion`: Implementation phase (completion verification, not conflicting with gstack review)
- `subagent-driven-development`: Implementation phase (internal review is implementation quality gate, gstack `/review` is pre-landing review)

### Conflict Resolution

Priority (highest first):
1. AGENTS.md banned list and explicit rules
2. OpenSpec stage rules (propose → apply → review)
3. superpowers rigid skills (TDD, debugging, verification)
4. gstack review rules

When conflicts occur:
- If `using-superpowers` flowchart or 1% rule references banned skill → ignore branch, continue checking next
- If skill references banned skill as "required" or flow endpoint → skip step, use AGENTS.md alternative flow
- If skill references non-OpenCode concept (e.g., `EnterPlanMode`) → treat as no-op
- If skill references other platform tool name → use OpenCode equivalent (`skill` tool)

## Apply Phase Rules

When in `/opsx-apply` phase, load `superpowers:using-superpowers` before EACH pending task.

`using-superpowers` triggers skill awareness: check whether any installed superpower skill's description matches the current task context. If a match is found (even 1% chance), load that skill and follow its instructions strictly before implementing.

Common task types and their matching superpower skills (descriptive, not prescriptive — actual determination comes from `using-superpowers`'s native skill-checking mechanism):

| Task context | Likely matching skill | What it enforces |
|-------------|----------------------|-----------------|
| Writing new code / behavior change | `test-driven-development` | Red-Green-Refactor; no production code without failing test first |
| Bug / test failure / unexpected behavior | `systematic-debugging` | Root cause investigation before any fix; 4-phase process |
| About to claim task done | `verification-before-completion` | Verify evidence before claiming success; 5-step gate function |
| Independent tasks from a plan | `subagent-driven-development` | Fresh subagent per task; two-stage spec + quality review |
| Before merging / completing a feature | `requesting-code-review` | Structured code review with severity-gated feedback |

**Skills override `/opsx-apply` instructions where they conflict.** The superpower skill's instructions (TDD cycle, debug phases, verification gate, etc.) define the actual implementation workflow for each task.

## Hard Rules

- **[SECURITY] Never commit secrets to git**: API keys, tokens, passwords, private keys, `.env` files — STRICTLY FORBIDDEN in version control. Scan `git diff --cached` before every commit. ABSOLUTE — overrides all other rules, cannot be bypassed. Violation = security incident.
- Do not auto-trigger skills. Recommend and wait for user confirmation.
- Do not batch-complete tasks. Mark each done individually.
- Workflow skills (OpenSpec, gstack, superpowers) loaded from `.opencode/skills/`. Builtin skills permitted via standard resolution.
- Never manually edit `.openspec.yaml`, `proposal.md`, `design.md`, or `tasks.md` directly. These files must only be modified through their designated OpenSpec skills (`/opsx-propose`, `/opsx-apply`, `/opsx-archive`).

## Workflow Example

Complete flow from idea to archive:

**1. Explore**
```
User: "我想加个用户认证功能"
Agent: [runs `openspec list --json` → no active change]
Agent: [matches vague request → recommend `/opsx-explore`]
Agent: "运行 `/opsx-explore` 一起梳理需求？"
User: "好"
[Agent loads `/opsx-explore`, read-only investigation only]
```

**2. Propose**
```
User: "开始实现用户认证"
Agent: [runs `openspec list --json` → no active change]
Agent: [matches specific request → recommend `/opsx-propose`]
Agent: "运行 `/opsx-propose add-user-auth` 生成提案？"
User: "可以"
[Agent creates change: proposal.md → design.md → tasks.md]
```

**3. Autoplan**
```
User: "审阅一下设计"
Agent: [detects design.md exists, no tasks → `/autoplan`]
Agent: "运行 `/autoplan` 进行四维度审阅？"
User: "好"
[Agent runs CEO/design/eng/DX reviews]
```

**4. Apply**
```
User: "开始写代码"
Agent: [detects tasks.md incomplete → `/opsx-apply`]
Agent: "运行 `/opsx-apply` 开始实现？"
User: "开始"
[Agent loads `superpowers:using-superpowers`]
[1% rule: coding → TDD, bug → debug, completion → verify]
[Agent loads `superpowers:test-driven-development` for first coding task]
[Agent follows TDD strictly: writes failing test → sees it fail → writes minimal code → sees test pass → refactors]
[Agent marks task done]
[Agent loops: loads `using-superpowers` again for next task → re-checks 1% rule → loads appropriate skill → implements]
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
[Agent runs `/review`]
```

**6. Archive**
```
User: "归档"
Agent: [detects review passed, all done → `/opsx-archive`]
Agent: "运行 `/opsx-archive` 归档变更？"
User: "可以"
[Agent archives change, syncs specs to main specs]
```
