package core.logic.execution;

import core.logic.program.SProgram;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;

public class ExecutionContext {

    private final Map<Variable, Long> variablesToValues;
    private final List<Variable> inputVariables;

    public ExecutionContext(SProgram program){
        inputVariables = new ArrayList<>(program.getOrderedInputVariables());
        variablesToValues = new TreeMap<>();
        Set<Variable> variables = program.getOrderedVariables();
        for(Variable variable : variables){
            variablesToValues.put(variable, 0L);
        }
        if(!variablesToValues.containsKey(Variable.RESULT)) {
            variablesToValues.put(Variable.RESULT, 0L);
        }
    }

    public void updateInputVariables(List<Long> input) {
        for (int i = 0; i < input.size(); i++) {
            if(i >= inputVariables.size()) {
                return;
            }
            Long value = input.get(i);
            if (value < 0) {
                throw new IllegalArgumentException("In ExecutionContextImpl::updateInputVariables: Input list cannot contain negative values");
            }
            variablesToValues.put(inputVariables.get(i), value);
        }
    }

    public long getVariableValue(Variable variable) {
        return variablesToValues.get(variable);
    }

    public void updateVariable(Variable variable, long value) {
        variablesToValues.put(variable, value);
    }

    public List<Long> getVariablesValues(Set<Variable> orderedVariables) {
        List<Long> result = new ArrayList<>(orderedVariables.size());
        for (Variable var : orderedVariables) {
            Long value = variablesToValues.get(var);

            // Debug logging
            if (value == null) {
                System.err.println("WARNING: Variable " + var.getRepresentation() +
                        " not found in context! Type=" + var.getType() + ", Number=" + var.getNumber());
                System.err.println("Available variables in context:");
                for (Variable key : variablesToValues.keySet()) {
                    System.err.println("  - " + key.getRepresentation() +
                            " (Type=" + key.getType() + ", Number=" + key.getNumber() +
                            ", Value=" + variablesToValues.get(key) + ")");
                }
            }

            result.add(value != null ? value : 0L);
        }

        return result;
    }
}
