package expand;

import core.logic.program.SProgram;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import expansion.Expansion;
import present.create.PresentDTOCreator;
import present.program.PresentProgramDTO;

public class ExpandDTO {
    private final SProgram program;

    public ExpandDTO(SProgram program) {
        this.program = program;
    }

    public int getMinDegree() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return program.getMinDegree();
    }

    public int getMaxDegree() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return program.getDegree();
    }

    public PresentProgramDTO expand(int degree) throws NoProgramException, DegreeOutOfRangeException {
        if (program == null) {
            throw new NoProgramException();
        }

        if (degree < 0 || degree > getMaxDegree()) {
            throw new DegreeOutOfRangeException(getMinDegree(), getMaxDegree());
        }

        return PresentDTOCreator.createPresentProgramDTO(Expansion.expand(program, degree));
    }
}
