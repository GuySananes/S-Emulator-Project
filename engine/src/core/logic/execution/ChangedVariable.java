package core.logic.execution;

import core.logic.variable.Variable;

public class ChangedVariable {
    private final Variable variable;
    private final long oldValue;
    private final long newValue;

    public ChangedVariable(Variable variable, long oldValue, long newValue) {
        this.variable = variable;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Variable getVariable() {
        return variable;
    }

    public long getOldValue() {
        return oldValue;
    }

    public long getNewValue() {
        return newValue;
    }


}
