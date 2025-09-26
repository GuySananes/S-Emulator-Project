package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentConstantAssignmentInstructionDTO extends PresentInstructionDTO {
    private final long constantValue;

    public PresentConstantAssignmentInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label, long constantValue, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, label, index, representation, parents, parentsRepresentation);
        this.constantValue = constantValue;
    }

    public PresentConstantAssignmentInstructionDTO(PresentInstructionDTO presentInstructionDTO, long constantValue) {
        super(presentInstructionDTO);
        this.constantValue = constantValue;
    }

    public long getConstantValue() {
        return constantValue;
    }
}
