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
    public List<SingleRunStatistic> getProgramStatistics(SProgram program) {
        return Collections.unmodifiableList(
                Objects.requireNonNullElse(statisticMap.get(program), List.of()));
    }

    @Override
    public void addRunStatistic(SProgram program, SingleRunStatistic statistic) {
        statisticMap
                .computeIfAbsent(program, p -> new ArrayList<>())
                .add(statistic);
    }
}