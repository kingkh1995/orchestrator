## ADDED Requirements

### Requirement: AgentCard endpoint serves hub metadata
The system SHALL expose a GET endpoint at `/.well-known/agent.json` that returns a valid A2A AgentCard JSON document describing the orchestrator hub.

#### Scenario: Client requests hub AgentCard
- **WHEN** an external client sends GET `/.well-known/agent.json`
- **THEN** the system responds with HTTP 200 and a JSON body containing `name`, `description`, `url`, `version`, `capabilities`, and `skills` fields

### Requirement: Task submission endpoint accepts A2A tasks
The system SHALL expose a POST endpoint at `/tasks/send` that accepts a valid A2A Task request and returns a Task response.

#### Scenario: Client submits a task
- **WHEN** an external client sends POST `/tasks/send` with a JSON body containing `id`, `sessionId`, and `message`
- **THEN** the system responds with HTTP 200 and a Task JSON with `status` set to `COMPLETED` and a mock `artifact`

### Requirement: Task status lifecycle follows A2A protocol
The system SHALL model Task status using the A2A standard states: `SUBMITTED`, `WORKING`, `INPUT_REQUIRED`, `COMPLETED`, `CANCELED`, `FAILED`.

#### Scenario: Mock task transitions to completed
- **WHEN** a task is submitted via `/tasks/send`
- **THEN** the returned Task status MUST be `COMPLETED` (in MVP; future versions will use `WORKING` → `COMPLETED`)

### Requirement: A2A protocol entities are properly modeled
The system SHALL define Java classes for core A2A entities: `AgentCard`, `Task`, `Message`, `Artifact`, `TaskStatus`, and `Part` (or reuse `a2a-java-sdk` entities).

#### Scenario: Entity serialization
- **WHEN** a Task object is serialized to JSON
- **THEN** it MUST conform to the A2A Protocol v1.0 JSON schema
