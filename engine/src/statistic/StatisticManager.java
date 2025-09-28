package statistic;

import core.logic.program.SProgram;

import java.util.List;
import java.util.Map;

public interface StatisticManager {

    void addRunStatistic(String program, SingleRunStatistic statistic);

    List<SingleRunStatistic> getProgramStatistics(String program);
}
