package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentJumpEqualConstantInstructionDTO extends PresentInstructionTwoLabelsDTO {
    private final long constantValue;

    public PresentJumpEqualConstantInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            Label secondLabel, long constantValue, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, label, secondLabel, index, representation, parents, parentsRepresentation);
        this.constantValue = constantValue;
    }

    public PresentJumpEqualConstantInstructionDTO(PresentInstructionDTO presentInstructionDTO, Label secondLabel, long constantValue) {
        super(presentInstructionDTO, secondLabel);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
