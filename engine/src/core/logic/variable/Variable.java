package core.logic.variable;

public interface Variable {
    VariableType getType();
    String getRepresentation();

    /* public static final ...
    * There is only one RESULT (y) in a program
    * Therefore -> public static object in the interface
    * */
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);

    long getValue();
    void setValue(long value);

}