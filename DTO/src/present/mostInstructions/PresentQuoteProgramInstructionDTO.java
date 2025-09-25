package present.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;
import present.quote.FunctionArgumentDTO;

public class PresentQuoteProgramInstructionDTO extends PresentInstructionDTO {

    private final FunctionArgumentDTO functionArgumentDTO;

    public PresentQuoteProgramInstructionDTO(
            InstructionData instructionData,
            Variable variable, Label label,
            FunctionArgumentDTO functionArgumentDTO, int index, String representation) {
        super(instructionData, variable, label, index, representation);
        this.functionArgumentDTO = functionArgumentDTO;
    }

    public FunctionArgumentDTO getFunctionArgumentDTO() {
        return functionArgumentDTO;
    }
}
