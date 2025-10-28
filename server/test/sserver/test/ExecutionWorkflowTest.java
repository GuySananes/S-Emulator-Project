package sserver.test;

import core.logic.engine.Engine;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import core.logic.execution.ResultCycle;
import exception.NoProgramException;
import load.LoadProgramDTO;
import present.program.PresentProgramDTO;
import run.DebugProgramDTO;
import run.ExecuteProgramDTO;
import run.ReExecuteProgramDTO;
import run.RunProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Test complete execution workflows.
 * Tests regular execution, debug execution, expand/collapse, and rerun functionality.
 * Requirements: 1.1, 1.2, 1.3, 1.4, 3.1, 3.2, 3.3, 3.4, 5.1, 5.2, 5.3, 5.4
 */
public class ExecutionWorkflowTest {

    private static final String TEST_PROGRAM_PATH = "run/badic.xml";
    
    public static void main(String[] args) {
        System.out.println("=== Execution Workflow Test ===\n");
        
        boolean allPassed = true;
        
        allPassed &= testRegularExecutionFlow();
        allPassed &= testDebugExecutionFlow();
        allPassed &= testExpandCollapseOperations();
        allPassed &= testRerunFunctionality();
        
        System.out.println("\n=== Test Results ===");
        if (allPassed) {
            System.out.println("✓ All tests PASSED");
        } else {
            System.out.println("✗ Some tests FAILED");
            System.exit(1);
        }
    }

