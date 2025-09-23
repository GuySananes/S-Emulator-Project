package core.logic.instruction.quoteInstruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ResultCycle;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import java.util.List;

public class FunctionArgument implements Argument {
    private final SProgram program;
    private final List<Argument> arguments;

    public FunctionArgument(SProgram program, List<Argument> arguments) {
        this.program = program;
        this.arguments = arguments;
    }

    public SProgram getProgram() {
        return program;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public String getRepresentation() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(program instanceof SFunction ? ((SFunction)program).getUserName() : program.getName());
        for (Argument arg : arguments) {
            sb.append(",");
            sb.append(arg.getRepresentation());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public ResultCycle evaluate(ExecutionContext context) {
        ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
        ResultCycle result;
        int totalCycles = 0;
        Long[] input = new Long[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            result = arguments.get(i).evaluate(context);
            input[i] = result.getResult();
            totalCycles += result.getCycles();
        }

        result = executor.run(input);
        totalCycles += result.getCycles();
        return new ResultCycle(result.getResult(), totalCycles);
    }

    @Override
    public Argument clone() {
        return this;
    }
}
