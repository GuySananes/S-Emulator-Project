package core.logic.engine;


import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import exception.NoProgramException;

public interface Engine {

    void laodProgram(String fullPath);
    PresentProgramDTO presentProgram() throws NoProgramException;
    void expandProgram();
    RunProgramDTO runProgram() throws NoProgramException;
    void presentProgramStats();
}
