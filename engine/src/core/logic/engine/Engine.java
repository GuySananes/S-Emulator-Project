package engine;

import core.logic.program.ContextPrograms;
import core.logic.program.SFunction;
import exception.*;
import expansion.Expansion;
/*import jaxb.JAXBLoader;*/
import load.LoadProgramDTO;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;
import run.ExecuteProgramDTO;
import run.ReExecuteProgramDTO;
import core.logic.program.SProgram;
import statistic.ProgramStatisticsDTO;
import statistic.SingleRunStatistic;
import statistic.StatisticManager;

public class Engine {

    private static final Engine instance = new Engine();
    private Engine() {}
    public static Engine getInstance() {
        return instance;
    }

    private SProgram program = null;
    private SProgram effectiveProgram = null;
    private ContextPrograms contextPrograms = null;
    private final StatisticManager statisticManager = StatisticManager.getInstance();

      public LoadProgramDTO loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException {
        JAXBLoader loader = new JAXBLoader();
        program = loader.load(fullPath);
        effectiveProgram = program;
        contextPrograms = program.getContextPrograms();
        return new LoadProgramDTO(getPresentDTOOfCurrentEffectiveProgram(),
                contextPrograms.getNames());
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

    public ExecuteProgramDTO executeProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }

        return new ExecuteProgramDTO(effectiveProgram);
    }

    public ReExecuteProgramDTO reExecuteProgram(int runNumber) throws NoProgramException, ProgramNotExecutedYetException, NoSuchRunException {

        if (program == null) {
            throw new NoProgramException();
        }
        if (statisticManager.getRunCount(program.getName()) == 0) {
            throw new ProgramNotExecutedYetException(program.getName());
        }
        if (runNumber < statisticManager.getStartCount() || runNumber > statisticManager.getRunCount(program.getName())) {
            throw new NoSuchRunException(statisticManager.getStartCount(), statisticManager.getRunCount(program.getName()));
        }

        SingleRunStatistic runStatistics = statisticManager.getProgramStatistics(program.getName()).get(runNumber - 1);
        effectiveProgram = Expansion.expand(program, runStatistics.getRunDegree());
        PresentProgramDTO presentProgramDTO = getPresentDTOOfCurrentEffectiveProgram();
        ExecuteProgramDTO executeProgramDTO = new ExecuteProgramDTO(effectiveProgram);

        try {
            executeProgramDTO.getRunProgramDTO().setInput(runStatistics.getInput());
            executeProgramDTO.getDebugProgramDTO().setInput(runStatistics.getInput());
        } catch (RunInputException e) {
            throw new RuntimeException("Unexpected error while re-running program in Engine::reRunProgram: " + e.getMessage());
        }

        return new ReExecuteProgramDTO(presentProgramDTO, executeProgramDTO);
    }

    public ProgramStatisticsDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(statisticManager.getRunCount(program.getName()) == 0) {
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
