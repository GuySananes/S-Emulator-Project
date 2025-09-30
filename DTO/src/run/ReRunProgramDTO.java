package run;

import present.program.PresentProgramDTO;

public class ReRunProgramDTO {
    private final PresentProgramDTO presentProgramDTO;
    private final RunProgramDTO runProgramDTO;

    public ReRunProgramDTO(PresentProgramDTO presentProgramDTO, RunProgramDTO runProgramDTO) {
        this.presentProgramDTO = presentProgramDTO;
        this.runProgramDTO = runProgramDTO;
    }

    public PresentProgramDTO getPresentProgramDTO() {
        return presentProgramDTO;
    }

    public RunProgramDTO getRunProgramDTO() {
        return runProgramDTO;
    }
}
