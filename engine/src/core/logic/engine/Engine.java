package core.logic.engine;


import exception.*;
import expand.ExpandDTO;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticsDTO;

import java.util.Set;

public interface Engine {

    Set<String> loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException;
    void chooseContextProgram(String programName) throws NoProgramException, NoSuchProgramInContextException;
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    RunProgramDTO reRunProgram(int runNumber) throws NoProgramException, ProgramNotExecutedYetException, NoSuchRunException;
    ProgramStatisticsDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException;
}
