## ADDED Requirements

### Requirement: OrchestrationEngine is defined
The system SHALL define a single `OrchestrationEngine` interface that encapsulates the ReAct loop logic (planning, scheduling, aggregating, re-planning) for MVP.

#### Scenario: Engine instantiation
- **WHEN** the Spring context initializes
- **THEN** an `OrchestrationEngine` bean MUST be registered

### Requirement: OrchestrationEngine generates mock DAG in MVP
In MVP, the OrchestrationEngine SHALL accept a `SessionContext` and return a static mock DAG (Directed Acyclic Graph) containing a single node representing direct completion.

#### Scenario: Engine generates mock plan
- **WHEN** the OrchestrationEngine receives a `SessionContext`
- **THEN** it returns a DAG with one task node and no dependencies

### Requirement: AgentScope-Java integration is configured
The system SHALL configure an `OpenAIChatModel` bean pointing to Opencode Zen with the DeepSeek V4 Flash Free model.

#### Scenario: LLM model bean is available
- **WHEN** the Spring context initializes
- **THEN** an `OpenAIChatModel` bean MUST be available with `baseUrl=https://opencode.ai/zen/v1/chat/completions` and `modelName=deepseek-v4-flash-free`
