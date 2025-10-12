package run;

import core.logic.program.SProgram;

public class ExecuteProgramDTO {
    private final RunProgramDTO runProgramDTO;
    private final DebugProgramDTO debugProgramDTO;

    public ExecuteProgramDTO(SProgram program) {
        this.runProgramDTO = new RunProgramDTO(program);
        this.debugProgramDTO = new DebugProgramDTO(program);
    }

    public RunProgramDTO getRunProgramDTO() {
        return runProgramDTO;
    }

    public DebugProgramDTO getDebugProgramDTO() {
        return debugProgramDTO;
    }
}
