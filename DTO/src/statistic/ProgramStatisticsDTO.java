package statistic;

import exception.ProgramNotExecutedYetException;

import java.util.ArrayList;
import java.util.List;

public class ProgramStatisticsDTO {

    private List<SingleRunStatisticDTO> statistics = null;

    public ProgramStatisticsDTO(String progName) throws ProgramNotExecutedYetException {
        List<SingleRunStatistic> statistics = StatisticManager.getInstance().getProgramStatistics(progName);
        if (statistics == null || statistics.isEmpty()) {
            throw new ProgramNotExecutedYetException(progName);
        } else {
            this.statistics = new ArrayList<>();
            for (SingleRunStatistic stat : statistics) {
                this.statistics.add(new SingleRunStatisticDTO(stat));
            }
        }
    }



    public List<SingleRunStatisticDTO> getProgramStatisticCopy() {
        return statistics;
    }

    public String getRepresentation() {
        StringBuilder sb = new StringBuilder();
        for (SingleRunStatisticDTO stat : statistics) {
            sb.append(stat.getRepresentation()).append("\n\n");
        }

        return sb.toString().trim();
    }
}
