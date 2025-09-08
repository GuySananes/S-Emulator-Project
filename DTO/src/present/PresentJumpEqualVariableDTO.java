package present;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentJumpEqualVariableDTO extends PresentInstructionTwoVariablesDTO {

    private final Label secondLabel;

    public PresentJumpEqualVariableDTO(
            InstructionData instructionData,
            Variable variable, Variable secondVariable,
            Label label, Label secondLabel, int index, String representation) {
        super(instructionData, variable, secondVariable, label, index, representation);
        this.secondLabel = secondLabel;
    }

    public Label getSecondLabel() {
        return secondLabel;
    }
}
