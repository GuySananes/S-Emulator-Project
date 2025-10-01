package run;

import present.program.PresentProgramDTO;

public class ReRunProgramDTO {
    private final PresentProgramDTO presentProgramDTO;
    private final RunProgramDTO runProgramDTO;
    private final DebugProgramDTO debugProgramDTO;

    public ReRunProgramDTO(PresentProgramDTO presentProgramDTO, RunProgramDTO runProgramDTO, DebugProgramDTO debugProgramDTO) {
        this.presentProgramDTO = presentProgramDTO;
        this.runProgramDTO = runProgramDTO;
        this.debugProgramDTO = debugProgramDTO;
    }

    public PresentProgramDTO getPresentProgramDTO() {
        return presentProgramDTO;
    }

    public RunProgramDTO getRunProgramDTO() {
        return runProgramDTO;
    }

    public DebugProgramDTO getDebugProgramDTO() {
        return debugProgramDTO;
    }
}
