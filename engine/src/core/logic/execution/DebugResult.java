package core.logic.execution;

import java.util.List;

public class DebugResult {
    private final List<Long> values;
    private final int nextIndex;
    private final int cycles;

    public DebugResult(List<Long> values, int nextIndex, int cycles) {
        this.values = values;
        this.nextIndex = nextIndex;
        this.cycles = cycles;
    }

    public List<Long> getValues() {
        return values;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getCycles() {
        return cycles;
    }
}
