package core.logic.execution;

import java.util.ArrayList;
import java.util.List;

public class DebugResult {
    protected List<Long> variablesValue;
    protected int nextIndex;
    protected int cycles;

    public DebugResult() {
        this.variablesValue = null;
        this.nextIndex = -1;
        this.cycles = -1;
    }

    public DebugResult(List<Long> variablesValue, int nextIndex, int cycles) {
        this.variablesValue = variablesValue;
        this.nextIndex = nextIndex;
        this.cycles = cycles;
    }

    protected void setVariablesValue(List<Long> variablesValue) {
        this.variablesValue = variablesValue;
    }

    protected void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    protected void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public List<Long> getVariablesValue() {
        return variablesValue;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getCycles() {
        return cycles;
    }
}
