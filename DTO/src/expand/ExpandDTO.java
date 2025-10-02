package expand;

import core.logic.program.SFunction;
import core.logic.program.SProgram;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import expansion.Expansion;
import present.create.PresentDTOCreator;
import present.program.PresentFunctionDTO;
import present.program.PresentProgramDTO;

public class ExpandDTO {
    private final SProgram program;

    public ExpandDTO(SProgram program) {
        this.program = program;
    }

    public int getMinDegree() { return program.getMinDegree() + 1;}

    public int getMaxDegree() {return program.getDegree();}

    public PresentProgramDTO expand(int degree) throws DegreeOutOfRangeException {

        if (degree < getMinDegree() || degree > getMaxDegree()) {
            throw new DegreeOutOfRangeException(getMinDegree(), getMaxDegree());
        }

        SProgram expandedProgram = Expansion.expand(program, degree);

        if(expandedProgram instanceof SFunction sf) {
            return new PresentFunctionDTO(sf);
        }

        return new PresentProgramDTO(expandedProgram);
    }
}
