package javafx.model;

import javafx.beans.property.*;

public class Statistic {
    private final StringProperty executionType = new SimpleStringProperty();
    private final IntegerProperty totalCycles = new SimpleIntegerProperty();
    private final IntegerProperty totalInstructions = new SimpleIntegerProperty();
    private final StringProperty executionTime = new SimpleStringProperty();

    // Constructors
    public Statistic() {}

    public Statistic(String executionType, int totalCycles, int totalInstructions, String executionTime) {
        this.executionType.set(executionType);
        this.totalCycles.set(totalCycles);
        this.totalInstructions.set(totalInstructions);
        this.executionTime.set(executionTime);
    }

    // Property getters for TableView binding
    public StringProperty executionTypeProperty() { return executionType; }
    public IntegerProperty totalCyclesProperty() { return totalCycles; }
    public IntegerProperty totalInstructionsProperty() { return totalInstructions; }
    public StringProperty executionTimeProperty() { return executionTime; }

    // Value getters/setters
    public String getExecutionType() { return executionType.get(); }
    public void setExecutionType(String executionType) { this.executionType.set(executionType); }

    public int getTotalCycles() { return totalCycles.get(); }
    public void setTotalCycles(int totalCycles) { this.totalCycles.set(totalCycles); }

    public int getTotalInstructions() { return totalInstructions.get(); }
    public void setTotalInstructions(int totalInstructions) { this.totalInstructions.set(totalInstructions); }

    public String getExecutionTime() { return executionTime.get(); }
    public void setExecutionTime(String executionTime) { this.executionTime.set(executionTime); }
}
