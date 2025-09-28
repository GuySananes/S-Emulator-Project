
package javafxUI.service;

import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.execution.ResultCycle;
import core.logic.program.SProgram;
import javafx.concurrent.Task;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for executing S-Programs and managing execution state
 */
public class ProgramExecutionService {

    private ProgramExecutor programExecutor;
    private SProgram currentProgram;
    private boolean isExecuting = false;
    private boolean isPaused = false;

    /**
     * Executes a program synchronously using actual engine API
     */
    public ResultCycle executeProgram(SProgram program, Long... inputs) {
        if (program == null) {
            throw new IllegalArgumentException("Program cannot be null");
        }

        this.currentProgram = program;
        this.programExecutor = new ProgramExecutorImpl(program);
        this.isExecuting = true;

        try {
            return programExecutor.run(Arrays.asList(inputs), 0);
        } finally {
            this.isExecuting = false;
        }
    }

    /**
     * Executes a program asynchronously
     */
    public CompletableFuture<ResultCycle> executeProgramAsync(SProgram program, Long... inputs) {
        return CompletableFuture.supplyAsync(() -> executeProgram(program, inputs));
    }

    /**
     * Creates a JavaFX Task for program execution with progress monitoring
     */
    public Task<ResultCycle> createExecutionTask(SProgram program, Long... inputs) {
        return new Task<ResultCycle>() {
            @Override
            protected ResultCycle call() throws Exception {
                updateMessage("Starting program execution...");
                updateProgress(0, 1);

                ResultCycle result = executeProgram(program, inputs);

                updateMessage("Execution completed");
                updateProgress(1, 1);

                return result;
            }
        };
    }

    /**
     * Starts debug execution (step-by-step)
     * Note: Your engine doesn't seem to support step debugging yet
     */
    public void startDebugExecution(SProgram program) {
        if (program == null) {
            throw new IllegalArgumentException("Program cannot be null");
        }

        this.currentProgram = program;
        this.programExecutor = new ProgramExecutorImpl(program);
        this.isExecuting = true;
        this.isPaused = true;
    }

    /**
     * Steps to the next instruction in debug mode
     * TODO: Your engine needs step-by-step execution support
     */
    public boolean stepNext() {
        if (!isExecuting || !isPaused || currentProgram == null) {
            return false;
        }

        // TODO: Implement step-by-step execution logic
        // Your current engine runs the entire program at once
        // You'd need to modify ProgramExecutor to support stepping
        return true;
    }

    /**
     * Steps back to the previous instruction in debug mode
     * TODO: Your engine needs step-back execution support
     */
    public boolean stepBack() {
        if (!isExecuting || !isPaused || currentProgram == null) {
            return false;
        }

        // TODO: Implement step-back logic
        // This would require execution history tracking
        return true;
    }

    /**
     * Resumes execution from paused state
     */
    public ResultCycle resumeExecution(Long... inputs) {
        if (isExecuting && isPaused && programExecutor != null) {
            this.isPaused = false;
            return programExecutor.run(Arrays.asList(inputs), 0);
        }
        return null;
    }

    /**
     * Stops the current execution
     */
    public void stopExecution() {
        this.isExecuting = false;
        this.isPaused = false;
        this.currentProgram = null;
        this.programExecutor = null;
    }

    /**
     * Checks if execution is currently running
     */
    public boolean isExecuting() {
        return isExecuting;
    }

    /**
     * Checks if execution is paused (debug mode)
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Gets the current program being executed
     */
    public SProgram getCurrentProgram() {
        return currentProgram;
    }

    /**
     * Gets the current program executor
     */
    public ProgramExecutor getProgramExecutor() {
        return programExecutor;
    }
}