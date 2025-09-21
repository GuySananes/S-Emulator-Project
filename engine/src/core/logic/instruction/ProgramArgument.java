package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ExecutionResult;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;

import java.util.List;

public class ProgramArgument implements Argument {
    private final SProgram program;
    private final List<Argument> arguments;

    public ProgramArgument(SProgram program, List<Argument> arguments) {
        this.program = program;
        this.arguments = arguments;
    }

    @Override
    public ExecutionResult evaluate(ExecutionContext context) {
        ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
        ExecutionResult result;
        int totalCycles = 0;
        Long[] input = new Long[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            result = arguments.get(i).evaluate(context);
            input[i] = result.getResult();
            totalCycles += result.getCycles();
        }

        result = executor.run(input);
        totalCycles += result.getCycles();
        return new ExecutionResult(result.getResult(), totalCycles);
    }
}
