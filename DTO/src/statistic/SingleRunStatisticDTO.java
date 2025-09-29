package statistic;

public class SingleRunStatisticDTO {
    private final int runNumber;
    private final int runDegree;
    private final long result;
    private final long cycles;
    private final String representation;

    public SingleRunStatisticDTO(SingleRunStatistic singleRunStatistic) {
        this.runNumber = singleRunStatistic.getRunNumber();
        this.runDegree = singleRunStatistic.getRunDegree();
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
