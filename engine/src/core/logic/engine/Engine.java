package core.logic.engine;


import exception.*;
import expand.ExpandDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;
import core.logic.program.SProgram; // added import

public interface Engine {

    void loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException;
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException, ProgramHasNoStatisticException;

    // New accessor to retrieve the currently loaded core program
    SProgram getLoadedProgram() throws NoProgramException; // added method
}
