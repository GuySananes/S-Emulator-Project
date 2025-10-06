package core.logic.execution;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManager;
import java.util.List;

public class Debug{

    private final SProgram program;
    private final SProgram originalProgram;
    private int nextInstructionIndex = 0;
    Label nextLabel = FixedLabel.EMPTY;
    SInstruction currentInstruction = null;
    int totalCycles = 0;
    LabelCycleChangedVariable labelCycleChangedVariable;
    private final ExecutionContext context;
    List<Long> input = null;
    List<SInstruction> instructions;
    int maxIterations = 1000000;
    int iterationCount = 0;

    private final StatisticManager statisticManager = StatisticManager.getInstance();

    public Debug(SProgram program) {
        this.program = program;
        this.originalProgram = program.getOriginalProgram();
        this.context = new ExecutionContext(program);
        this.nextInstructionIndex = 0;
        this.instructions = program.getInstructionList();
    }

    public void setInput(List<Long> input) {
        this.input = input;
        context.updateInputVariables(input);
    }

    public DebugResult nextStep() {
        if (nextInstructionIndex >= 0 && nextInstructionIndex < instructions.size()) {
            currentInstruction = instructions.get(nextInstructionIndex);
        } else {
            currentInstruction = null;
        }

        if (nextLabel == FixedLabel.EXIT || currentInstruction == null) {
            long result = context.getVariableValue(Variable.RESULT);
            statisticManager.incrementRunCount(originalProgram.getName());
            statisticManager.addRunStatistic(
                    originalProgram.getName(),
                    new SingleRunStatisticImpl(statisticManager.getRunCount(originalProgram.getName()),
                            originalProgram.getDegree() - program.getDegree(),
                            input, result, totalCycles));
            return new DebugFinalResult(result, totalCycles);
        }

        if (iterationCount++ > maxIterations) {
            throw new RuntimeException("Program execution exceeded maximum iterations (possible infinite loop)");
        }

        labelCycleChangedVariable = currentInstruction.execute(context);
        nextLabel = labelCycleChangedVariable.getLabel();
        totalCycles += labelCycleChangedVariable.getCycles();

        if (nextLabel == FixedLabel.EMPTY) {
            if (++nextInstructionIndex >= instructions.size()) {
                long result = context.getVariableValue(Variable.RESULT);
                statisticManager.incrementRunCount(originalProgram.getName());
                statisticManager.addRunStatistic(
                        originalProgram.getName(),
                        new SingleRunStatisticImpl(
                                statisticManager.getRunCount(originalProgram.getName()),
                                originalProgram.getDegree() - program.getDegree(),
                                input, result, totalCycles
                        )
                );
                return new DebugFinalResult(result, totalCycles);
            } else {
                currentInstruction = instructions.get(nextInstructionIndex);
            }
        } else if (nextLabel != FixedLabel.EXIT) {
            currentInstruction = program.getInstructionByLabel(nextLabel);
            if (currentInstruction == null) {
                throw new RuntimeException("Invalid label reference: " + nextLabel);
            }
            nextInstructionIndex = instructions.indexOf(currentInstruction);
            if (nextInstructionIndex == -1) {
                throw new RuntimeException("Instruction not found in program: " + currentInstruction);
            }
        } else {
            long result = context.getVariableValue(Variable.RESULT);
            statisticManager.incrementRunCount(originalProgram.getName());
            statisticManager.addRunStatistic(
                    originalProgram.getName(),
                    new SingleRunStatisticImpl(
                            statisticManager.getRunCount(originalProgram.getName()),
                            originalProgram.getDegree() - program.getDegree(),
                            input, result, totalCycles
                    )
            );
            return new DebugFinalResult(result, totalCycles);
        }

        return new DebugResult(labelCycleChangedVariable.getChangedVariable(),
                nextInstructionIndex, totalCycles);
    }

    public DebugFinalResult resume() {
        DebugResult nextStepResult;
        while(true) {
            nextStepResult = nextStep();
            if (nextStepResult instanceof DebugFinalResult) {
                return (DebugFinalResult) nextStepResult;
            }
        }
    }

    public List<Long> getOrderedInputValues() {
        return context.getVariablesValues(program.getOrderedInputVariables());
    }

    public List<Long> getOrderedValues() {
        return context.getVariablesValues(program.getOrderedVariables());
    }
}
