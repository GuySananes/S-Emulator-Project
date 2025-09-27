package core.logic.variable;

import core.logic.instruction.quoteInstructions.Argument;
import present.quote.ArgumentDTO;

public interface Variable extends Comparable<Variable>, Argument, ArgumentDTO {
    int getNumber();
    VariableType getType();
    String getRepresentation();
    Variable deepCopy();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);


}