## ADDED Requirements

### Requirement: OrchestrationEngine is defined
The system SHALL define a single `OrchestrationEngine` interface that encapsulates the ReAct loop logic (planning, scheduling, aggregating, re-planning) for MVP.

#### Scenario: Engine instantiation
- **WHEN** the Spring context initializes
- **THEN** an `OrchestrationEngine` bean MUST be registered (unconditional, no conditional property needed)

### Requirement: OrchestrationEngine delegates to ReActAgent
The `ReActOrchestrationEngine` SHALL delegate execution to AgentScope-Java's `ReActAgent` (the only engine implementation). The engine does not depend on `OrchLLMProperties` — timeout is injected as `Duration` at construction.

#### Scenario: Engine executes plan via ReActAgent
- **WHEN** the OrchestrationEngine receives a `SessionContext`
- **THEN** it delegates to the injected `Agent.call()` to produce a text result

### Requirement: Model bean is config-driven
The system SHALL provide an `OpenAIChatModel` bean configured entirely from `application.yml`, not from hardcoded constants.

#### Scenario: LLM model bean is available
- **WHEN** the Spring context initializes
- **THEN** a `Model` bean MUST be available with configuration bound from `orch.llm.*` properties (base-url, endpoint-path, model, api-key, timeout, max-retries)
