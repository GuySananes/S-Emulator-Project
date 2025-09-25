package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentInstructionDTO {

    private final InstructionData instructionData;
    private final Variable variable;
    private final Label label;
    private final int index;
    private final String representation;
    private final List<PresentInstructionDTO> parents;
    private final String parentsRepresentation;

    public PresentInstructionDTO(InstructionData instructionData, Variable variable, Label label, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        this.instructionData = instructionData;
        this.variable = variable;
        this.label = label;
        this.index = index;
        this.representation = representation;
        this.parents = parents;
        this.parentsRepresentation = parentsRepresentation;
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

    public int getIndex() {
        return index;
    }

    public String getRepresentation() {
        return representation;
    }

    public List<PresentInstructionDTO> getParents() {
        return parents;
    }

    public String getParentsRepresentation() {
        return parentsRepresentation;
    }
}
