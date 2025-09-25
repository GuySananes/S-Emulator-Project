package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentConstantAssignmentInstructionDTO extends PresentInstructionDTO {
    private final long constantValue;

    public PresentConstantAssignmentInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label, long constantValue, int index, String representation) {
        super(instructionData, variable, label, index, representation);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
