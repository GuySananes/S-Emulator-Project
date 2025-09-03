package statistic;

import core.logic.program.SProgram;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticManagerImpl implements StatisticManager{

    private static final StatisticManager instance = new StatisticManagerImpl();

    private StatisticManagerImpl() { }

    public static StatisticManager getInstance() {
        return instance;
    }

    // Use ConcurrentHashMap for thread safety
    private final Map<SProgram, List<SingleRunStatistic>> statisticsMap = new ConcurrentHashMap<>();

    @Override
    public void addRunStatistic(SProgram program, SingleRunStatistic statistic) {
        if (program == null || statistic == null) {
            throw new IllegalArgumentException("Program and statistic cannot be null");
        }

        // Use computeIfAbsent with thread-safe list operations
        statisticsMap
                .computeIfAbsent(program, p -> Collections.synchronizedList(new ArrayList<>()))
                .add(statistic);
    }

    @Override
    public List<SingleRunStatistic> getStatisticsForProgramCopy(SProgram program) {
        if (program == null) {
            return new ArrayList<>();
        }

        List<SingleRunStatistic> stats = statisticsMap.get(program);
        if (stats == null) {
            return new ArrayList<>();
        }

        // Create defensive copy with synchronization
        synchronized (stats) {
            return new ArrayList<>(stats);
        }
    }
}
