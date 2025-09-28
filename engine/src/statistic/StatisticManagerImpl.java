package statistic;

import core.logic.program.SProgram;

import java.util.*;

public class StatisticManager implements StatisticManager{

    private static final StatisticManager instance = new StatisticManager();
    public static StatisticManager getInstance() {return instance;}
    private StatisticManager() {}

    private final Map<String, List<SingleRunStatistic>> statisticMap = new HashMap<>();

    private final Map<String, Integer> runCount = new HashMap<>();

    private static final int startCount = 1;


    public int getStartCount() {
        return startCount;
    }

    public List<SingleRunStatistic> getProgramStatistics(String progName) {
        return Objects.requireNonNullElse(statisticMap.get(progName), List.of());
    }


    public void addRunStatistic(String progName, SingleRunStatistic statistic) {
        statisticMap.computeIfAbsent(progName, pn -> new ArrayList<>()).add(statistic);
    }


    public int getRunCount(String progName) {
        return runCount.computeIfAbsent(progName, p -> 0);
    }

    public void incrementRunCount(String progName) {
        runCount.put(progName, getRunCount(progName) + 1);
    }
}