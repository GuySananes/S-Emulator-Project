package present;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentInstructionTwoVariablesDTO extends PresentInstructionDTO {
    private final Variable secondVariable;

    public PresentInstructionTwoVariablesDTO
            (InstructionData instructionData,
             Variable variable, Variable secondVariable,
             Label label, String representation, int index) {
        super(instructionData, variable, label, representation, index);
        this.secondVariable = secondVariable;
    }

    public Variable getSecondVariable() {
        return secondVariable;
    }
}
