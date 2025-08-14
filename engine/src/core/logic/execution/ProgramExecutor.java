package core.logic.execution;

import core.logic.variable.Variable;

import java.util.Map;

public interface ProgramExecutor {

    /**
     * Executes the program with the given input (x1, x2, x3...).
     *
     * @param input The input values for the program.
     * @return The result of the program execution.
     */
    long run(Long... input);

    /**
     * Returns the current state of all variables in the program.
     *
     * @return A map where keys are variables and values are their current values.
     */
    Map<Variable, ExecutionContext> variableState();
}