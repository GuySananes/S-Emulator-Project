package present.program;

import core.logic.program.SFunction;

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
