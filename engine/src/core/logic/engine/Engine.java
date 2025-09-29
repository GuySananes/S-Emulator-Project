package core.logic.engine;

import core.logic.program.SFunction;
import exception.*;
import expansion.Expansion;
import jaxb.JAXBLoader;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import core.logic.program.SProgram;
import statistic.ProgramStatisticsDTO;
import statistic.StatisticManager;
import java.util.*;

public class Engine {

    private static final Engine instance = new Engine();
    private Engine() {}
    public static Engine getInstance() {
        return instance;
    }

    private SProgram program = null;
    private SProgram effectiveProgram = null;
    private ContextPrograms contextPrograms = null;

    public Set<String> loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException {

        JAXBLoader loader = new JAXBLoader();
        program = loader.load(fullPath);
        contextPrograms = new ContextPrograms(program);
        effectiveProgram = program;
        return contextPrograms.getNames();
    }

    public PresentProgramDTO chooseContextProgram(String progName) throws NoProgramException, NoSuchProgramInContextException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(!contextPrograms.getNames().contains(progName)) {
            throw new NoSuchProgramInContextException();
        }

        changeContextProgram(progName);
        return getPresentDTOOfCurrentEffectiveProgram();
    }

    public PresentProgramDTO presentProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(effectiveProgram instanceof SFunction sf) {
            return new PresentFunctionDTO(sf);
        }

        return new PresentProgramDTO(effectiveProgram);
    }

    public PresentProgramDTO expandOrShrinkProgram(int degree) throws NoProgramException, DegreeOutOfRangeException {
        if(program == null) {
            throw new NoProgramException();
        }
        if(degree < program.getMinDegree() || degree > program.getDegree()){
            throw new DegreeOutOfRangeException(program.getMinDegree(), program.getDegree());
        }

        effectiveProgram = Expansion.expand(program, degree);
        return getPresentDTOOfCurrentEffectiveProgram();
    }

    public RunProgramDTO runProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        return new RunProgramDTO(program);
    }

    public RunProgramDTO reRunProgram(int runNumber) throws NoProgramException, ProgramNotExecutedYetException, NoSuchRunException {

        StatisticManager statisticManager = StatisticManager.getInstance();
        if(program == null) {
            throw new NoProgramException();
        }
        if(statisticManager.getRunCount(program.getName()) == 0) {
            throw new ProgramNotExecutedYetException(program.getName());
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

    public ProgramStatisticsDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(StatisticManager.getInstance().getRunCount(program.getName()) == 0) {
            throw new ProgramNotExecutedYetException(program.getName());
        }

        return new ProgramStatisticsDTO(program.getName());
    }

    private void changeContextProgram(String newProgName) {
        program = contextPrograms.getNameToProgram().get(newProgName);
        effectiveProgram = program;
    }

    private PresentProgramDTO getPresentDTOOfCurrentEffectiveProgram() {
        if(effectiveProgram instanceof SFunction sf) {
            return new PresentFunctionDTO(sf);
        }

        return new PresentProgramDTO(effectiveProgram);
    }
}
