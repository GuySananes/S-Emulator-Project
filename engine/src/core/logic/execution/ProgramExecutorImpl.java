package core.logic.execution;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;

import java.util.Map;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final SProgram program;

    public ProgramExecutorImpl(SProgram program) {
        this.program = program;
    }

    @Override
    public long run(Long... input) {

        ExecutionContext context = null; // create the context with inputs.

        SInstruction currentInstruction = program.getInstructions().get(0);
        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);

            if (nextLabel == FixedLabel.EMPTY) {
                // set currentInstruction to the next instruction in line
            } else if (nextLabel != FixedLabel.EXIT) {
                // need to find the instruction at 'nextLabel' and set current instruction to it
            }
        } while (nextLabel != FixedLabel.EXIT);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> variableState() {
        return Map.of();
    }
}