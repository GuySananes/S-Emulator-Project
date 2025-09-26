package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;

public class PresentInstructionTwoVariablesDTO extends PresentInstructionDTO {
    private final Variable secondVariable;

    public PresentInstructionTwoVariablesDTO
            (InstructionData instructionData,
             Variable variable, Variable secondVariable,
             Label label, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, label, index, representation, parents, parentsRepresentation);
        this.secondVariable = secondVariable;
    }

    public PresentInstructionTwoVariablesDTO(PresentInstructionDTO presentInstructionDTO, Variable secondVariable) {
        super(presentInstructionDTO);
        this.secondVariable = secondVariable;
    }

    public Variable getSecondVariable() {
        return secondVariable;
    }
}
