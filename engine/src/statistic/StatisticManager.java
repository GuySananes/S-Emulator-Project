package statistic;

import core.logic.program.SProgram;

import java.util.List;

public interface StatisticManager {

    void addRunStatistic(SProgram program, SingleRunStatistic statistic);

    public List<SingleRunStatistic> getStatisticsForProgramCopy(SProgram program);
}
