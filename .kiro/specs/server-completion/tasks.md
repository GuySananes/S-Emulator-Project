# Server Implementation Completion Tasks

- [x] 1. Fix core engine management and create base servlet infrastructure





  - [x] 1.1 Update AppContext to include EngineRegistry


    - Add `private static final EngineRegistry ENGINES = new EngineRegistry();` to AppContext.java
    - Add `public static EngineRegistry engines() { return ENGINES; }` method to AppContext.java
    - EngineRegistry already exists in server/src/sserver/registry/EngineRegistry.java
    - _Requirements: 2.1, 2.2, 2.3, 2.4_



  - [x] 1.2 Create BaseServlet with common session engine logic





    - Create server/src/sserver/api/BaseServlet.java extending HttpServlet
    - Add getSessionIdFromCookie() method (copy from existing servlets)
    - Add getSessionEngine() method using AppContext.engines().getOrCreateEngine()
    - Add checkAuthentication() helper method
    - Add standardized error response methods


    - _Requirements: 2.1, 2.2, 6.1, 6.2, 6.3, 6.4, 6.5_

  - [x] 1.3 Create standardized ErrorResponse class





    - Create server/src/sserver/api/dto/ErrorResponse.java (ErrorResponse already exists)
    - Enhance existing ErrorResponse with type and details fields if needed
    - Add static factory methods for different error types
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 2. Fix existing servlets to use session-based engines





  - [x] 2.1 Update ProgramSelectServlet to use session engines


    - Modify server/src/sserver/api/ProgramSelectServlet.java
    - Change from Engine.getInstance() to getSessionEngine() from BaseServlet
    - Update to extend BaseServlet instead of HttpServlet
    - Store LoadProgramDTO in EngineContext.loadedProgramDTO after loading
    - _Requirements: 2.1, 4.1, 7.1, 7.2_

  - [x] 2.2 Update ChooseFunctionServlet to use session engines  


    - Modify server/src/sserver/api/ChooseFunctionServlet.java
    - Change from Engine.getInstance() to getSessionEngine() from BaseServlet
    - Update to extend BaseServlet instead of HttpServlet
    - Fix context switching using session engine
    - _Requirements: 2.2, 4.2, 4.3, 4.4_

  - [x] 2.3 Update DebugServlet to use session engines


    - Modify server/src/sserver/api/DebugServlet.java
    - Change to use session-based engine from EngineRegistry
    - Update to extend BaseServlet instead of HttpServlet
    - Fix debug session creation using session engine
    - _Requirements: 2.3, 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix ExecuteServlet for actual program execution




  - [x] 3.1 Fix existing ExecuteServlet to actually execute programs


    - Modify server/src/sserver/api/ExecuteServlet.java
    - Change from returning program data to executing programs
    - Use session engine to get ExecuteProgramDTO via engine.executeProgram()
    - Get RunProgramDTO, set input values from request and call runProgram()
    - Return result and cycle count instead of program data
    - Update to extend BaseServlet and use session engines
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 4. Add missing API endpoints (check if any servlets already exist)





  - [x] 4.1 Check if ExpandServlet exists, create or fix expand functionality


    - Look for existing expand endpoint in current servlets
    - If missing, create servlet at `/api/execution/expand` endpoint
    - Use session engine to call expandOrShrinkProgram(currentDegree + 1)
    - Return updated instructions and variables
    - _Requirements: 5.1_

  - [x] 4.2 Check if CollapseServlet exists, create or fix collapse functionality


    - Look for existing collapse endpoint in current servlets
    - If missing, create servlet at `/api/execution/collapse` endpoint  
    - Use session engine to call expandOrShrinkProgram(currentDegree - 1)
    - Return updated instructions and variables
    - _Requirements: 5.2_

  - [x] 4.3 Check if RerunServlet exists, create or fix rerun functionality


    - Look for existing rerun endpoint in current servlets
    - If missing, create servlet at `/api/execution/rerun` endpoint
    - Use session engine to call reExecuteProgram(runNumber)
    - Handle input collection and program re-execution
    - _Requirements: 5.3_

  - [x] 4.4 Fix existing StatisticsServlet to use session engines


    - Modify server/src/sserver/api/StatisticsServlet.java
    - Update to extend BaseServlet and use session-based engines
    - Use session engine to call presentProgramStats()
    - Format and return program statistics properly
    - _Requirements: 5.4_

- [x] 5. Enhance EngineContext for session state management






  - [x] 5.1 Add current degree tracking to EngineContext

    - Modify server/src/sserver/registry/EngineRegistry.java
    - Add currentDegree field to EngineRegistry.EngineContext class
    - Update expand/collapse operations to track degree
    - _Requirements: 7.1, 7.2_


  - [x] 5.2 Add program loading state to EngineContext

    - EngineContext already has loadedProgramDTO field
    - Ensure LoadProgramDTO is stored in EngineContext when programs are loaded
    - Add helper methods to check if program is loaded
    - _Requirements: 7.1, 7.2_
- [x] 6. Implement session cleanup and resource management












- [ ] 6. Implement session cleanup and resource management

  - [x] 6.1 Add session cleanup to LogoutServlet



    - Modify server/src/sserver/api/LogoutServlet.java
    - Update LogoutServlet to call AppContext.engines().removeEngine(sessionId)
    - Clean up associated execution contexts from ExecutionRegistry
    - _Requirements: 2.4, 7.3_

  - [x] 6.2 Add automatic session cleanup



    - Modify server/src/sserver/ctx/AppBootstrapListener.java
    - Implement periodic cleanup of stale engine contexts
    - Add cleanup call using EngineRegistry.cleanupStaleEngines()
    - _Requirements: 2.4, 7.4_
-

- [x] 7. Update error handling across all servlets





  - [x] 7.1 Standardize exception handling in all servlets





    - Update all servlets to use BaseServlet error handling methods
    - Map XMLUnmarshalException to HTTP 400 validation errors
    - Map ProgramValidationException to HTTP 400 validation errors
    - Map engine exceptions to HTTP 500 execution errors
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_


  - [x] 7.2 Add comprehensive error logging





    - Add proper logging for all error conditions
    - Include session ID and operation context in logs
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
- [x] 8. Test and validate the implementation









- [ ] 8. Test and validate the implementation


  - [x] 8.1 Test session isolation between multiple users






    - Verify that different sessions have independent engine states
    - Test concurrent program loading and execution
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 8.2 Test complete execution workflows




  - [x] 8.2 Test complete execution workflows



    - Test regular execution flow end-to-end
    - Test debug execution flow with step/resume operations
    - Test expand/collapse operations
    - Test rerun functionality
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 3.1, 
3.2, 3.3, 3.4, 5.1, 5.2, 5.3, 5.4_









  - [x] 8.3 Test error handling and edge cases




    - Test authentication failures
    - Test invalid program operations
    - Test session expiration scenarios
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_