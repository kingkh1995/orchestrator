## ADDED Requirements

### Requirement: Session context tracks task state
The system SHALL maintain a `SessionContext` object that tracks all tasks, their states, and intermediate results within a session.

#### Scenario: Session context creation
- **WHEN** a new task is submitted without an existing session
- **THEN** the system creates a new `SessionContext` with a generated `sessionId`

### Requirement: Session snapshot mechanism is defined
The system SHALL define a `SessionSnapshot` concept for checkpointing session state to enable fault recovery.

#### Scenario: Snapshot structure
- **WHEN** a snapshot is requested
- **THEN** it contains the sessionId, current task states, and last event offset

### Requirement: Event types are defined
The system SHALL define the five core event types: `OrchestrationDecisionEvent`, `TaskCommandEvent`, `TaskProgressEvent`, `TaskSummaryEvent`, and `SessionSnapshotEvent`.

#### Scenario: Event type enumeration
- **WHEN** the system processes an internal event
- **THEN** it MUST be one of the five defined event types

### Requirement: Session event stream supports replay
The system SHALL support replaying events from a given offset to reconstruct session state.

#### Scenario: Event replay
- **WHEN** a session is recovered from a snapshot at offset 100
- **THEN** the system replays all events from offset 100 onwards to restore full context
