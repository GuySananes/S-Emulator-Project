package core.logic.engine;

import expand.ExpandDTO;
import jaxb.JAXBLoader;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import present.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import exception.NoProgramException;
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
    public void loadProgram(String fullPath) {

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
    public ProgramStatisticDTO presentProgramStats() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return new ProgramStatisticDTO(StatisticManagerImpl.getInstance().getStatisticMap(), program);


    }
}
