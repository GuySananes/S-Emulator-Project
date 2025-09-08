package present;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class PresentInstructionDTO {

    private final InstructionData instructionData;
    private final Variable variable;
    private final Label label;
    private final int index;
    private final String representation;

    public PresentInstructionDTO(InstructionData instructionData, Variable variable, Label label, int index, String representation) {
        this.instructionData = instructionData;
        this.variable = variable;
        this.label = label;
        this.index = index;
        this.representation = representation;
    }

    public InstructionData getInstructionData() {
        return instructionData;
    }

    public Variable getVariable() {
        return variable;
    }

    public Label getLabel() {
        return label;
    }

    public String getRepresentation() {
        return representation;
    }

    public int getIndex() {
        return index;
    }
}
