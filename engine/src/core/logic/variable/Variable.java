package core.logic.variable;

public interface Variable {
    VariableType getType();
    String getRepresentation();
    Variable copy();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}