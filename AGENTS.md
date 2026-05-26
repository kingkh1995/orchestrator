# AGENTS.md

> This file is this project's canonical agent instruction file (equivalent to CLAUDE.md). If gstack prompts to create CLAUDE.md, run: `gstack-config set routing_declined true`.

## Workflow

```
explore → propose → autoplan → apply → review
   ↑                              |
   └──────── superpowers (TDD/debug/verify)
```

## Phase Rules

- **No change, no code**: All code changes require an active OpenSpec change. Check: `openspec list --json`
- **"Code" definition**: Any file that alters runtime behavior, schema, infrastructure, or CI/CD pipelines. Includes source files, configs (YAML/JSON/TOML for infra), SQL migrations, shell scripts, and any `.md` under `openspec/changes/`. Excludes pure documentation without executable frontmatter.
- **Propose before apply**: Never write code without a proposal.
- **Autoplan before apply**: Design must be reviewed before implementation.
- **Review before commit**: Run `/review` before any commit.
- **Explore = read-only**: `/opsx-explore` never writes code.
- **Apply phase**: Must load `superpowers:using-superpowers` first. Its 1% rule determines which superpowers skill to load next.

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
- **Recommend, don't auto-execute**: Suggest next skill, wait for user confirmation. Never auto-trigger multiple skills in sequence. Exception: within `/opsx-apply` phase, after user confirms entry, `superpowers:using-superpowers` may be loaded once without additional confirmation. Each subsequent 1% rule skill (TDD/debug/verify) must still be confirmed individually unless user explicitly granted blanket apply-phase permission.
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

When in `/opsx-apply` phase:
1. Must load `superpowers:using-superpowers` first
2. Its 1% rule determines next skill:
   - Coding task → `test-driven-development`
   - Bug encountered → `systematic-debugging`
   - Agent development → `subagent-driven-development`
   - Claiming completion → `verification-before-completion`
3. If 1% rule references banned skill → reject and continue checking
4. Banned skills must be rejected even if flowchart suggests them

## Hard Rules

- Do not auto-trigger skills. Recommend and wait for user confirmation.
- Do not batch-complete tasks. Mark each done individually.
- TDD in apply phase: failing test before any code change.
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
[Agent marks tasks done one by one, never batch]
```

**5. Review**
```
[All tasks complete]
Agent: "实现完成。运行 `/review` 做预着陆检视？"
User: "好"
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
