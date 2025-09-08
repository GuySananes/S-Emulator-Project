package present;

import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;
import java.util.List;
import java.util.stream.Collectors;


public class PresentProgramDTOCreator {

    public static PresentProgramDTO create(SProgram program){
        List<SInstruction> instructions = program.getInstructionList();
        List<PresentInstructionDTO> presentInstructionDTOList =
                instructions.stream()
                .map(PresentInstructionDTOCreator::create)
                .collect(Collectors.toList());


        return new PresentProgramDTO(
                program.getName(),
                program.getInputVariablesCopy(),
                program.getOrderedLabels(),
                presentInstructionDTOList, program.getRepresentation());

    }
}
