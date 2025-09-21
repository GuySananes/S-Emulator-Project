package core.logic.variable;

import core.logic.instruction.quoteInstruction.Argument;

public interface Variable extends Comparable<Variable>, Argument {
    int getNumber();
    VariableType getType();
    String getRepresentation();
    Variable copy();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);


}