package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentJumpEqualVariableDTO extends PresentInstructionTwoVariablesDTO {

    private final Label secondLabel;

    public PresentJumpEqualVariableDTO(
            InstructionData instructionData,
            Variable variable, Variable secondVariable,
            Label label, Label secondLabel, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, secondVariable, label, index, representation, parents, parentsRepresentation);
        this.secondLabel = secondLabel;
    }

    public PresentJumpEqualVariableDTO(PresentInstructionDTO presentInstructionDTO, Variable secondVariable, Label secondLabel) {
        super(presentInstructionDTO, secondVariable);
        this.secondLabel = secondLabel;
    }

    public Label getSecondLabel() {
        return secondLabel;
    }
}
