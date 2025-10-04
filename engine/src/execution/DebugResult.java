package execution;

public class DebugResult {
    protected ChangedVariable changedVariable;
    protected int nextIndex;
    protected int cycles;

    public DebugResult() {
        this.changedVariable = null;
        this.nextIndex = -1;
        this.cycles = -1;
    }

    public DebugResult(ChangedVariable changedVariable, int nextIndex, int cycles) {
        this.changedVariable = changedVariable;
        this.nextIndex = nextIndex;
        this.cycles = cycles;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public void setChangedVariable(ChangedVariable changedVariable) {
        this.changedVariable = changedVariable;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getCycles() {
        return cycles;
    }

    public ChangedVariable getChangedVariable() {
        return changedVariable;
    }
}
