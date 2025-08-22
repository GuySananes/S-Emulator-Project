package core.logic.execution;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;

import java.util.HashMap;
import java.util.Map;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final SProgram program;

    public ProgramExecutorImpl(SProgram program) {
        this.program = program;
    }

    @Override
    public long run(Long... input) {

        ExecutionContext context = new ExecutionContextImpl();

        // Start from the first instruction
        int currentInstructionIndex = 0;
        SInstruction currentInstruction = program.getInstructionList().get(currentInstructionIndex);
        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);

            if (nextLabel == FixedLabel.EMPTY) {
                // Move to next instruction by index
                currentInstructionIndex++;
                currentInstruction = currentInstructionIndex < program.getInstructionList().size()
                        ? program.getInstructionList().get(currentInstructionIndex)
                        : null;
            } else if (nextLabel != FixedLabel.EXIT) {
                // Find instruction by label
                currentInstruction = program.getInstructionByLabel(nextLabel);
                if (currentInstruction == null) {
                    throw new IllegalStateException("No instruction found for label: " + nextLabel);
                }
                // Update the current index to match the found instruction
                currentInstructionIndex = program.getInstructionList().indexOf(currentInstruction);
            }
        } while (nextLabel != FixedLabel.EXIT && currentInstruction != null);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> variableState() {
        Map<Variable, Long> variableValues = new HashMap<Variable, Long>();

        for (SInstruction instruction : program.getInstructionList()) {
            Variable variable = instruction.getVariable();
            if (variable != null) {
                variableValues.put(variable, variable.getValue());
            }
        }
        return variableValues;
    }
}