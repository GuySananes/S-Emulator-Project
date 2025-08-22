package core.logic.execution;

import core.logic.*;
import core.logic.variable.Variable;

public class ExecutionContextImpl implements ExecutionContext {

    @Override
    public long getVariableValue(Variable v) {
        if (v == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }
        return v.getValue();
    }

    @Override
    public void updateVariable(Variable v, long value) {
        if (v == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }
        v.setValue(value);

    }
}