package javafx.model.ui;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ExecutionResult {
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty currentStep = new SimpleIntegerProperty(0);
    private final IntegerProperty totalSteps = new SimpleIntegerProperty(0);
    private final LongProperty executionTime = new SimpleLongProperty(0);
    private final IntegerProperty cycles = new SimpleIntegerProperty(0);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty isPaused = new SimpleBooleanProperty(false);
    private final BooleanProperty isCompleted = new SimpleBooleanProperty(false);
    private final StringProperty currentInstruction = new SimpleStringProperty();
    private final ObservableList<String> executionHistory = FXCollections.observableArrayList();

    // Constructors
    public ExecutionResult() {
        setStatus("Ready");
    }

    // Property getters for JavaFX binding
    public StringProperty statusProperty() { return status; }
    public IntegerProperty currentStepProperty() { return currentStep; }
    public IntegerProperty totalStepsProperty() { return totalSteps; }
    public LongProperty executionTimeProperty() { return executionTime; }
    public IntegerProperty cyclesProperty() { return cycles; }
    public BooleanProperty isRunningProperty() { return isRunning; }
    public BooleanProperty isPausedProperty() { return isPaused; }
    public BooleanProperty isCompletedProperty() { return isCompleted; }
    public StringProperty currentInstructionProperty() { return currentInstruction; }

    // Observable list for UI binding
    public ObservableList<String> getExecutionHistory() { return executionHistory; }

    // Value getters/setters
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public int getCurrentStep() { return currentStep.get(); }
    public void setCurrentStep(int currentStep) { this.currentStep.set(currentStep); }

    public int getTotalSteps() { return totalSteps.get(); }
    public void setTotalSteps(int totalSteps) { this.totalSteps.set(totalSteps); }

    public long getExecutionTime() { return executionTime.get(); }
    public void setExecutionTime(long executionTime) { this.executionTime.set(executionTime); }

    public int getCycles() { return cycles.get(); }
    public void setCycles(int cycles) { this.cycles.set(cycles); }

    public boolean isRunning() { return isRunning.get(); }
    public void setRunning(boolean running) { this.isRunning.set(running); }

    public boolean isPaused() { return isPaused.get(); }
    public void setPaused(boolean paused) { this.isPaused.set(paused); }

    public boolean isCompleted() { return isCompleted.get(); }
    public void setCompleted(boolean completed) { this.isCompleted.set(completed); }

    public String getCurrentInstruction() { return currentInstruction.get(); }
    public void setCurrentInstruction(String currentInstruction) { this.currentInstruction.set(currentInstruction); }

    // Utility methods
    public void addToHistory(String step) {
        executionHistory.add(step);
    }

    public void clearHistory() {
        executionHistory.clear();
    }

    public void reset() {
        setCurrentStep(0);
        setCycles(0);
        setExecutionTime(0);
        setRunning(false);
        setPaused(false);
        setCompleted(false);
        setStatus("Ready");
        setCurrentInstruction("");
        clearHistory();
    }

    public double getProgress() {
        if (totalSteps.get() == 0) return 0.0;
        return (double) currentStep.get() / totalSteps.get();
    }
}
