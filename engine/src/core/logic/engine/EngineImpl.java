package core.logic.engine;


import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import DTOcreate.PresentProgramDTOCreator;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import exception.NoProgramException;
import exception.XMLUnmarshalException;
import expansion.Expandable;
import expansion.ExpansionContext;
import jaxb.JAXBLoader;
import statistic.SingleRunStatistic;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManagerImpl;

import java.nio.file.Files;
import java.nio.file.Path;
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
        // Backwards compatible convenience method - delegate to Path variant
        try {
            loadProgram(Path.of(fullPath));
        } catch (XMLUnmarshalException e) {
            // wrap as unchecked for this method signature
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadProgram(Path xmlPath) throws XMLUnmarshalException {
        if (xmlPath == null) throw new XMLUnmarshalException("Path is null");
        String s = xmlPath.toString();
        if (!s.toLowerCase().endsWith(".xml")) {
            throw new XMLUnmarshalException("File must have .xml extension: " + s);
        }
        if (!Files.isRegularFile(xmlPath)) {
            throw new XMLUnmarshalException("File not found: " + s);
        }

        JAXBLoader jaxbLoader = new JAXBLoader();
        this.program = jaxbLoader.load(s);
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
        try {
            expandProgram(1); // Default expansion degree of 1
        } catch (NoProgramException e) {
            // This should not happen if program is null, but handle gracefully
            throw new RuntimeException("No program loaded for expansion", e);
        }
    }

    @Override
    public void expandProgram(int degree) throws NoProgramException {
        if (program == null) throw new NoProgramException();

        if (degree < 0) {
            throw new IllegalArgumentException("Expansion degree must be non-negative");
        }

        if (degree == 0) {
            return; // No expansion needed
        }

        // Create expansion context
        ExpansionContext expansionContext = new ExpansionContext(program);

        // Create a new program to hold expanded instructions
        SProgramImpl expandedProgram = new SProgramImpl(program.getName() + "_expanded_" + degree);

        // Expand each instruction that implements Expandable
        for (SInstruction instruction : program.getInstructionList()) {
            if (instruction instanceof Expandable) {
                List<SInstruction> expandedInstructions = ((Expandable) instruction).expand(expansionContext);
                for (SInstruction expandedInstruction : expandedInstructions) {
                    expandedProgram.addInstruction(expandedInstruction);
                }
            } else {
                // Add non-expandable instructions as-is
                expandedProgram.addInstruction(instruction);
            }
        }

        // If degree > 1, recursively expand
        if (degree > 1) {
            this.program = expandedProgram;
            expandProgram(degree - 1);
        } else {
            this.program = expandedProgram;
        }
    }

    @Override
    public RunProgramDTO createRunDTO() throws NoProgramException {
        if (program == null) throw new NoProgramException();
        return new RunProgramDTO(program);
    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        // Create a program executor and run the program
        ProgramExecutor executor = new ProgramExecutorImpl(program);

        // Execute the program with empty input for now (this could be parameterized later)
        long result = executor.run();

        // Increment the run number for the program
        program.incrementRunNumber();

        // Create a statistic for this run with proper parameters
        SingleRunStatistic runStatistic = new SingleRunStatisticImpl(
            program.getRunNumber(),        // run number
            program.calculateMaxDegree(),  // run degree
            new java.util.ArrayList<>(),   // input (empty for now)
            result,                        // result from execution
            program.calculateCycles()      // cycles
        );

        // Record the statistic
        StatisticManagerImpl.getInstance().addRunStatistic(program, runStatistic);

        // Create run DTO with the executed program
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
