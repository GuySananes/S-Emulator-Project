package run;

import present.program.PresentProgramDTO;

public class ReExecuteProgramDTO {
    private final PresentProgramDTO presentProgramDTO;
    private final ExecuteProgramDTO executeProgramDTO;

    public ReExecuteProgramDTO(PresentProgramDTO presentProgramDTO, ExecuteProgramDTO executeProgramDTO) {
        this.presentProgramDTO = presentProgramDTO;
        this.executeProgramDTO = executeProgramDTO;
    }

    public PresentProgramDTO getPresentProgramDTO() {
        return presentProgramDTO;
    }

    public ExecuteProgramDTO getExecuteProgramDTO() {
        return executeProgramDTO;
    }
}
