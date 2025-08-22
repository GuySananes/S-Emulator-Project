
package core.logic.execution;

import core.logic.variable.Variable;

import java.util.Map;

public interface ProgramExecutor {

    long run(java.lang.Long... input);

    Map<Variable, Long> variableState();
}
