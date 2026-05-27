## ADDED Requirements

### Requirement: AgentCard Discovery
The A2A test page SHALL allow users to retrieve and view the orchestration hub's AgentCard.

#### Scenario: Fetch AgentCard on demand
- **WHEN** user clicks the "获取卡片" button
- **THEN** page sends GET request to `/.well-known/agent.json`
- **THEN** page displays the AgentCard JSON with syntax-highlighted formatting
- **THEN** page shows key fields (name, description, url, capabilities) in a structured table

#### Scenario: Display AgentCard loading state
- **WHEN** user clicks "获取卡片" and request is pending
- **THEN** button shows loading spinner and becomes disabled
- **THEN** button re-enables when request completes or fails

#### Scenario: Handle AgentCard network error
- **WHEN** the hub is unreachable
- **THEN** page displays "连接失败 — 请确认编排中心正在运行" in red
- **THEN** previous card content (if any) is not cleared

### Requirement: Task Submission
The A2A test page SHALL allow users to compose and submit A2A tasks via JSON-RPC.

#### Scenario: Submit task with session ID and message
- **WHEN** user enters a `session-id` and a `message` text
- **WHEN** user clicks "发送任务" button
- **THEN** page constructs a JSON-RPC 2.0 request with `method: "tasks/send"`
- **THEN** page sends POST request to `/tasks/send` with the JSON-RPC envelope
- **THEN** page displays the JSON-RPC response with syntax highlighting

#### Scenario: Auto-generate task ID
- **WHEN** user submits a task without manually specifying a task ID
- **THEN** page auto-generates a UUID using `crypto.randomUUID()` (or `Date.now()` fallback)
- **THEN** auto-generated ID is included in `params.id`

#### Scenario: Display JSON-RPC error responses
- **WHEN** the hub returns a JSON-RPC error response (with `error.code` and `error.message`)
- **THEN** page displays error code and message in red, distinct from successful responses
- **THEN** page shows the full error object for debugging

#### Scenario: Submit loading state
- **WHEN** user clicks "发送任务" and request is pending
- **THEN** button text changes to "发送中..." and becomes disabled
- **THEN** button re-enables when request completes or fails

### Requirement: Session History
The A2A test page SHALL persist session history across page reloads.

#### Scenario: Record task in history after submission
- **WHEN** a task is submitted (regardless of success or failure)
- **THEN** an entry is added to the history panel showing: session-id, truncated message, status, and timestamp
- **THEN** history entries are stored in `localStorage`

#### Scenario: Restore response from history
- **WHEN** user clicks a history entry
- **THEN** page shows the full request and response for that entry in the main response area
- **THEN** the entry is visually marked as selected

#### Scenario: Persist history across page reload
- **WHEN** user closes and reopens the page
- **THEN** all previous history entries are restored from `localStorage`
- **THEN** empty history shows placeholder text "暂无记录。请在上方发送任务。"

### Requirement: Page Routing
The A2A test page SHALL be accessible via the hub's root URL.

#### Scenario: Root URL redirect
- **WHEN** user visits `GET /` on the orchestration hub
- **THEN** server redirects or forwards to `/a2a-test.html`
- **THEN** page loads correctly with all features functional

#### Scenario: Direct URL access
- **WHEN** user visits `GET /a2a-test.html` directly
- **THEN** page loads correctly with all features functional
