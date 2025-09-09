package statistic;

import core.logic.program.SProgram;
import exception.ProgramNotExecutedYetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProgramStatisticDTO {

    private final List<SingleRunStatistic> statisticList;

    public ProgramStatisticDTO(List<SingleRunStatistic> statisticList) {
        this.statisticList = statisticList;
    }



    public List<SingleRunStatistic> getProgramStatisticCopy() throws ProgramNotExecutedYetException {
        if (statisticList == null || statisticList.isEmpty() ) {
            throw new ProgramNotExecutedYetException();
        }

        return new ArrayList<>(statisticList);
    }

    public String getRepresentation() throws ProgramNotExecutedYetException {
        if (statisticList == null || statisticList.isEmpty() ) {
            throw new ProgramNotExecutedYetException();
        }

        StringBuilder sb = new StringBuilder();
        for (SingleRunStatistic stat : statisticList) {
            sb.append(stat.getRepresentation()).append("\n\n");
        }

        return sb.toString().trim();
    }
}
