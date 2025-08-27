package statistic;

import core.logic.program.SProgram;

import java.util.*;

public class StatisticManagerImpl implements StatisticManager{

    private final Map<SProgram, List<SingleRunStatistic>> statisticsMap = new HashMap<>();

    @Override
    public void addRunStatistic(SProgram program, SingleRunStatistic statistic) {
        statisticsMap
                .computeIfAbsent(program, p -> new ArrayList<>())
                .add(statistic);
    }

    @Override
    public List<SingleRunStatistic> getStatisticsForProgramCopy(SProgram program) {
        return new ArrayList<>(statisticsMap.getOrDefault(program, Collections.emptyList()));
    }

}
