package core.logic.execution;

import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

public interface ExecutionContext {

    long getVariableValue(Variable variable);

    void updateVariable(Variable variable, long value);

    void updateInputVariables(Long... inputVariables);

    List<Long> getOrderedValuesCopy(Set<Variable> orderedVariables);
}