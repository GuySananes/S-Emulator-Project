package present.quote;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;
import present.mostInstructions.PresentInstructionDTO;
import present.mostInstructions.PresentInstructionTwoLabelsDTO;

import java.util.List;

public class PresentJumpEqualFunctionDTO extends PresentInstructionTwoLabelsDTO {

    private final FunctionArgumentDTO functionArgumentDTO;

    public PresentJumpEqualFunctionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            Label secondLabel, FunctionArgumentDTO functionArgumentDTO,
            int index, String representation, List<PresentInstructionDTO> parents,
            String parentsRepresentation) {
        super(instructionData, variable, label, secondLabel, index, representation, parents, parentsRepresentation);
        this.functionArgumentDTO = functionArgumentDTO;
    }

    public PresentJumpEqualFunctionDTO(PresentInstructionDTO presentInstructionDTO,
                                       Label secondLabel,
                                       FunctionArgumentDTO functionArgumentDTO) {
        super(presentInstructionDTO, secondLabel);
        this.functionArgumentDTO = functionArgumentDTO;
    }
}
