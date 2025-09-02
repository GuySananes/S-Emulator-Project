package statistic;

import core.logic.program.SProgram;

import java.util.List;
import java.util.Map;

public interface StatisticManager {

    void addRunStatistic(SProgram program, SingleRunStatistic statistic);

    Map<SProgram, List<SingleRunStatistic>> getStatisticMap();
}
