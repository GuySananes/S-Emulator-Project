package statistic;

import java.util.ArrayList;
import java.util.List;

public class SingleRunStatisticImpl implements SingleRunStatistic {

    private final int runNumber;
    private final int runDegree;
    private final List<Long> input;
    private final long result;
    private final long cycles;

    public SingleRunStatisticImpl(int runNumber, int runDegree,
                                  List<Long> input, Long result, long cycles) {
        this.runNumber = runNumber;
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
    public List<Long> getInputCopy() {
        return new ArrayList<>(input);
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





}
