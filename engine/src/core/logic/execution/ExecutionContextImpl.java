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

public class ExecutionContextImpl implements ExecutionContext {

    private final Map<Variable, Long> variablesToValues;
    private final List<Variable> inputVariables;

    public ExecutionContextImpl(SProgram program){
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

    @Override
    public void updateInputVariables(List<Long> input) {
        if(input == null){
            return;
        }
        for (int i = 0; i < input.size(); i++) {
            Long value = input.get(i);
            if (value < 0) {
                throw new IllegalArgumentException("In ExecutionContextImpl::updateInputVariables: Input list cannot contain negative values");
            }

            variablesToValues.put(inputVariables.get(i), value);
        }
    }

    @Override
    public long getVariableValue(Variable variable) {
        return variablesToValues.get(variable);
    }

    @Override
    public void updateVariable(Variable variable, long value) {
        variablesToValues.put(variable, value);
    }

    @Override
    public List<Long> getVariableValues(Set<Variable> orderedVariables) {
        List<Long> result = new ArrayList<>(orderedVariables.size());
        for (Variable var : orderedVariables) {
            Long value = variablesToValues.get(var);
            result.add(value);
        }

        return result;
    }
}
