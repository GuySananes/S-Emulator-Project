
package core.logic.instruction.quoteInstructions;

import core.logic.variable.Variable;
import core.logic.execution.ExecutionContext;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import core.logic.program.ContextPrograms;  // ADD THIS IMPORT
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import expansion.ExpansionContext;

import java.util.*;

public class FunctionArgument implements Argument {
    private SProgram program;  // Remove 'final' keyword
    private final String functionName;  //Store function name
    private boolean isSystemFunction;  //Track if it's a system function
    private final List<Argument> arguments;

    // CONSTRUCTOR: For local functions
    public FunctionArgument(SProgram program, List<Argument> arguments) {
        if (program == null) {
            throw new IllegalArgumentException("Program cannot be null for local functions");
        }
        this.program = program;
        this.functionName = program.getName();
        this.isSystemFunction = false;
        this.arguments = new ArrayList<>(arguments);
    }

    //COPY CONSTRUCTOR
    public FunctionArgument(FunctionArgument other) {
        this.program = other.program;
        this.functionName = other.functionName;
        this.isSystemFunction = other.isSystemFunction;
        this.arguments = new ArrayList<>();
        for (Argument arg : other.arguments) {
            if (arg instanceof Variable) {
                this.arguments.add(((Variable) arg).deepCopy());  // FIXED: Use deepCopy for Variables
            } else if (arg instanceof FunctionArgument) {
                this.arguments.add(new FunctionArgument((FunctionArgument) arg));  // FIXED: Use copy constructor
            } else {
                this.arguments.add(arg);
            }
        }
    }

    //For system functions
    public FunctionArgument(String functionName, List<Argument> arguments) {
        this.functionName = functionName;
        this.program = null;  // Will be resolved later
        this.isSystemFunction = true;
        this.arguments = new ArrayList<>(arguments);
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



    //Add validation for unresolved system functions
    public SProgram getProgram() {
        if (isSystemFunction && program == null) {
            throw new IllegalStateException(
                    "System function '" + functionName + "' has not been resolved yet. " +
                            "Call resolveSystemFunctions() on the SProgram after loading."
            );
        }
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
        if (program == null) {
            throw new IllegalStateException(
                    "Cannot get degree of function '" + functionName + "': " +
                            "Function has not been resolved yet. isSystemFunction=" + isSystemFunction
            );
        }

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

    // NEW: Check if this is a system function that needs resolution
    public boolean isSystemFunction() {
        return isSystemFunction;
    }

    // NEW: Get the function name (useful for debugging and resolution)
    public String getFunctionName() {
        return functionName;
    }

    public void resolveSystemFunction(ContextPrograms contextPrograms) {
        if (!isSystemFunction) {
            return;  // Already a local function, nothing to do
        }

        if (program != null) {
            return;  // Already resolved
        }

        // Look up the function in the system
        Map<String, SProgram> nameToProgram = contextPrograms.getNameToProgram();
        SProgram resolvedProgram = nameToProgram.get(functionName);

        if (resolvedProgram == null) {
            throw new RuntimeException(
                    "System function '" + functionName + "' not found in ContextPrograms. " +
                            "Available functions: " + nameToProgram.keySet()
            );
        }

        System.out.println("DEBUG FunctionArgument: Resolved '" + functionName + "' to program: " + resolvedProgram.getName());
        this.program = resolvedProgram;
        this.isSystemFunction = false;  // MARK AS RESOLVED - no longer a system function
    }

    public void resolveSystemFunctionsRecursively(ContextPrograms contextPrograms) {
        System.out.println("DEBUG FunctionArgument: Resolving '" + functionName + "' (isSystem=" + isSystemFunction + ", program=" + (program != null ? program.getName() : "null") + ")");

        // Resolve this function if needed
        resolveSystemFunction(contextPrograms);

        // Recursively resolve nested function arguments
        for (Argument arg : arguments) {
            if (arg instanceof FunctionArgument funcArg) {
                funcArg.resolveSystemFunctionsRecursively(contextPrograms);
            }
        }
    }
}