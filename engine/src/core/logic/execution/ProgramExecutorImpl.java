package core.logic.execution;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.ProgramNotExecutedYetException;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManager;
import java.util.List;

public class ProgramExecutorImpl implements ProgramExecutor {

    private final SProgram program;
    private final SProgram originalProgram;
    private int currentInstructionIndex;
    private ExecutionContext context;
    private final StatisticManager statisticManager = StatisticManager.getInstance();

    public ProgramExecutorImpl(SProgram program) {
        this.program = program;
        this.originalProgram = program.getOriginalProgram();
        this.currentInstructionIndex = 0;
    }

    @Override
    public ResultCycle run(List<Long> input) {
        context = new ExecutionContextImpl(program);
        context.updateInputVariables(input);
        currentInstructionIndex = 0;
        int cycles = -1;

        List<SInstruction> instructions = program.getInstructionList();

        SInstruction currentInstruction = instructions.get(currentInstructionIndex);
        LabelCycle labelCycle;
        Label nextLabel;
        int maxIterations = 1000000;
        int iterationCount = 0;

        do {
            if (iterationCount++ > maxIterations) {
                throw new RuntimeException("Program execution exceeded maximum iterations (possible infinite loop)");
            }

            labelCycle = currentInstruction.execute(context);
            nextLabel = labelCycle.getLabel();
            cycles += labelCycle.getCycles();

            if (nextLabel == FixedLabel.EMPTY) {
                currentInstructionIndex++;
                currentInstruction = currentInstructionIndex < instructions.size()
                        ? instructions.get(currentInstructionIndex)
                        : null;
            } else if (nextLabel != FixedLabel.EXIT) {
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
        } while (nextLabel != FixedLabel.EXIT && currentInstruction != null);

        long result = context.getVariableValue(Variable.RESULT);
        statisticManager.incrementRunCount(originalProgram.getName());
        statisticManager.addRunStatistic(originalProgram.getName(),
                new SingleRunStatisticImpl(statisticManager.getRunCount(originalProgram.getName()),
                        originalProgram.getDegree() - program.getDegree(), input, result, cycles));

        return new ResultCycle(result, cycles);
    }








    @Override
    public List<Long> getOrderedValues() throws ProgramNotExecutedYetException {
        if(context == null){
            throw new ProgramNotExecutedYetException(program.getName());
        }
        return context.getVariableValues(program.getOrderedVariables());
    }
}
