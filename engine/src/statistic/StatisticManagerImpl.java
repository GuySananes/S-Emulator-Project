package statistic;

import core.logic.program.SProgram;

import java.util.*;

public class StatisticManagerImpl implements StatisticManager{

    private static final StatisticManager instance = new StatisticManagerImpl();

    private StatisticManagerImpl() {}

    public static StatisticManager getInstance() {
        return instance;
    }


    private final Map<SProgram, List<SingleRunStatistic>> statisticMap = new HashMap<>();

    @Override
    public Map<SProgram, List<SingleRunStatistic>> getStatisticMap() {
        return statisticMap;
    }

    @Override
    public void addRunStatistic(SProgram program, SingleRunStatistic statistic) {
        statisticMap
                .computeIfAbsent(program, p -> new ArrayList<>())
                .add(statistic);
    }
}
