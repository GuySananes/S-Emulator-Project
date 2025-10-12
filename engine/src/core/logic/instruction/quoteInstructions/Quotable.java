package core.logic.instruction.quoteInstructions;

import core.logic.variable.Variable;
import expansion.ExpansionContext;

import java.util.Map;

public interface Quotable {
    FunctionArgument getFunctionArgument();
    void setVariablesInFunctionArgument(Map<Variable, Variable> xyzToz, ExpansionContext context);

}
