package present.program;

import core.logic.label.Label;
import core.logic.variable.Variable;
import present.mostInstructions.PresentInstructionDTO;

import java.util.List;
import java.util.Set;

public class PresentFunctionDTO extends PresentProgramDTO {
    private final String userName;

    public PresentFunctionDTO(String userName, String programName, Set<Variable> Xs, Set<Label> labels,
                              List<PresentInstructionDTO> instructionList, String representation) {
        super(programName, Xs, labels, instructionList, representation);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
