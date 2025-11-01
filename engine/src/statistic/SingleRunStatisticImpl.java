package statistic;

import java.util.ArrayList;
import java.util.List;

public class SingleRunStatisticImpl implements SingleRunStatistic {

    private final int runNumber;
    private final boolean isMainProgram;
    private final String programOrFunctionName;
    private final String architectureType;
    private final int runDegree;
    private final List<Long> input;
    private final long result;
    private final long cycles;

    public SingleRunStatisticImpl(int runNumber, boolean isMainProgram,
                                  String programOrFunctionName, String architectureType,
                                  int runDegree, List<Long> input, Long result, long cycles) {
        this.runNumber = runNumber;
        this.isMainProgram = isMainProgram;
        this.programOrFunctionName = programOrFunctionName;
        this.architectureType = architectureType;
        this.runDegree = runDegree;
        this.input = List.copyOf(input);
        this.result = result;
        this.cycles = cycles;
    }

    @Override
    public int getRunNumber() {
        return runNumber;
    }

    @Override
    public int getRunDegree() {
        return runDegree;
    }

    @Override
    public List<Long> getInput() {
        return input;
    }

    @Override
    public long getResult() {
        return result;
    }

    @Override
    public long getCycles() {
        return cycles;
    }

    @Override
    public String getRepresentation() {
        return "Run number: " + runNumber + "\n" +
                "Run degree: " + runDegree + "\n" +
                "Input: " + input + "\n" +
                "Result: " + result + "\n" +
                "Cycles: " + cycles + "\n";
    }

    @Override
    public boolean isMainProgram() {
        return isMainProgram;
    }

    @Override
    public String getProgramOrFunctionName() {
        return programOrFunctionName;
    }

    @Override
    public String getArchitectureType() {
        return architectureType;
    }
}
