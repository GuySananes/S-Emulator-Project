package statistic;

import core.logic.program.SProgram;
import exception.ProgramNotExecutedYetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProgramStatisticDTO {

    private final SProgram program;
    private final Map<SProgram, List<SingleRunStatistic>> statisticMap;

    public ProgramStatisticDTO(Map<SProgram, List<SingleRunStatistic>> statisticMap, SProgram program) {
        this.program = program;
        this.statisticMap = statisticMap;
    }

    public List<SingleRunStatistic> getProgramStatisticCopy(SProgram program) throws ProgramNotExecutedYetException {
        if (!statisticMap.containsKey(program)) {
            throw new ProgramNotExecutedYetException();
        }

        return new ArrayList<>(statisticMap.getOrDefault(program, Collections.emptyList()));
    }

    public String getRepresentation() throws ProgramNotExecutedYetException {
        if (!statisticMap.containsKey(program)) {
            throw new ProgramNotExecutedYetException();
        }

        List<SingleRunStatistic> runs = statisticMap.getOrDefault(program, Collections.emptyList());
        if (runs.isEmpty()) {
            throw new ProgramNotExecutedYetException();
        }

        StringBuilder sb = new StringBuilder();
        for (SingleRunStatistic stat : runs) {
            sb.append(stat.getRepresentation()).append("\n\n");
        }

        return sb.toString().trim();
    }



}
