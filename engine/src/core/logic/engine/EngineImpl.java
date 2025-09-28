package core.logic.engine;

import core.logic.program.SFunction;
import exception.*;
import expand.ExpandDTO;
import jaxb.JAXBLoader;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import core.logic.program.SProgram;
import statistic.ProgramStatisticDTO;
import statistic.StatisticManager;
import java.util.*;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();
    private EngineImpl() {}
    public static Engine getInstance() {
        return instance;
    }

    private SProgram program = null;
    private ContextPrograms contextPrograms = null;

    @Override
    public Set<String> loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException {

        JAXBLoader loader = new JAXBLoader();
        program = loader.load(fullPath);
        contextPrograms = new ContextPrograms(program);
        return contextPrograms.getNames();
    }

    @Override
    public void chooseContextProgram(String programName) throws NoProgramException, NoSuchProgramInContextException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(!contextPrograms.getNames().contains(programName)) {
            throw new NoSuchProgramInContextException();
        }

        program = contextPrograms.getNameToProgram().get(programName);
    }

    @Override
    public PresentProgramDTO presentProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(program instanceof SFunction sf) {
            return new PresentFunctionDTO(sf);
        }

        return new PresentProgramDTO(program);
    }

    @Override
    public ExpandDTO expandProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        return new ExpandDTO(program);
    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        return new RunProgramDTO(program);
    }

    @Override
    public RunProgramDTO reRunProgram(int runNumber) throws NoProgramException, ProgramNotExecutedYetException, NoSuchRunException {

        StatisticManager statisticManager = StatisticManager.getInstance();
        if(program == null) {
            throw new NoProgramException();
        }
        if(statisticManager.getRunCount(program.getName()) == 0) {
            throw new ProgramNotExecutedYetException();
        }
        if(runNumber < statisticManager.getStartCount() || runNumber > statisticManager.getRunCount(program.getName())) {
            throw new NoSuchRunException(statisticManager.getStartCount(), statisticManager.getRunCount(program.getName()));
        }

        RunProgramDTO runProgramDTO = new RunProgramDTO(program);
        try {
            runProgramDTO.setDegree(statisticManager.getProgramStatistics(program.getName()).get(runNumber - 1).getRunDegree());
            runProgramDTO.setInputs(statisticManager.getProgramStatistics(program.getName()).get(runNumber - 1).getInput());
        } catch (DegreeOutOfRangeException | RunInputException e) {
            throw new RuntimeException("Unexpected error while re-running program: " + e.getMessage());
        }

        return runProgramDTO;
    }

    @Override
    public ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException, ProgramHasNoStatisticException {
        StatisticManager statisticManager = StatisticManager.getInstance();
        if(program == null) {
            throw new NoProgramException();
        }

        if(statisticManager.getRunCount(program.getName()) == 0) {
            throw new ProgramNotExecutedYetException();
        }

        if(statisticManager.getProgramStatistics(program.getName()) == null ||
                statisticManager.getProgramStatistics(program.getName()).isEmpty()) {
            throw new ProgramHasNoStatisticException();
        }

        return new ProgramStatisticDTO(statisticManager.getProgramStatistics(program.getName()));
    }
}
