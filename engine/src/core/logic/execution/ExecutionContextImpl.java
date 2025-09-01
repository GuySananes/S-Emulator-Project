package core.logic.execution;

import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

public class ExecutionContextImpl implements ExecutionContext {

    private final Map<Variable, Long> variablesToValues;

    public ExecutionContextImpl(SProgram program){
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating ExecutionContext");
        }

        variablesToValues = new TreeMap<>();
        Set<Variable> variables = program.getOrderedVariables();
        for(Variable variable : variables){
            variablesToValues.put(variable, 0L);
        }
    }

    @Override
    public void updateInputVariables(Long... inputVariables) {

        for (int i = 0; i < inputVariables.length; i++) {
            Long value = inputVariables[i];

            if (value == null) {
                continue;
            }

            Variable variable = new VariableImpl(VariableType.INPUT, i + 1);

            if (variablesToValues.containsKey(variable)) {
                variablesToValues.put(variable, value);
            }
        }
    }

    @Override
    public long getVariableValue(Variable variable) {
        if(!variablesToValues.containsKey(variable)){
            throw new IllegalArgumentException("Variable "
                    + variable.getRepresentation() +
                    " not found in context when getting value");
        }

        return variablesToValues.get(variable);
    }

    @Override
    public void updateVariable(Variable variable, long value) {
        if(!variablesToValues.containsKey(variable)){
            throw new IllegalArgumentException("Variable "
                    + variable.getRepresentation() +
                    " not found in context when updating value");
        }

        variablesToValues.put(variable, value);

    }

    @Override
    public List<Long> getOrderedValuesCopy(Set<Variable> orderedVariables) {
        List<Long> result = new ArrayList<>(orderedVariables.size());
        for (Variable var : orderedVariables) {
            Long value = variablesToValues.get(var);
            if (value == null) {
                throw new IllegalArgumentException("Variable "
                        + var.getRepresentation() + " not found in context");
            }
            result.add(value);
        }

        return Collections.unmodifiableList(result);
    }
}
