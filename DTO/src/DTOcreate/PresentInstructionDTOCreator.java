package DTOcreate;

import DTO.PresentInstructionDTO;
import core.logic.instruction.SInstruction;

public class PresentInstructionDTOCreator {

    public static PresentInstructionDTO create(SInstruction instruction) {
        return new PresentInstructionDTO(
                instruction.getInstructionData(),
                instruction.getVariableCopy(),
                instruction.getLabel()
        );
    }
}
