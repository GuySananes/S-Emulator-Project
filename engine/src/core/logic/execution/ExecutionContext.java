package core.logic.execution;

import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

public interface ExecutionContext {

    long getVariableValue(Variable variable);

    void updateVariable(Variable variable, long value);

    void updateInputVariables(List<Long> inputVariables);

    List<Long> getOrderedValues(Set<Variable> orderedVariables);
}