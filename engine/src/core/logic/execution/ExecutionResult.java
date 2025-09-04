package core.logic.execution;

public class ExecutionResult {
    private final long result;
    private final int cycles;

    public ExecutionResult(long result, int cycles){
        this.result = result;
        this.cycles = cycles;
    }

    public long getResult() {
        return result;
    }

    public int getCycles() {
        return cycles;
    }
}
