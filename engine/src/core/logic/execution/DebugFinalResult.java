package core.logic.execution;

import java.util.List;

public class DebugFinalResult extends DebugResult {

    private final long result;

    public DebugFinalResult(long result, List<Long> values, int cycles) {
        super(values, -1, cycles);
        this.result = result;
    }

    public long getResult() {
        return result;
    }
}
