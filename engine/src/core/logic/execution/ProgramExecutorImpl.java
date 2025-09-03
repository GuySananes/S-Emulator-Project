package core.logic.execution;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.ProgramNotExecutedYetException;

import java.util.List;

public class ProgramExecutorImpl implements ProgramExecutor {

    private final SProgram program;
    private int currentInstructionIndex;
    private ExecutionContext context;

    public ProgramExecutorImpl(SProgram program) {
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating ProgramExecutorImpl");
        }
        if(program.getInstructionList().isEmpty()){
            throw new IllegalArgumentException("Program must have at least one instruction");
        }
        this.program = program;
        this.currentInstructionIndex = 0;
    }

    @Override
    public long run(Long... input) {
        context = new ExecutionContextImpl(program);
        context.updateInputVariables(input);
        currentInstructionIndex = 0;

        List<SInstruction> instructions = program.getInstructionList();
        if (instructions.isEmpty()) {
            throw new IllegalStateException("Cannot execute empty program");
        }

        SInstruction currentInstruction = instructions.get(currentInstructionIndex);
        Label nextLabel;
        int maxIterations = 10000; // Prevent infinite loops
        int iterationCount = 0;

        do {
            if (iterationCount++ > maxIterations) {
                throw new RuntimeException("Program execution exceeded maximum iterations (possible infinite loop)");
            }

            nextLabel = currentInstruction.execute(context);

            if (nextLabel == FixedLabel.EMPTY) {
                currentInstructionIndex++;
                currentInstruction = currentInstructionIndex < instructions.size()
                        ? instructions.get(currentInstructionIndex)
                        : null;
            } else if (nextLabel != FixedLabel.EXIT && !"EXIT".equals(nextLabel.getRepresentation())) {
                currentInstruction = program.getInstructionByLabel(nextLabel);
                if (currentInstruction == null) {
                    throw new RuntimeException("Invalid label reference: " + nextLabel);
                }
                currentInstructionIndex = instructions.indexOf(currentInstruction);
                if (currentInstructionIndex == -1) {
                    throw new RuntimeException("Instruction not found in program: " + currentInstruction);
                }
            } else {
                // Either FixedLabel.EXIT or "EXIT" label - terminate the program
                currentInstruction = null;
            }
        } while (nextLabel != FixedLabel.EXIT && !"EXIT".equals(nextLabel.getRepresentation()) && currentInstruction != null);

        // Ensure we have a result variable
        if (Variable.RESULT == null) {
            throw new RuntimeException("Result variable not defined");
        }

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(context == null){
            throw new ProgramNotExecutedYetException(); // Use no-argument constructor
        }
        return context.getOrderedValuesCopy(program.getOrderedVariables());
    }
}
