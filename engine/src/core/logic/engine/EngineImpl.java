package core.logic.engine;

import exception.ProgramNotExecutedYetException;
import expand.ExpandDTO;
import jaxb.JAXBLoader;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import present.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import exception.NoProgramException;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import statistic.ProgramStatisticDTO;
import statistic.StatisticManagerImpl;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();

    private SProgram program = null;

    private EngineImpl() { }

    public static Engine getInstance() {
        return instance;
    }

    @Override
    public void loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException {

        JAXBLoader loader = new JAXBLoader();
        program = loader.load(fullPath);

    }


    @Override
    public PresentProgramDTO presentProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return PresentProgramDTOCreator.create(program);
    }

    @Override
    public ExpandDTO expandProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return new ExpandDTO(program);
    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return new RunProgramDTO(program);
    }

    @Override
    public ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException {
        if (program == null) {
            throw new NoProgramException();
        }

        if(StatisticManagerImpl.getInstance().getProgramStatistics(program) == null ||
                StatisticManagerImpl.getInstance().getProgramStatistics(program).isEmpty()) {
            throw new ProgramNotExecutedYetException();
        }

        return new ProgramStatisticDTO(StatisticManagerImpl.getInstance().getProgramStatistics(program));
    }
}
