package DTOcreate;

import DTO.PresentInstructionDTO;
import DTO.PresentProgramDTO;
import core.logic.program.SProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class PresentProgramDTOCreator {

    public static PresentProgramDTO create(SProgram program){
        List<PresentInstructionDTO> presentInstructionDTOList =
                program.getInstructionList().stream()
                        .map(PresentInstructionDTOCreator::create)
                        .collect(Collectors.toList());


        return new PresentProgramDTO(
                program.getName(),
                program.getXsCopy(),
                program.getLabels(),
                presentInstructionDTOList);

    }
}
