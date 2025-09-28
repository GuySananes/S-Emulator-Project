package statistic;

import core.logic.program.SProgram;

import java.util.*;

public class StatisticManagerImpl implements StatisticManager{

    private static final StatisticManager instance = new StatisticManagerImpl();

    private StatisticManagerImpl() {}

    public static StatisticManager getInstance() {
        return instance;
    }

    private final Map<String, List<SingleRunStatistic>> statisticMap = new HashMap<>();



    @Override
    public List<SingleRunStatistic> getProgramStatistics(String progName) {
        return Objects.requireNonNullElse(statisticMap.get(progName), List.of());
    }

    @Override
    public void addRunStatistic(String progName, SingleRunStatistic statistic) {
        statisticMap
                .computeIfAbsent(progName, pn -> new ArrayList<>())
                .add(statistic);
    }
}