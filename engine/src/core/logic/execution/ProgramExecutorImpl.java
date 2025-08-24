package core.logic.execution;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;

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

        // Initialize input variables with provided values
        initializeInputVariables(context, input);

        int currentInstructionIndex = 0;
        SInstruction currentInstruction = program.getInstructionList().get(currentInstructionIndex);

        Label nextLabel;
        do {
            assert currentInstruction != null;
            nextLabel = currentInstruction.execute(context);
            currentInstructionIndex = determineNextInstructionIndex(nextLabel, currentInstructionIndex);
            currentInstruction = getInstructionAtIndex(currentInstructionIndex);
        } while (shouldContinueExecution(nextLabel, currentInstruction));

        return context.getVariableValue(Variable.RESULT);
    }

    private void initializeInputVariables(ExecutionContext context, Long... input) {
        for (int i = 0; i < input.length; i++) {
            if (input[i] != null) {
                Variable inputVariable = new VariableImpl(VariableType.INPUT, i + 1);
                context.updateVariable(inputVariable, input[i]);
            }
        }
    }

    private int determineNextInstructionIndex(Label nextLabel, int currentIndex) {
        if (nextLabel == FixedLabel.EMPTY) {
            return currentIndex + 1;
        } else if (nextLabel != FixedLabel.EXIT) {
            SInstruction targetInstruction = program.getInstructionByLabel(nextLabel);
            if (targetInstruction == null) {
                throw new IllegalStateException("No instruction found for label: " + nextLabel);
            }
            return program.getInstructionList().indexOf(targetInstruction);
        }
        return currentIndex; // For EXIT label, keep current index
    }

    private SInstruction getInstructionAtIndex(int index) {
        return index < program.getInstructionList().size()
                ? program.getInstructionList().get(index)
                : null;
    }

    private boolean shouldContinueExecution(Label nextLabel, SInstruction currentInstruction) {
        return nextLabel != FixedLabel.EXIT && currentInstruction != null;
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