package statistic;

import java.util.List;

public class SingleRunStatisticDTO {
    private final int runNumber;
    private final int runDegree;
    private final List<Long> input;
    private final long result;
    private final long cycles;
    private final String representation;

    public SingleRunStatisticDTO(SingleRunStatistic singleRunStatistic) {
        this.runNumber = singleRunStatistic.getRunNumber();
        this.runDegree = singleRunStatistic.getRunDegree();
        this.input = singleRunStatistic.getInput();
        this.result = singleRunStatistic.getResult();
        this.cycles = singleRunStatistic.getCycles();
        this.representation = singleRunStatistic.getRepresentation();
    }

    public int getRunNumber() {
        return runNumber;
    }

    public int getRunDegree() {
        return runDegree;
    }

    public List<Long> getInput() {
        return input;
    }

    public long getResult() {
        return result;
    }

    public long getCycles() {
        return cycles;
    }

    public String getRepresentation() {
        return representation;
    }
}