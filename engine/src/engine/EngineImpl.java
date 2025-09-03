package engine;

import core.logic.instruction.*;
import core.logic.label.LabelImpl;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import expand.ExpandDTO;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import present.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import exception.NoProgramException;
import statistic.ProgramStatisticDTO;
import statistic.StatisticManagerImpl;

import java.util.ArrayList;
import java.util.List;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();

    private SProgram program = null;

    private EngineImpl() { }

    public static Engine getInstance() {
        return instance;
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








    //for testing purposes
    private SProgram createProgram(List<SInstruction> instructions) {
        SProgram program = new SProgramImpl("TestProgram");
        for (SInstruction instruction : instructions) {
            program.addInstruction(instruction);
        }

        return program;
    }

    private List<SInstruction> createInstructionListNoExpand() {
        LabelImpl L1 = new LabelImpl(1);
        List<SInstruction> instructions = new ArrayList<>();
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.INPUT, 1), L1));
        instructions.add(new NoOpInstruction(Variable.RESULT));
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.WORK, 1)));
        instructions.add(new JumpNotZeroInstruction(Variable.RESULT, L1));

        return instructions;
    }

    private List<SInstruction> createInstructionListExpand() {
        LabelImpl L1 = new LabelImpl(1);
        List<SInstruction> instructions = new ArrayList<>();
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.INPUT, 1), L1));
        instructions.add(new IncreaseInstruction(Variable.RESULT));
        instructions.add(new ZeroVariableInstruction(Variable.RESULT));
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.WORK, 1)));
        instructions.add(new JumpNotZeroInstruction(Variable.RESULT, L1));

        return instructions;
    }



    @Override
    public void loadProgram(String fullPath) {
        program = createProgram(createInstructionListExpand());
    }
}
