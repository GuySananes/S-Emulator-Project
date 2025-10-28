# Server Implementation Completion Requirements

## Introduction

This spec addresses completion and fixes needed for the S-Emulator server implementation. The server correctly uses existing engine/DTO/exception modules but has several gaps and issues that need to be resolved to match the functionality provided by the JavaFX client.

## Glossary

- **Engine**: The core S-language execution engine from the engine module
- **ExecuteServlet**: HTTP endpoint for program execution
- **DebugServlet**: HTTP endpoint for debug operations  
- **ProgramSelectServlet**: HTTP endpoint for selecting programs
- **ChooseFunctionServlet**: HTTP endpoint for selecting context functions
- **EngineRegistry**: Per-session engine management
- **ExecutionRegistry**: Execution context management

## Requirements

### Requirement 1: Fix Program Execution Flow

**User Story:** As a web client user, I want to execute S-programs through the API so that I can run programs remotely.

#### Acceptance Criteria

1. WHEN a user calls `/api/execute/start` with a valid program ID, THE ExecuteServlet SHALL use the per-session Engine to execute the program
2. WHEN executing a program, THE ExecuteServlet SHALL collect input values from the request and pass them to the engine
3. WHEN program execution completes, THE ExecuteServlet SHALL return the result and cycle count
4. WHEN program execution fails, THE ExecuteServlet SHALL return appropriate error messages using existing exception types

### Requirement 2: Fix Session-Based Engine Management

**User Story:** As a server administrator, I want each user session to have its own Engine instance so that multiple users can work independently.

#### Acceptance Criteria

1. WHEN a user selects a program, THE ProgramSelectServlet SHALL use the session-specific Engine from EngineRegistry
2. WHEN a user chooses a function, THE ChooseFunctionServlet SHALL use the session-specific Engine from EngineRegistry  
3. WHEN a user starts debugging, THE DebugServlet SHALL use the session-specific Engine from EngineRegistry
4. WHEN a session ends, THE EngineRegistry SHALL clean up the associated Engine instance

### Requirement 3: Complete Debug Implementation

**User Story:** As a web client user, I want to debug S-programs step-by-step so that I can understand program execution.

#### Acceptance Criteria

1. WHEN starting debug mode, THE DebugServlet SHALL create a DebugProgramDTO using the session Engine
2. WHEN stepping through debug, THE DebugServlet SHALL call nextStep() and return current state
3. WHEN resuming debug, THE DebugServlet SHALL call runUntilEnd() and return final result
4. WHEN debug completes, THE DebugServlet SHALL update ExecutionRegistry with final results

### Requirement 4: Fix Program Selection and Context Switching

**User Story:** As a web client user, I want to select programs and switch between functions so that I can work with different parts of my code.

#### Acceptance Criteria

1. WHEN selecting a program, THE ProgramSelectServlet SHALL load the program into the session Engine
2. WHEN choosing a function, THE ChooseFunctionServlet SHALL call engine.chooseContextProgram()
3. WHEN switching contexts, THE servlets SHALL return updated instruction and variable lists
4. WHEN context switching fails, THE servlets SHALL return appropriate error messages

### Requirement 5: Add Missing API Endpoints

**User Story:** As a web client user, I want access to all program operations so that I have the same functionality as the desktop client.

#### Acceptance Criteria

1. THE server SHALL provide `/api/execution/expand` endpoint for program expansion
2. THE server SHALL provide `/api/execution/collapse` endpoint for program collapse  
3. THE server SHALL provide `/api/execution/rerun` endpoint for re-executing previous runs
4. THE server SHALL provide `/api/execution/statistics` endpoint for program statistics
5. THE server SHALL provide `/api/execution/regular` endpoint for regular (non-debug) execution

### Requirement 6: Improve Error Handling

**User Story:** As a web client developer, I want consistent error responses so that I can handle errors properly.

#### Acceptance Criteria

1. WHEN XMLUnmarshalException occurs, THE servlets SHALL return HTTP 400 with structured error response
2. WHEN ProgramValidationException occurs, THE servlets SHALL return HTTP 400 with validation details
3. WHEN Engine operations fail, THE servlets SHALL return HTTP 500 with error details
4. WHEN authentication fails, THE servlets SHALL return HTTP 401 with unauthorized message
5. ALL error responses SHALL use consistent JSON structure with error field

### Requirement 7: Session Management Integration

**User Story:** As a web client user, I want my program state to persist during my session so that I can continue working.

#### Acceptance Criteria

1. WHEN a user loads a program, THE system SHALL store the LoadProgramDTO in the session EngineContext
2. WHEN a user switches programs, THE system SHALL update the session EngineContext
3. WHEN a user logs out, THE system SHALL clean up the session EngineContext
4. WHEN session expires, THE system SHALL automatically clean up associated Engine resources