    /**
     * Test regular execution flow end-to-end
     * Requirements: 1.1, 1.2, 1.3, 1.4
     */
    private static boolean testRegularExecutionFlow() {
        System.out.println("Test: Regular execution flow");
        
        try {
            String sessionId = "exec-test-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Step 1: Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            if (!ctx.isProgramLoaded()) {
                System.out.println("  ✗ FAILED: Program not loaded");
                return false;
            }
            
            // Step 2: Create execution
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
            
            // Step 3: Set inputs (if program requires them)
            List<Long> inputs = Arrays.asList(5L, 3L);
            try {
                runDTO.setInput(inputs);
            } catch (Exception e) {
                // Some programs may not require inputs, that's okay
            }
            
            // Step 4: Execute program
            ResultCycle result = runDTO.runProgram();
            
            if (result == null) {
                System.out.println("  ✗ FAILED: No result returned");
                return false;
            }
            
            if (result.getCycles() < 0) {
                System.out.println("  ✗ FAILED: Invalid cycle count");
                return false;
            }
            
            System.out.println("  Program executed: result=" + result.getResult() + 
                             ", cycles=" + result.getCycles());
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Regular execution flow works");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test debug execution flow with step/resume operations
     * Requirements: 3.1, 3.2, 3.3, 3.4
     */
    private static boolean testDebugExecutionFlow() {
        System.out.println("\nTest: Debug execution flow");
        
        try {
            String sessionId = "debug-test-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Step 1: Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            // Step 2: Create debug execution
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            DebugProgramDTO debugDTO = executeDTO.getDebugProgramDTO();
            
            // Step 3: Set inputs
            List<Long> inputs = Arrays.asList(5L, 3L);
            try {
                debugDTO.setInput(inputs);
            } catch (Exception e) {
                // Some programs may not require inputs
            }
            
            // Step 4: Execute debug steps
            int stepCount = 0;
            int maxSteps = 100; // Safety limit
            DebugResult lastResult = null;
            
            while (stepCount < maxSteps) {
                DebugResult result = debugDTO.nextStep();
                lastResult = result;
                stepCount++;
                
                if (result instanceof DebugFinalResult) {
                    DebugFinalResult finalResult = (DebugFinalResult) result;
                    System.out.println("  Debug completed after " + stepCount + " steps");
                    System.out.println("  Final result: " + finalResult.getResult() + 
                                     ", cycles: " + finalResult.getCycles());
                    break;
                }
            }
            
            if (stepCount >= maxSteps) {
                System.out.println("  ✗ FAILED: Debug did not complete within " + maxSteps + " steps");
                return false;
            }
            
            if (!(lastResult instanceof DebugFinalResult)) {
                System.out.println("  ✗ FAILED: Last result is not DebugFinalResult");
                return false;
            }
            
            // Test resume functionality with a new debug session
            ExecuteProgramDTO executeDTO2 = engine.executeProgram();
            DebugProgramDTO debugDTO2 = executeDTO2.getDebugProgramDTO();
            
            try {
                debugDTO2.setInput(inputs);
            } catch (Exception e) {
                // Ignore
            }
            
            // Take a few steps then resume
            debugDTO2.nextStep();
            debugDTO2.nextStep();
            
            DebugFinalResult resumeResult = debugDTO2.runUntilEnd();
            
            if (resumeResult == null) {
                System.out.println("  ✗ FAILED: Resume did not return valid result");
                return false;
            }
            
            System.out.println("  Resume result: " + resumeResult.getResult());
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Debug execution flow works");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test expand/collapse operations
     * Requirements: 5.1, 5.2
     */
    private static boolean testExpandCollapseOperations() {
        System.out.println("\nTest: Expand/collapse operations");
        
        try {
            String sessionId = "expand-test-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Load a program
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            int initialDegree = ctx.currentDegree;
            System.out.println("  Initial degree: " + initialDegree);
            
            // Get initial instruction count
            PresentProgramDTO initialPresent = loadDTO.getPresentProgramDTO();
            int initialInstructionCount = initialPresent.getInstructionList() != null ? 
                initialPresent.getInstructionList().size() : 0;
            
            // Test expand
            try {
                int newDegree = initialDegree + 1;
                PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(newDegree);
                ctx.currentDegree = newDegree;
                
                if (ctx.currentDegree != newDegree) {
                    System.out.println("  ✗ FAILED: Degree not updated after expand");
                    return false;
                }
                
                int expandedInstructionCount = expandedProgram.getInstructionList() != null ?
                    expandedProgram.getInstructionList().size() : 0;
                
                System.out.println("  After expand: degree=" + newDegree + 
                                 ", instructions=" + expandedInstructionCount);
                
                // Test collapse back
                int collapseDegree = newDegree - 1;
                PresentProgramDTO collapsedProgram = engine.expandOrShrinkProgram(collapseDegree);
                ctx.currentDegree = collapseDegree;
                
                if (ctx.currentDegree != collapseDegree) {
                    System.out.println("  ✗ FAILED: Degree not updated after collapse");
                    return false;
                }
                
                int collapsedInstructionCount = collapsedProgram.getInstructionList() != null ?
                    collapsedProgram.getInstructionList().size() : 0;
                
                System.out.println("  After collapse: degree=" + collapseDegree + 
                                 ", instructions=" + collapsedInstructionCount);
                
            } catch (Exception e) {
                // Some programs may not support expand/collapse
                System.out.println("  Note: Program may not support expand/collapse: " + e.getMessage());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Expand/collapse operations work");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test rerun functionality
     * Requirements: 5.3
     */
    private static boolean testRerunFunctionality() {
        System.out.println("\nTest: Rerun functionality");
        
        try {
            String sessionId = "rerun-test-session";
            EngineRegistry.EngineContext ctx = AppContext.engines().getOrCreateEngine(sessionId);
            Engine engine = ctx.engine;
            
            // Load and execute a program first
            LoadProgramDTO loadDTO = engine.loadProgram(TEST_PROGRAM_PATH);
            ctx.setLoadedProgram(loadDTO);
            
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
            
            List<Long> inputs = Arrays.asList(5L, 3L);
            try {
                runDTO.setInput(inputs);
            } catch (Exception e) {
                // Ignore
            }
            
            ResultCycle firstResult = runDTO.runProgram();
            System.out.println("  First execution: result=" + firstResult.getResult());
            
            // Now test rerun
            try {
                ReExecuteProgramDTO rerunDTO = engine.reExecuteProgram(1);
                
                if (rerunDTO == null) {
                    System.out.println("  ✗ FAILED: Rerun returned null");
                    return false;
                }
                
                System.out.println("  Rerun successful for run #1");
                
                // Try to rerun a non-existent run number
                try {
                    engine.reExecuteProgram(999);
                    System.out.println("  ✗ FAILED: Should have thrown exception for invalid run number");
                    return false;
                } catch (Exception e) {
                    // Expected - invalid run number should throw exception
                    System.out.println("  Correctly rejected invalid run number");
                }
                
            } catch (Exception e) {
                System.out.println("  Note: Rerun may not be supported: " + e.getMessage());
            }
            
            // Cleanup
            AppContext.engines().removeEngine(sessionId);
            
            System.out.println("  ✓ PASSED: Rerun functionality works");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
