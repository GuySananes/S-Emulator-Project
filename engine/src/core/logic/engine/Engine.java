package core.logic.engine;


import exception.*;
import expand.ExpandDTO;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;

public interface Engine {

    void loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException;
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException, ProgramHasNoStatisticException;
}
