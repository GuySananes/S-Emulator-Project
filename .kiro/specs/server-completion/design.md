# Server Implementation Completion Design

## Overview

This design addresses the gaps and issues in the current S-Emulator server implementation. The server correctly uses the existing engine/DTO/exception modules but needs fixes to properly manage per-session engines, complete missing functionality, and provide consistent error handling.

## Architecture

### Current System Analysis
The existing server has these components working correctly:
- `AppContext` - Provides singleton access to registries
- `ProgramsRegistry` - Manages uploaded programs using Engine
- `SessionRegistry` - Manages user sessions
- `LoginServlet`, `FileLoadServlet`, `ProgramsServlet` - Working correctly
- Basic servlet structure with proper authentication

### Issues Identified
1. **Engine Management**: Servlets use `Engine.getInstance()` instead of per-session engines
2. **Execution Flow**: `ExecuteServlet` returns program data instead of executing
3. **Missing Endpoints**: No expand/collapse/rerun/statistics/regular execution endpoints
4. **Session Integration**: EngineRegistry exists but isn't used by servlets
5. **Error Handling**: Inconsistent error response formats

### Enhanced Architecture
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Request  │───▶│  Authentication  │───▶│ Session Engine  │
│                 │    │   & Validation   │    │   Resolution    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Engine Operation│    │ Response Builder │    │ Error Handler   │
│  (per session)  │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Components and Interfaces

### 1. Session Engine Management

**Problem**: Servlets use global `Engine.getInstance()` instead of per-session engines.

**Solution**: Update servlets to use `AppContext.engines().getOrCreateEngine(sessionId)`

```java
// Current (incorrect):
Engine engine = Engine.getInstance();

// Fixed:
String sessionId = getSessionIdFromCookie(req);
EngineRegistry.EngineContext engineCtx = AppContext.engines().getOrCreateEngine(sessionId);
Engine engine = engineCtx.engine;
```

### 2. Fixed ExecuteServlet

**Problem**: Returns program data instead of executing programs.

**Solution**: Implement actual program execution flow:

```java
@WebServlet(name="ExecuteServlet", urlPatterns="/api/execution/regular")
public class ExecuteServlet extends HttpServlet {
    // POST /api/execution/regular
    // - Get session engine
    // - Create ExecuteProgramDTO via engine.executeProgram()
    // - Get RunProgramDTO
    // - Set input values from request
    // - Call runProgram()
    // - Return result and cycles
}
```

### 3. New Missing Endpoints

**Expand/Collapse Operations**:
```java
@WebServlet(name="ExpandServlet", urlPatterns="/api/execution/expand")
// - Call engine.expandOrShrinkProgram(currentDegree + 1)
// - Return updated instructions and variables

@WebServlet(name="CollapseServlet", urlPatterns="/api/execution/collapse")  
// - Call engine.expandOrShrinkProgram(currentDegree - 1)
// - Return updated instructions and variables
```

**Statistics and Rerun**:
```java
@WebServlet(name="StatisticsServlet", urlPatterns="/api/execution/statistics")
// - Call engine.presentProgramStats()
// - Return formatted statistics

@WebServlet(name="RerunServlet", urlPatterns="/api/execution/rerun")
// - Call engine.reExecuteProgram(runNumber)
// - Execute with original or new inputs
// - Return execution results
```

### 4. Enhanced Error Handling

**Standardized Error Response**:
```java
public class ErrorResponse {
    public String error;
    public String type;    // "validation", "execution", "authentication"
    public String details; // Additional context
    
    public static ErrorResponse validation(String message) {
        return new ErrorResponse("validation", message, null);
    }
    
    public static ErrorResponse execution(String message, String details) {
        return new ErrorResponse("execution", message, details);
    }
}
```

**Exception Mapping**:
- `XMLUnmarshalException` → HTTP 400, type: "validation"
- `ProgramValidationException` → HTTP 400, type: "validation"  
- `Engine` exceptions → HTTP 500, type: "execution"
- Authentication failures → HTTP 401, type: "authentication"

### 5. Updated Servlet Base Class

**Common Functionality**:
```java
public abstract class BaseServlet extends HttpServlet {
    protected final Gson gson = new Gson();
    
    protected String getSessionIdFromCookie(HttpServletRequest req) { ... }
    
    protected EngineRegistry.EngineContext getSessionEngine(HttpServletRequest req) {
        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) return null;
        return AppContext.engines().getOrCreateEngine(sessionId);
    }
    
    protected void sendError(HttpServletResponse resp, int status, String type, String message) {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(ErrorResponse.of(type, message)));
    }
    
    protected boolean checkAuthentication(HttpServletRequest req, HttpServletResponse resp) {
        // Common auth check logic
    }
}
```

## Implementation Strategy

### Phase 1: Fix Core Engine Management
1. Update `AppContext` to include `EngineRegistry`
2. Create `BaseServlet` with common session engine logic
3. Update existing servlets to extend `BaseServlet`
4. Fix `ProgramSelectServlet` and `ChooseFunctionServlet` to use session engines

### Phase 2: Fix Execution Flow
1. Rewrite `ExecuteServlet` to actually execute programs
2. Fix `DebugServlet` to use session engines properly
3. Update `ExecutionRegistry` integration

### Phase 3: Add Missing Endpoints
1. Create `ExpandServlet` and `CollapseServlet`
2. Create `StatisticsServlet` and `RerunServlet`
3. Add proper error handling to all endpoints

### Phase 4: Testing and Integration
1. Test session isolation between users
2. Test all execution flows (regular, debug, expand, etc.)
3. Verify error handling consistency
4. Test session cleanup on logout

## Data Flow Examples

### Program Selection Flow
```
1. POST /api/execution/select-program {"programName": "MyProgram"}
2. Get session engine from EngineRegistry
3. Load program: engine.loadProgram(filePath)
4. Store LoadProgramDTO in EngineContext
5. Return program data (instructions, variables, context functions)
```

### Regular Execution Flow
```
1. POST /api/execution/regular {"inputs": [1, 2, 3]}
2. Get session engine and verify program loaded
3. Create execution: engine.executeProgram()
4. Set inputs and run: runDTO.setInput(inputs); runDTO.runProgram()
5. Return result and cycles
```

### Debug Flow
```
1. POST /api/debug/start {"inputs": [1, 2, 3]}
2. Create debug session with session engine
3. POST /api/debug/{sessionId}/step (multiple times)
4. Each step returns current state and changed variables
5. POST /api/debug/{sessionId}/resume to complete
```

This design maintains the existing correct architecture while fixing the identified issues and adding missing functionality to match the JavaFX client capabilities.