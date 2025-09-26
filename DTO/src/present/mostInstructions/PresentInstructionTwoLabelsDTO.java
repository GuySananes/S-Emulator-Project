package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentInstructionTwoLabelsDTO extends PresentInstructionDTO {
    private final Label secondLabel;

    public PresentInstructionTwoLabelsDTO(InstructionData instructionData,
                                          Variable variable, Label label,
                                          Label secondLabel, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, label, index, representation, parents, parentsRepresentation);
        this.secondLabel = secondLabel;
    }

    public PresentInstructionTwoLabelsDTO(PresentInstructionDTO presentInstructionDTO, Label secondLabel) {
        super(presentInstructionDTO);
        this.secondLabel = secondLabel;
    }

    public Label getSecondLabel() {
        return secondLabel;
    }
}
