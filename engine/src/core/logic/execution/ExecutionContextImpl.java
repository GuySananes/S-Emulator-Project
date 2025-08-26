package core.logic.execution;

import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExecutionContextImpl implements ExecutionContext {

    private final Map<Variable, Long> variableValues;

    public ExecutionContextImpl(SProgram program){
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating ExecutionContext");
        }

        variableValues = new TreeMap<>();
        Set<Variable> variables = program.getOrderedVariables();
        for(Variable variable : variables){
            variableValues.put(variable, 0L);
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

            if (variableValues.containsKey(variable)) {
                variableValues.put(variable, value);
            }
        }
    }


    @Override
    public long getVariableValue(Variable variable) {
        if(!variableValues.containsKey(variable)){
            throw new IllegalArgumentException("Variable "
                    + variable.getRepresentation() +
                    " not found in context when getting value");
        }

        return variableValues.get(variable);
    }

    @Override
    public void updateVariable(Variable variable, long value) {
        if(!variableValues.containsKey(variable)){
            throw new IllegalArgumentException("Variable "
                    + variable.getRepresentation() +
                    " not found in context when updating value");
        }

        variableValues.put(variable, value);

    }
}
