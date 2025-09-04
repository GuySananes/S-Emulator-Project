package engine;

import core.logic.instruction.*;
import core.logic.label.FixedLabel;
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
/*    private SProgram createProgram(List<SInstruction> instructions) {
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

    private List<SInstruction> createInstructionListExpand1() {
        LabelImpl L1 = new LabelImpl(1);
        List<SInstruction> instructions = new ArrayList<>();
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.INPUT, 1), L1));
        instructions.add(new IncreaseInstruction(Variable.RESULT));
        instructions.add(new ZeroVariableInstruction(Variable.RESULT));
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.WORK, 1)));
        instructions.add(new JumpNotZeroInstruction(Variable.RESULT, L1));

        return instructions;
    }

    private List<SInstruction> createInstructionListExpand3() {
        LabelImpl L1 = new LabelImpl(1);
        LabelImpl L2 = new LabelImpl(2);
        LabelImpl L3 = new LabelImpl(3);
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable x2 = new VariableImpl(VariableType.INPUT, 2);

        List<SInstruction> instructions = new ArrayList<>();

        instructions.add(new IncreaseInstruction(x1, L1));
        instructions.add(new IncreaseInstruction(x2, L2));
        instructions.add(new IncreaseInstruction(Variable.RESULT));
        instructions.add(new JumpEqualVariable(Variable.RESULT, x1, L3));
        instructions.add(new ZeroVariableInstruction(Variable.RESULT, L3));
        instructions.add(new IncreaseInstruction(new VariableImpl(VariableType.WORK, 1)));
        instructions.add(new JumpNotZeroInstruction(Variable.RESULT, L1));

        return instructions;
    }

    private List<SInstruction> createInstructionListExpand3_1() {
        List<SInstruction> instructions = new ArrayList<>();
        instructions.add(new JumpEqualVariable(Variable.RESULT, new VariableImpl
                (VariableType.INPUT, 1), FixedLabel.EXIT));

        return instructions;
    }*/




        @Override
    public void loadProgram(String fullPath) {

    }
}
