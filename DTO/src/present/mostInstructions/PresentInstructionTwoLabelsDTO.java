package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentInstructionTwoLabelsDTO extends PresentInstructionDTO {
    private final Label secondLabel;

    public PresentInstructionTwoLabelsDTO(InstructionData instructionData,
                                          Variable variable, Label label,
                                          Label secondLabel, int index, String representation) {
        super(instructionData, variable, label, index, representation);
        this.secondLabel = secondLabel;
    }

    public Label getSecondLabel() {
        return secondLabel;
    }
}
