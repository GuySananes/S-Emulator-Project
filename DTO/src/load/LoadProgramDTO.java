package load;

import present.program.PresentProgramDTO;
import run.RunProgramDTO;

import java.util.Set;

public class LoadProgramDTO {
    private final PresentProgramDTO presentProgramDTO;
    private final Set<String> contextProgramsNames;

    public LoadProgramDTO(PresentProgramDTO presentProgramDTO, Set<String> contextProgramsNames) {
        this.presentProgramDTO = presentProgramDTO;
        this.contextProgramsNames = contextProgramsNames;
    }

    public PresentProgramDTO getPresentProgramDTO() {
        return presentProgramDTO;
    }

    public Set<String> getContextProgramsNames() {
        return contextProgramsNames;
    }
}
