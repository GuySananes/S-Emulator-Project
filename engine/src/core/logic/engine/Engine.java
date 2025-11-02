
package core.logic.engine;

import core.logic.program.ContextPrograms;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import exception.*;
import expansion.Expansion;
import jaxb.JAXBLoader;
import load.LoadProgramDTO;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;
import run.ExecuteProgramDTO;
import run.ReExecuteProgramDTO;
import statistic.ProgramStatisticsDTO;
import statistic.SingleRunStatistic;
import statistic.StatisticManager;

import java.util.HashMap;
import java.util.Map;

public class Engine {

    private static final Engine instance = new Engine();
    public Engine() {}
    public static Engine getInstance() {
        return instance;
    }

    private SProgram program = null;
    private SProgram effectiveProgram = null;
    private ContextPrograms contextPrograms = null;
    private final StatisticManager statisticManager = StatisticManager.getInstance();

    // NEW: Global registry of all loaded functions across all programs
    private final Map<String, SFunction> globalFunctionRegistry = new HashMap<>();

    public LoadProgramDTO loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException {
        // Get existing system functions if any programs are already loaded
        java.util.Set<String> systemFunctionNames = null;
        if (this.contextPrograms != null) {
            systemFunctionNames = this.contextPrograms.getNames();
        }

        // Load the program with system function context
        JAXBLoader loader = new JAXBLoader();
        SProgram engineProgram = loader.load(fullPath, systemFunctionNames);

        // NEW: Merge the local functions from the new program into the global registry
        if (engineProgram instanceof SProgramImpl programImpl) {
            Map<String, SFunction> newLocalFunctions = programImpl.getLocalFunctions();

            System.out.println("DEBUG: Loading program with " + newLocalFunctions.size() + " new functions");
            System.out.println("DEBUG: New functions: " + newLocalFunctions.keySet());
            System.out.println("DEBUG: Global registry before merge has " + globalFunctionRegistry.size() + " functions");

            // Add all new functions to the global registry
            globalFunctionRegistry.putAll(newLocalFunctions);

            System.out.println("DEBUG: Global registry after merge has " + globalFunctionRegistry.size() + " functions");
            System.out.println("DEBUG: All functions in registry: " + globalFunctionRegistry.keySet());

            // Create a complete function map from the global registry
            Map<String, SFunction> completeFunctionMap = new HashMap<>(globalFunctionRegistry);

            // CRITICAL FIX: Update ALL functions (both old and new) with the complete map
            // This ensures every function's ContextPrograms sees all available functions
            System.out.println("DEBUG: Updating ALL functions with complete function map...");
            for (Map.Entry<String, SFunction> entry : completeFunctionMap.entrySet()) {
                System.out.println("DEBUG: Updating function: " + entry.getKey());
                try {
                    entry.getValue().setLocalFunctions(completeFunctionMap);
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to update function " + entry.getKey() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            // Set the complete map on the main program (this updates ALL its local functions)
            System.out.println("DEBUG: Setting complete function map on main program...");
            try {
                programImpl.setLocalFunctions(completeFunctionMap);
            } catch (Exception e) {
                System.err.println("ERROR: Failed to set local functions on main program: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        // Set the program (but don't set contextPrograms yet)
        this.program = engineProgram;
        this.effectiveProgram = engineProgram;

        // Resolve all system function references BEFORE creating ContextPrograms
        System.out.println("DEBUG: Resolving system functions...");
        if (engineProgram instanceof SProgramImpl programImpl) {
            try {
                programImpl.resolveSystemFunctions();
                System.out.println("DEBUG: System functions resolved successfully");

                // NOW recreate ContextPrograms AFTER resolution is complete
                System.out.println("DEBUG: Recreating ContextPrograms after resolution...");
                programImpl.recreateContextPrograms();

                // Also recreate for all functions in the global registry
                for (SFunction function : globalFunctionRegistry.values()) {
                    function.recreateContextPrograms();
                }

                System.out.println("DEBUG: ContextPrograms recreated successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to resolve system functions: " + e.getMessage());
                e.printStackTrace();
                throw new ProgramValidationException("Failed to resolve system functions: " + e.getMessage(), e);
            }
        }

        // NOW set the context programs after recreation
        this.contextPrograms = engineProgram.getContextPrograms();

        // Create and return the DTO with context programs names
        System.out.println("DEBUG: Creating PresentProgramDTO...");
        try {
            PresentProgramDTO presentDTO = new PresentProgramDTO(engineProgram);
            System.out.println("DEBUG: Available context programs: " + this.contextPrograms.getNames());
            return new LoadProgramDTO(presentDTO, this.contextPrograms.getNames());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create DTO: " + e.getMessage());
            e.printStackTrace();
            throw new ProgramValidationException("Failed to create program DTO: " + e.getMessage(), e);
        }
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

    public PresentProgramDTO presentProgram() throws NoProgramException {
        if(program == null) {
            throw new NoProgramException();
        }
        return getPresentDTOOfCurrentEffectiveProgram();
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