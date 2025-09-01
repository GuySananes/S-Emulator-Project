package core.logic;

import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.instruction.*;
import core.logic.label.LabelImpl;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;


public class Main {

    public static void main(String[] args) {

        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable z2 = new VariableImpl(VariableType.WORK, 1);

        LabelImpl l1 = new LabelImpl(1);
        LabelImpl l2 = new LabelImpl(1);

        SInstruction increase = new IncreaseInstruction(x1, l1);
        SInstruction decrease = new DecreaseInstruction(z2, l2);
        SInstruction noop = new NoOpInstruction(Variable.RESULT);
        SInstruction jnz = new JumpNotZeroInstruction(x1, l2);

        SProgram p = new SProgramImpl("test");
        p.addInstruction(increase);
        p.addInstruction(increase);
        p.addInstruction(decrease);
        p.addInstruction(jnz);

        ProgramExecutor programExecutor = new ProgramExecutorImpl(p);
        long result = programExecutor.run(3L, 6L, 2L);
        System.out.println(result);

        sanity();
    }

    private static void sanity() {
        /*

        {y = x1}

        [L1] x1 ← x1 – 1
             y ← y + 1
             IF x1 != 0 GOTO L1
        * */

        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        LabelImpl l1 = new LabelImpl(1);

        SProgram p = new SProgramImpl("SANITY");
        p.addInstruction(new DecreaseInstruction(x1, l1));
        p.addInstruction(new IncreaseInstruction(Variable.RESULT));
        p.addInstruction(new JumpNotZeroInstruction(x1, l1));

        long result = new ProgramExecutorImpl(p).run(4L);
        System.out.println(result);
    }
}