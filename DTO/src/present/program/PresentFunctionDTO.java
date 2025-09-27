package present.program;

import core.logic.label.Label;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import present.mostInstructions.PresentInstructionDTO;

import java.util.List;
import java.util.Set;

public class PresentFunctionDTO extends PresentProgramDTO {
    private final String userName;

    public PresentFunctionDTO(SFunction program) {
        super(program);
        this.userName = program.getUserName();
    }

    public String getUserName() {
        return userName;
    }
}
