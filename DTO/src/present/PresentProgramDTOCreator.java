package present;

import core.logic.instruction.IndexedInstruction;
import core.logic.program.SProgram;
import util.Util;

import java.util.List;
import java.util.stream.Collectors;


public class PresentProgramDTOCreator {

    public static PresentProgramDTO create(SProgram program){
        List<IndexedInstruction> indexedInstructions = Util.makeIndexedInstructionList(program);
        List<PresentInstructionDTO> presentInstructionDTOList =
                indexedInstructions.stream()
                .map(PresentInstructionDTOCreator::create)
                .collect(Collectors.toList());


        return new PresentProgramDTO(
                program.getName(),
                program.getOrderedVariablesCopy(),
                program.getOrderedLabels(),
                presentInstructionDTOList, program.getRepresentation());

    }
}
