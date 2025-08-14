package core.logic.execution;

import core.logic.variable.Variable;

import java.util.Map;

public interface ProgramExecutor {

    long run(Long... input);
    Map<Variable, Long> variableState();
}