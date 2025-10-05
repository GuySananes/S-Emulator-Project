package core.logic.execution;

public class DebugFinalResult extends DebugResult {

    private final long result;
    public DebugFinalResult(long result, int cycles) {
        super(null, -1, cycles);
        this.result = result;
    }

    public long getResult() {
        return result;
    }
}
