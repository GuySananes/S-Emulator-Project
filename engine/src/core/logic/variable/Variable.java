package core.logic.variable;

public interface Variable extends Comparable<Variable> {
    int getNumber();
    VariableType getType();
    String getRepresentation();
    Variable copy();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}