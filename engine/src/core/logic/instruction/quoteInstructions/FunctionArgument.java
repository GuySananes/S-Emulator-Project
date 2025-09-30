package core.logic.instruction.quoteInstructions;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import core.logic.program.SFunction;
import core.logic.program.SProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        ProgramExecutor executor = new ProgramExecutor(program);
        ResultCycle result;
        int totalCycles = 0;
        List<Long> input = new ArrayList<>(arguments.size());
        for (Argument argument : arguments) {
            result = argument.evaluate(context);
            input.add(result.getResult());
            totalCycles += result.getCycles();
        }

        result = executor.run(input);
        totalCycles += result.getCycles();
        return new ResultCycle(result.getResult(), totalCycles);
    }

    public int getDegree() {
        return program.getDegree();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FunctionArgument that = (FunctionArgument) o;
        return Objects.equals(program, that.program) && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(program, arguments);
    }
}
