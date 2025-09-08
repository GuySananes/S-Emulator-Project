package present;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentJumpEqualConstantInstructionDTO extends PresentInstructionTwoLabelsDTO {
    private final long constantValue;

    public PresentJumpEqualConstantInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            Label secondLabel, long constantValue, int index, String representation) {
        super(instructionData, variable, label, secondLabel, index, representation);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
