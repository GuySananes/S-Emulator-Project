package statistic;

import java.util.List;

public class SingleRunStatisticDTO {
    private final int runNumber;
    private final boolean isMainProgram;
    private final String programOrFunctionName;
    private final String architectureType;
    private final int runDegree;
    private final List<Long> input;
    private final long result;
    private final long cycles;
    private final String representation;

    public SingleRunStatisticDTO(SingleRunStatistic singleRunStatistic) {
        this.runNumber = singleRunStatistic.getRunNumber();
        this.isMainProgram = singleRunStatistic.isMainProgram();
        this.programOrFunctionName = singleRunStatistic.getProgramOrFunctionName();
        this.architectureType = singleRunStatistic.getArchitectureType();
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

    public boolean isMainProgram() {
        return isMainProgram;
    }

    public String getProgramOrFunctionName() {
        return programOrFunctionName;
    }

    public String getArchitectureType() {
        return architectureType;
    }
}