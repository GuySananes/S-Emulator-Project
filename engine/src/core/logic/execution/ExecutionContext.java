package core.logic.execution;

import core.logic.variable.Variable;

public interface ExecutionContext {

    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
}