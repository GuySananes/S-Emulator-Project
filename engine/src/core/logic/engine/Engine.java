package core.logic.engine;


import expand.ExpandDTO;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import exception.NoProgramException;
import statistic.ProgramStatisticDTO;

public interface Engine {

    void loadProgram(String fullPath);
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    ProgramStatisticDTO presentProgramStats() throws NoProgramException;
}
