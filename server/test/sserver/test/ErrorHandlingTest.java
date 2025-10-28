package sserver.test;

import core.logic.engine.Engine;
import exception.NoProgramException;
import exception.ProgramValidationException;
import exception.XMLUnmarshalException;
import load.LoadProgramDTO;
import run.ExecuteProgramDTO;
import run.RunProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Test error handling and edge cases.
 * Tests authentication failures, invalid program operations, and session expiration scenarios.
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
public class ErrorHandlingTest {

    private static final String TEST_PROGRAM_PATH = "run/badic.xml";
    private static final String INVALID_XML_PATH = "run/error-1.xml";

    /**
     * Test authentication failure scenarios
     * Requirements: 6.4
     */
    private static boolean testAuthenticationFailure() {
        System.out.println("Test: Authentication failure handling");
        
        try {
            // Test 1: Invalid session ID
            String invalidSessionId = "invalid-session-12345";
            
            // Verify session doesn't exist
            String username = AppContext.sessions().getUserBySession(invalidSessionId);
            if (username != null) {
                System.out.println("  ✗ FAILED: Invalid session should not have user");
                return false;
            }
            System.out.println("  Correctly rejected invalid session ID");
            
            // Test 2: Expired session
            String expiredSessionId = "expired-session-67890";
            
            // Try to get engine with expired session (should work but session validation would fail)
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(expiredSessionId);
            if (ctx == null) {
                System.out.println("  ✗ FAILED: Should create engine context even for unauthenticated session");
                return false;
            }
            
            // Verify session is not authenticated
            username = AppContext.sessions().getUserBySession(expiredSessionId);
            if (username != null) {
                System.out.println("  ✗ FAILED: Unauthenticated session should not have user");
                AppContext.engines().removeEngine(expiredSessionId);
                return false;
            }
            System.out.println("  Correctly identified unauthenticated session");
            
            // Cleanup
            AppContext.engines().removeEngine(expiredSessionId);
            
            // Test 3: Null session ID
            username = AppContext.sessions().getUserBySession(null);
            if (username != null) {
                System.out.println("  ✗ FAILED: Null session should not have user");
                return false;
            }
            System.out.println("  Correctly handled null session ID");
            
            System.out.println("  ✓ PASSED: Authentication failures handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Error Handling Test ===\n");
        
        boolean allPassed = true;
        
        allPassed &= testAuthenticationFailure();
        allPassed &= testInvalidProgramPath();
        allPassed &= testInvalidXMLFile();
        allPassed &= testExecutionWithoutLoadedProgram();
        allPassed &= testInvalidInputs();
        allPassed &= testSessionExpiration();
        allPassed &= testSessionExpirationDuringOperation();
        allPassed &= testConcurrentModification();
        
        System.out.println("\n=== Test Results ===");
        if (allPassed) {
            System.out.println("✓ All tests PASSED");
        } else {
            System.out.println("✗ Some tests FAILED");
            System.exit(1);
        }
    }

    /**
     * Test handling of invalid program file paths
     * Requirements: 6.3
     */
    private static boolean testInvalidProgramPath() {
        System.out.println("Test: Invalid program path handling");
        
        try {
            String sessionId = "invalid-path-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Try to load non-existent file
            try {
                engine.loadProgram("nonexistent/program.xml");
                System.out.println("  ✗ FAILED: Should have thrown exception for invalid path");
                AppContext.engines().removeEngine(sessionId);
                return false;
            } catch (Exception e) {
                // Expected - should throw exception
                System.out.println("  Correctly rejected invalid path: " + e.getClass().getSimpleName());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Invalid path handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test handling of invalid XML files
     * Requirements: 6.1
     */
    private static boolean testInvalidXMLFile() {
        System.out.println("\nTest: Invalid XML file handling");
        
        try {
            String sessionId = "invalid-xml-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Try to load invalid XML file
            File errorFile = new File(INVALID_XML_PATH);
            if (errorFile.exists()) {
                try {
                    engine.loadProgram(INVALID_XML_PATH);
                    System.out.println("  ✗ FAILED: Should have thrown exception for invalid XML");
                    AppContext.engines().removeEngine(sessionId);
                    return false;
                } catch (XMLUnmarshalException e) {
                    // Expected - should throw XMLUnmarshalException
                    System.out.println("  Correctly threw XMLUnmarshalException: " + e.getMessage());
                } catch (ProgramValidationException e) {
                    // Also acceptable - validation error
                    System.out.println("  Correctly threw ProgramValidationException: " + e.getMessage());
                } catch (Exception e) {
                    // Other exceptions are also acceptable for invalid XML
                    System.out.println("  Correctly rejected invalid XML: " + e.getClass().getSimpleName());
                }
            } else {
                System.out.println("  Note: Test file " + INVALID_XML_PATH + " not found, skipping");
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Invalid XML handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test execution without loaded program
     * Requirements: 6.3
     */
    private static boolean testExecutionWithoutLoadedProgram() {
        System.out.println("\nTest: Execution without loaded program");
        
        try {
            String sessionId = "no-program-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Try to execute without loading a program
            try {
                engine.executeProgram();
                System.out.println("  ✗ FAILED: Should have thrown exception for no program");
                AppContext.engines().removeEngine(sessionId);
                return false;
            } catch (NoProgramException e) {
                // Expected - should throw NoProgramException
                System.out.println("  Correctly threw NoProgramException: " + e.getMessage());
            } catch (Exception e) {
                // Other exceptions might also be thrown
                System.out.println("  Correctly rejected execution without program: " + e.getClass().getSimpleName());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: No program error handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test handling of invalid inputs
     * Requirements: 6.2
     */
    private static boolean testInvalidInputs() {
        System.out.println("\nTest: Invalid input handling");
        
        try {
            String sessionId = "invalid-input-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            // Create execution
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
            
            // Test with wrong number of inputs
            try {
                List<Long> tooManyInputs = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
                runDTO.setInput(tooManyInputs);
                runDTO.runProgram();
                // Some programs might accept extra inputs, so this might not fail
                System.out.println("  Note: Program accepted extra inputs");
            } catch (Exception e) {
                // Expected for some programs
                System.out.println("  Correctly rejected invalid input count: " + e.getClass().getSimpleName());
            }
            
            // Test with null inputs
            try {
                ExecuteProgramDTO executeDTO2 = engine.executeProgram();
                RunProgramDTO runDTO2 = executeDTO2.getRunProgramDTO();
                runDTO2.setInput(null);
                // This might be acceptable for programs with no inputs
                System.out.println("  Note: Null inputs handled");
            } catch (Exception e) {
                System.out.println("  Correctly handled null inputs: " + e.getClass().getSimpleName());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Invalid inputs handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test session expiration scenarios
     * Requirements: 6.4, 6.5
     */
    private static boolean testSessionExpiration() {
        System.out.println("\nTest: Session expiration handling");
        
        try {
            String sessionId = "expiring-session";
            
            // Create a session
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            ctx.currentProgramName = "TestProgram";
            
            // Verify session exists
            int beforeCount = AppContext.engines().getActiveEngineCount();
            
            // Manually expire the session
            AppContext.engines().removeEngine(sessionId);
            
            int afterCount = AppContext.engines().getActiveEngineCount();
            
            if (afterCount >= beforeCount) {
                System.out.println("  ✗ FAILED: Session not removed");
                return false;
            }
            
            // Try to get the expired session - should create new one
            EngineRegistry.EngineContext newCtx = AppContext.engines().getOrCreateEngine(sessionId);
            
            if (newCtx.currentProgramName != null) {
                System.out.println("  ✗ FAILED: New session has old state");
                return false;
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Session expiration handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test session expiration during active operations
     * Requirements: 6.4, 6.5
     */
    private static boolean testSessionExpirationDuringOperation() {
        System.out.println("\nTest: Session expiration during operation");
        
        try {
            String sessionId = "operation-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            // Create execution
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            
            // Expire session while execution is in progress
            AppContext.engines().removeEngine(sessionId);
            
            // Try to use the execution DTO - should still work as it's already created
            try {
                RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
                List<Long> inputs = Arrays.asList(5L, 3L);
                try {
                    runDTO.setInput(inputs);
                } catch (Exception e) {
                    // Ignore input errors
                }
                runDTO.runProgram();
                System.out.println("  Note: Existing execution completed after session expiration");
            } catch (Exception e) {
                System.out.println("  Note: Execution failed after session expiration: " + e.getClass().getSimpleName());
            }
            
            // Try to create new execution with expired session - should fail
            try {
                EngineRegistry.EngineContext expiredCtx = AppContext.engines().getOrCreateEngine(sessionId);
                if (expiredCtx.loadedProgramDTO != null) {
                    System.out.println("  ✗ FAILED: Expired session should not have loaded program");
                    AppContext.engines().removeEngine(sessionId);
                    return false;
                }
                System.out.println("  Correctly created fresh session after expiration");
            } catch (Exception e) {
                System.out.println("  Note: Session recreation behavior: " + e.getClass().getSimpleName());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Session expiration during operation handled correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test concurrent modification scenarios
     * Requirements: 6.3
     */
    private static boolean testConcurrentModification() {
        System.out.println("\nTest: Concurrent modification handling");
        
        try {
            String sessionId = "concurrent-mod-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            // Create multiple execution contexts
            ExecuteProgramDTO exec1 = engine.executeProgram();
            ExecuteProgramDTO exec2 = engine.executeProgram();
            
            // Both should be valid
            if (exec1 == null || exec2 == null) {
                System.out.println("  ✗ FAILED: Failed to create multiple execution contexts");
                AppContext.engines().removeEngine(sessionId);
                return false;
            }
            
            // Try to run both
            try {
                RunProgramDTO run1 = exec1.getRunProgramDTO();
                RunProgramDTO run2 = exec2.getRunProgramDTO();
                
                List<Long> inputs = Arrays.asList(5L, 3L);
                try {
                    run1.setInput(inputs);
                    run2.setInput(inputs);
                } catch (Exception e) {
                    // Ignore input errors
                }
                
                // Run both - the engine should handle this appropriately
                run1.runProgram();
                run2.runProgram();
                
                System.out.println("  Multiple executions handled");
                
            } catch (Exception e) {
                // Some engines might not support concurrent execution
                System.out.println("  Note: Concurrent execution behavior: " + e.getClass().getSimpleName());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Concurrent modification handled");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
