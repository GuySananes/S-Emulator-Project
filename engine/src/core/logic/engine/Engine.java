package core.logic.engine;


import exception.*;
import expand.ExpandDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;
import core.logic.program.SProgram; // added import

import java.util.Set;

public interface Engine {

    Set<String> loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException;
    void chooseContextProgram(String programName) throws NoProgramException, NoSuchProgramInContextException;
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException, ProgramHasNoStatisticException;

    // New accessor to retrieve the currently loaded core program
    SProgram getLoadedProgram() throws NoProgramException; // added method
}
