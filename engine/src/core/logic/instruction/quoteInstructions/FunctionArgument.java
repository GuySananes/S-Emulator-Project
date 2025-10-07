package core.logic.instruction.quoteInstructions;

import core.logic.variable.Variable;
import core.logic.execution.ExecutionContext;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import expansion.ExpansionContext;

import java.util.*;

public class FunctionArgument implements Argument {
    private final SProgram program;
    private final List<Argument> arguments;

    public FunctionArgument(SProgram program, List<Argument> arguments) {
        this.program = program;
        this.arguments = arguments;
    }

    public FunctionArgument(FunctionArgument other) {
        this.program = other.program;
        this.arguments = new ArrayList<>();
        this.arguments.addAll(other.arguments);
    }

    public void setArgumentsThatAreVariable(Map<Variable, Variable> xyzToz, ExpansionContext context) {
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            if(arg instanceof Variable var) {
                Variable z;
                if(!xyzToz.containsKey(var)){
                    z = context.generateZ();
                    xyzToz.put(var, z);
                } else {
                    z = xyzToz.get(var);
                }
                arguments.set(i, (Argument)z);
            }
            else if(arg instanceof FunctionArgument fa) {
                fa.setArgumentsThatAreVariable(xyzToz, context);
            }
        }
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
        int maxDegree = 0;
        for (Argument argument : arguments) {
            if(argument instanceof FunctionArgument fa) {
                int degree = fa.getDegree();
                if(degree > maxDegree) {
                    maxDegree = degree;
                }
            }
        }

        return Math.max(maxDegree, program.getDegree());
    }

    public Set<Variable> getVariablesInArgumentList() {
        Set<Variable> variables = new HashSet<>();
        for (Argument argument : arguments) {
            if(argument instanceof FunctionArgument fa) {
                variables.addAll(fa.getVariablesInArgumentList());
            } else {//argument is Variable
                variables.add((Variable) argument);
            }
        }

        return variables;
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

    @Override
    public FunctionArgument clone() {
        return new FunctionArgument(this);
    }
}
