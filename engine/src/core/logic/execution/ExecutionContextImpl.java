package core.logic.execution;

public class ExecutionContextImpl implements ExecutionContext {

    private Map<Variable, long> variableValues;

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
