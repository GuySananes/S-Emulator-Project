
package core.logic.execution;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.NoProgramException;
import exception.ProgramNotExecutedYetException;

import java.util.List;

public class ProgramExecutorImpl implements ProgramExecutor {

    private final SProgram program;
    private int currentInstructionIndex;
    private ExecutionContext context;

    public ProgramExecutorImpl(SProgram program) {
        if(program == null){
            throw new IllegalArgumentException("Program cannot " +
                    "be null when creating ProgramExecutorImpl");
        }
        this.program = program;
        this.currentInstructionIndex = 0;
    }

    @Override
    public long run(Long... input)  {
        context = new ExecutionContextImpl(program);
        context.updateInputVariables(input);
        currentInstructionIndex = 0;
        SInstruction currentInstruction = program.getInstructionList()
                .get(currentInstructionIndex);
        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);

            if (nextLabel == FixedLabel.EMPTY) {
                currentInstructionIndex++;
                currentInstruction = currentInstructionIndex <
                        program.getInstructionList().size()
                        ? program.getInstructionList().get(currentInstructionIndex)
                        : null;
            } else if (nextLabel != FixedLabel.EXIT) {
                currentInstruction = program.getInstructionByLabel(nextLabel);
                currentInstructionIndex = program.getInstructionList().
                        indexOf(currentInstruction);
            }
        } while (nextLabel != FixedLabel.EXIT && currentInstruction != null);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(context == null){
            throw new ProgramNotExecutedYetException();
        }
        return context.getOrderedValuesCopy(program.getOrderedVariables());
    }
}
