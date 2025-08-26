package core.logic.execution;

import core.logic.variable.Variable;
import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext {

    private Map<Variable, Long> variableValues;

    @Override
    public long getVariableValue(Variable v) {
        // Implementation to retrieve the value of the variable
        return 0; // Placeholder return value
    }

    @Override
    public void updateVariable(Variable v, long value) {
        // Implementation to update the variable with the new value
    }
}
