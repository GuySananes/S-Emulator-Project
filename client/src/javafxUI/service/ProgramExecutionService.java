package javafxUI.service;

/**
 * Service responsible for managing execution state.
 * Execution is now handled through Engine and DTOs only.
 */
public class ProgramExecutionService {

    private boolean isExecuting = false;
    private boolean isPaused = false;

    /**
     * Stops the current execution
     */
    public void stopExecution() {
        this.isExecuting = false;
        this.isPaused = false;
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

    public void setExecuting(boolean executing) {
        this.isExecuting = executing;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
}