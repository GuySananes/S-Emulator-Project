package core.logic.execution;

import core.logic.program.SProgram;
import core.logic.variable.Variable;
import java.util.Map;
import java.util.Set;

public class ExecutionContextImpl implements ExecutionContext {

    private Map<Variable, Long> variableValues;

    public ExecutionContextImpl(SProgram program){


    }

    @Override
    public long getVariableValue(Variable variable) {

    }

    @Override
    public void updateVariable(Variable variable, long value) {
    }
}
