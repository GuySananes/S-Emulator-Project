package javafxUI.model.ui;

import javafx.beans.property.*;

public class Statistic {
    private final StringProperty executionType = new SimpleStringProperty();
    private final IntegerProperty totalCycles = new SimpleIntegerProperty();
    private final IntegerProperty runCount = new SimpleIntegerProperty();
    private final StringProperty additionalInfo = new SimpleStringProperty();
    private final LongProperty resultValue = new SimpleLongProperty();  // New field for result

    public Statistic() {}

    public Statistic(String executionType, int totalCycles, int runCount, String additionalInfo) {
        this.executionType.set(executionType);
        this.totalCycles.set(totalCycles);
        this.runCount.set(runCount);
        this.additionalInfo.set(additionalInfo);
        this.resultValue.set(0);
    }

    // New constructor with result value
    public Statistic(String executionType, int totalCycles, int runCount, String additionalInfo, long resultValue) {
        this.executionType.set(executionType);
        this.totalCycles.set(totalCycles);
        this.runCount.set(runCount);
        this.additionalInfo.set(additionalInfo);
        this.resultValue.set(resultValue);
    }

    // Property getters
    public StringProperty executionTypeProperty() { return executionType; }
    public IntegerProperty totalCyclesProperty() { return totalCycles; }
    public IntegerProperty runCountProperty() { return runCount; }
    public StringProperty additionalInfoProperty() { return additionalInfo; }
    public LongProperty resultValueProperty() { return resultValue; }

    // Value getters/setters
    public String getExecutionType() { return executionType.get(); }
    public void setExecutionType(String executionType) { this.executionType.set(executionType); }

    public int getTotalCycles() { return totalCycles.get(); }
    public void setTotalCycles(int totalCycles) { this.totalCycles.set(totalCycles); }

    public int getRunCount() { return runCount.get(); }
    public void setRunCount(int runCount) { this.runCount.set(runCount); }

    public String getAdditionalInfo() { return additionalInfo.get(); }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo.set(additionalInfo); }

    public long getResultValue() { return resultValue.get(); }
    public void setResultValue(long resultValue) { this.resultValue.set(resultValue); }
}