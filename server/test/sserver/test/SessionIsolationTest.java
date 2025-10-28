package sserver.test;

import core.logic.engine.Engine;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

/**
 * Test session isolation between multiple users.
 * Verifies that different sessions have independent engine states.
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
public class SessionIsolationTest {

    public static void main(String[] args) {
        System.out.println("=== Session Isolation Test ===\n");
        
        boolean allPassed = true;
        
        allPassed &= testDifferentSessionsGetDifferentEngines();
        allPassed &= testSessionEngineStateIsolation();
        allPassed &= testConcurrentSessionAccess();
        allPassed &= testSessionCleanup();
        
        System.out.println("\n=== Test Results ===");
        if (allPassed) {
            System.out.println("✓ All tests PASSED");
        } else {
            System.out.println("✗ Some tests FAILED");
            System.exit(1);
        }
    }

    /**
     * Test that different sessions get different Engine instances
     */
    private static boolean testDifferentSessionsGetDifferentEngines() {
        System.out.println("Test: Different sessions get different engines");
        
        try {
            String session1 = "session-1";
            String session2 = "session-2";
            
            EngineRegistry.EngineContext ctx1 = AppContext.engines().getOrCreateEngine(session1);
            EngineRegistry.EngineContext ctx2 = AppContext.engines().getOrCreateEngine(session2);
            
            if (ctx1 == ctx2) {
                System.out.println("  ✗ FAILED: Same context returned for different sessions");
                return false;
            }
            
            if (ctx1.engine == ctx2.engine) {
                System.out.println("  ✗ FAILED: Same engine instance for different sessions");
                return false;
            }
            
            // Cleanup
            AppContext.engines().removeEngine(session1);
            AppContext.engines().removeEngine(session2);
            
            System.out.println("  ✓ PASSED: Different sessions have different engines");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test that engine state is isolated between sessions
     */
    private static boolean testSessionEngineStateIsolation() {
        System.out.println("\nTest: Engine state isolation between sessions");
        
        try {
            String session1 = "session-state-1";
            String session2 = "session-state-2";
            
            EngineRegistry.EngineContext ctx1 = AppContext.engines().getOrCreateEngine(session1);
            EngineRegistry.EngineContext ctx2 = AppContext.engines().getOrCreateEngine(session2);
            
            // Set different program names
            ctx1.currentProgramName = "Program1";
            ctx2.currentProgramName = "Program2";
            
            // Set different degrees
            ctx1.currentDegree = 1;
            ctx2.currentDegree = 2;
            
            // Verify isolation
            if (ctx1.currentProgramName.equals(ctx2.currentProgramName)) {
                System.out.println("  ✗ FAILED: Program names not isolated");
                return false;
            }
            
            if (ctx1.currentDegree == ctx2.currentDegree) {
                System.out.println("  ✗ FAILED: Degrees not isolated");
                return false;
            }
            
            // Verify values are correct
            if (!ctx1.currentProgramName.equals("Program1") || ctx1.currentDegree != 1) {
                System.out.println("  ✗ FAILED: Session 1 state corrupted");
                return false;
            }
            
            if (!ctx2.currentProgramName.equals("Program2") || ctx2.currentDegree != 2) {
                System.out.println("  ✗ FAILED: Session 2 state corrupted");
                return false;
            }
            
            // Cleanup
            AppContext.engines().removeEngine(session1);
            AppContext.engines().removeEngine(session2);
            
            System.out.println("  ✓ PASSED: Engine state properly isolated");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test concurrent access to different sessions
     */
    private static boolean testConcurrentSessionAccess() {
        System.out.println("\nTest: Concurrent session access");
        
        try {
            final boolean[] success = {true};
            final int numThreads = 5;
            Thread[] threads = new Thread[numThreads];
            
            // Create multiple threads accessing different sessions
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        String sessionId = "concurrent-session-" + threadId;
                        
                        // Get or create engine
                        EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
                        
                        // Set unique state
                        ctx.currentProgramName = "Program-" + threadId;
                        ctx.currentDegree = threadId;
                        
                        // Small delay to simulate work
                        Thread.sleep(10);
                        
                        // Verify state hasn't been corrupted
                        if (!ctx.currentProgramName.equals("Program-" + threadId)) {
                            System.out.println("  ✗ Thread " + threadId + ": Program name corrupted");
                            success[0] = false;
                        }
                        
                        if (ctx.currentDegree != threadId) {
                            System.out.println("  ✗ Thread " + threadId + ": Degree corrupted");
                            success[0] = false;
                        }
                        
                        // Cleanup
                        AppContext.engines().removeEngine(sessionId);
                        
                    } catch (Exception e) {
                        System.out.println("  ✗ Thread " + threadId + " failed: " + e.getMessage());
                        success[0] = false;
                    }
                });
                threads[i].start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            if (success[0]) {
                System.out.println("  ✓ PASSED: Concurrent access handled correctly");
            } else {
                System.out.println("  ✗ FAILED: Concurrent access issues detected");
            }
            
            return success[0];
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test session cleanup functionality
     */
    private static boolean testSessionCleanup() {
        System.out.println("\nTest: Session cleanup");
        
        try {
            String session1 = "cleanup-session-1";
            String session2 = "cleanup-session-2";
            
            // Create two sessions
            AppContext.engines().getOrCreateEngine(session1);
            AppContext.engines().getOrCreateEngine(session2);
            
            int initialCount = AppContext.engines().getActiveEngineCount();
            if (initialCount < 2) {
                System.out.println("  ✗ FAILED: Sessions not created properly");
                return false;
            }
            
            // Remove one session
            AppContext.engines().removeEngine(session1);
            
            int afterRemoval = AppContext.engines().getActiveEngineCount();
            if (afterRemoval != initialCount - 1) {
                System.out.println("  ✗ FAILED: Session not removed properly");
                return false;
            }
            
            // Verify removed session returns new context
            EngineRegistry.EngineContext newCtx = AppContext.engines().getOrCreateEngine(session1);
            if (newCtx.currentProgramName != null) {
                System.out.println("  ✗ FAILED: New context has old state");
                return false;
            }
            
            // Cleanup
            AppContext.engines().removeEngine(session1);
            AppContext.engines().removeEngine(session2);
            
            System.out.println("  ✓ PASSED: Session cleanup works correctly");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
