package core.logic.execution;

public class ResultCycle {
    private final long result;
    private final int cycles;

    public ResultCycle(long result, int cycles){
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
