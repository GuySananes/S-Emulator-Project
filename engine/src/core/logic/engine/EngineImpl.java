package core.logic.engine;

import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import DTOcreate.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import exception.NoProgramException;

public class EngineImpl implements Engine {

    SProgram program;

    @Override
    //implement a method that loads the program
    public void loadProgram(String fullPath) {

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

        return new RunProgramDTO();

    }

    @Override
    public void presentProgramStats() {

    }
}
