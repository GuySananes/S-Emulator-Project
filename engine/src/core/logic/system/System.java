package core.logic.system;


import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import exception.NoProgramException;

public interface System {

    void laodProgram(String fullPath);
    PresentProgramDTO presentProgram() throws NoProgramException;
    void expandProgram();
    RunProgramDTO runProgram() throws NoProgramException;
    void presentProgramStats();
}
