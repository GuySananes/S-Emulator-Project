package DTO;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentJumpEqualConstantInstructionDTO extends PresentInstructionTwoLabelsDTO {
    private final long constantValue;

    public PresentJumpEqualConstantInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            Label secondLabel, long constantValue, String representation, int index) {
        super(instructionData, variable, label, secondLabel, representation, index);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
