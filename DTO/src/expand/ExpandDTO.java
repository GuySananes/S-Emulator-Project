package expand;

import core.logic.program.SProgram;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import expansion.Expansion;
import present.PresentProgramDTO;
import present.PresentProgramDTOCreator;

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

        return program.calculateMaxDegree();
    }

    public SProgram expand(int degree) throws NoProgramException, DegreeOutOfRangeException {
        if (program == null) {
            throw new NoProgramException();
        }

        if (degree < 0 || degree > getMaxDegree()) {
            throw new DegreeOutOfRangeException(getMinDegree(), getMaxDegree());
        }

        return Expansion.expand(program, degree);
    }
}
