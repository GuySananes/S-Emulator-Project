package core.logic.engine;

import core.logic.execution.RunCount;
import core.logic.program.SFunction;
import exception.*;
import expand.ExpandDTO;
import jaxb.JAXBLoader;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import core.logic.program.SProgram;
import statistic.ProgramStatisticDTO;
import statistic.StatisticManagerImpl;

import java.util.*;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();

    private SProgram program = null;
    private ContextPrograms contextPrograms = null;


    private EngineImpl() {}

    public static Engine getInstance() {
        return instance;
    }

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
    public ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException, ProgramHasNoStatisticException {
        if(program == null) {
            throw new NoProgramException();
        }

        if(RunCount.getRunCount(program) == 0) {
            throw new ProgramNotExecutedYetException();
        }

        if(StatisticManagerImpl.getInstance().getProgramStatistics(program) == null ||
                StatisticManagerImpl.getInstance().getProgramStatistics(program).isEmpty()) {
            throw new ProgramHasNoStatisticException();
        }

        return new ProgramStatisticDTO(StatisticManagerImpl.getInstance().getProgramStatistics(program));
    }

    private Set<String> getOrderedContextProgramsNames(SProgram program) {
        Set<String> names = new LinkedHashSet<>();
        names.add(program.getName());
        List<SInstruction> instructions = program.getInstructionList();
        for(SInstruction instruction : instructions) {
            if(instruction instanceof Quotable quotable) {
                extractProgramNamesFromFunctionArgument(quotable.getFunctionArgument(), names);
            }
        }

        return names;
    }

    private void extractProgramNamesFromFunctionArgument(FunctionArgument functionArgument, Set<String> names) {
        names.add(functionArgument.getProgram().getName());
        List<Argument> arguments = functionArgument.getArguments();
        for(Argument argument : arguments) {
            if(argument instanceof FunctionArgument funcArg) {
                extractProgramNamesFromFunctionArgument(funcArg, names);
            }
        }
    }

    @Override
    public SProgram getLoadedProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }
        return program;
    }
}
