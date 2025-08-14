package core.logic.execution;

import core.logic.variable.Variable;

public interface ExecutionContext {

    long getVariableValue(Variable v);
    void updateVariable(Variable v, long value);
}