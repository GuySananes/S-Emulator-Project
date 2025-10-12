package core.logic.execution;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManager;
import java.util.List;

public class ProgramExecutor {

    private final SProgram program;
    private final SProgram originalProgram;
    private int currentInstructionIndex;
    private final ExecutionContext context;
    private final StatisticManager statisticManager = StatisticManager.getInstance();

    public ProgramExecutor(SProgram program) {
        this.program = program;
        this.originalProgram = program.getOriginalProgram();
        this.currentInstructionIndex = 0;
        this.context = new ExecutionContext(program);
    }

    public ResultCycle run(List<Long> input) {
        int cycles = 0;
        if(!program.getInstructionList().isEmpty()) {
            context.updateInputVariables(input);
            currentInstructionIndex = 0;
            List<SInstruction> instructions = program.getInstructionList();
            SInstruction currentInstruction = instructions.get(currentInstructionIndex);
            LabelCycleChangedVariable labelCycleChangedVariable;
            Label nextLabel;
            int maxIterations = 1000000;
            int iterationCount = 0;

            do {
                if (iterationCount++ > maxIterations) {
                    throw new RuntimeException("Program execution exceeded maximum iterations (possible infinite loop)");
                }

                labelCycleChangedVariable = currentInstruction.execute(context);
                nextLabel = labelCycleChangedVariable.getLabel();
                cycles += labelCycleChangedVariable.getCycles();

                if (nextLabel == FixedLabel.EMPTY) {
                    currentInstructionIndex++;
                    currentInstruction = currentInstructionIndex < instructions.size()
                            ? instructions.get(currentInstructionIndex)
                            : null;
                } else if (!"EXIT".equals(nextLabel.getRepresentation())) {
                    currentInstruction = program.getInstructionByLabel(nextLabel);
                    if (currentInstruction == null) {
                        throw new RuntimeException("Invalid label reference: " + nextLabel);
                    }
                    currentInstructionIndex = instructions.indexOf(currentInstruction);
                    if (currentInstructionIndex == -1) {
                        throw new RuntimeException("Instruction not found in program: " + currentInstruction);
                    }
                } else {
                    currentInstruction = null;
                }
            } while (!"EXIT".equals(nextLabel.getRepresentation()) && currentInstruction != null);
        }

        long result = context.getVariableValue(Variable.RESULT);
        statisticManager.incrementRunCount(originalProgram.getName());
        statisticManager.addRunStatistic(originalProgram.getName(),
                new SingleRunStatisticImpl
                        (statisticManager.getRunCount(originalProgram.getName()),
                        originalProgram.getDegree() - program.getDegree(),
                                input, result, cycles));

        return new ResultCycle(result, cycles);
    }

    public List<Long> getOrderedInputValues() {
        return context.getVariablesValues(program.getOrderedInputVariables());
    }

    public List<Long> getOrderedValues() {
        return context.getVariablesValues(program.getOrderedVariables());
    }
}
