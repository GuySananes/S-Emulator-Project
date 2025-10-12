package present.quote;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;
import present.mostInstructions.PresentInstructionDTO;

import java.util.List;

public class PresentQuoteProgramInstructionDTO extends PresentInstructionDTO {

    private final FunctionArgumentDTO functionArgumentDTO;

    public PresentQuoteProgramInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            FunctionArgumentDTO functionArgumentDTO, int index, String representation, List<PresentInstructionDTO> parents, String parentsRepresentation) {
        super(instructionData, variable, label, index, representation, parents, parentsRepresentation);
        this.functionArgumentDTO = functionArgumentDTO;
    }

    public PresentQuoteProgramInstructionDTO (PresentInstructionDTO presentInstructionDTO, FunctionArgumentDTO functionArgumentDTO) {
        super(presentInstructionDTO);
        this.functionArgumentDTO = functionArgumentDTO;
    }


    public FunctionArgumentDTO getFunctionArgumentDTO() {
        return functionArgumentDTO;
    }
}
