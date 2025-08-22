package core.logic.system;

import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import DTOcreate.PresentProgramDTOCreator;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import exception.NoProgramException;

public class SystemImpl implements System {

    SProgram program;

    @Override
    //implement a method that loads the program
    public void laodProgram(String fullPath) {

    }


    @Override
    public PresentProgramDTO presentProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return PresentProgramDTOCreator.create(program);





    }

    @Override
    public void expandProgram() {

    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        ProgramExecutor programExecutor = new ProgramExecutorImpl(program);






    }

    @Override
    public void presentProgramStats() {

    }
}
