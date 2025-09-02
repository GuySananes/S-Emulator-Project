package core.logic.engine;

import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import DTOCreate.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import exception.NoProgramException;
import statistic.SingleRunStatistic;
import statistic.StatisticManagerImpl;

import java.util.List;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();

    private SProgram program = null;

    private EngineImpl() { }




    public static Engine getInstance() {
        return instance;
    }

    @Override
    public void loadProgram(String fullPath) {

    }


    @Override
    public PresentProgramDTO presentProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }
        return PresentProgramDTOCreator.create(program);
    }

    @Override
    public void expandProgram() {

    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return new RunProgramDTO(program);

    }

    @Override
    public List<SingleRunStatistic> presentProgramStats() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return StatisticManagerImpl.getInstance().getStatisticsForProgramCopy(program);


    }
}
