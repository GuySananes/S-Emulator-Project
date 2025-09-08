package present;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentInstructionTwoVariablesDTO extends PresentInstructionDTO {
    private final Variable secondVariable;

    public PresentInstructionTwoVariablesDTO
            (InstructionData instructionData,
             Variable variable, Variable secondVariable,
             Label label, int index, String representation) {
        super(instructionData, variable, label, index, representation);
        this.secondVariable = secondVariable;
    }

    public Variable getSecondVariable() {
        return secondVariable;
    }
}
