package DTO;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentConstantAssignmentInstructionDTO extends PresentInstructionDTO {
    private final long constantValue;

    public PresentConstantAssignmentInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label, long constantValue, String representation, int index) {
        super(instructionData, variable, label, representation, index);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
