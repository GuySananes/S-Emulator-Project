package core.logic.engine;


import exception.ProgramNotExecutedYetException;
import expand.ExpandDTO;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import exception.NoProgramException;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import statistic.ProgramStatisticDTO;

public interface Engine {

    void loadProgram(String fullPath) throws XMLUnmarshalException, ProgramValidationException;
    PresentProgramDTO presentProgram() throws NoProgramException;
    ExpandDTO expandProgram() throws NoProgramException;
    RunProgramDTO runProgram() throws NoProgramException;
    ProgramStatisticDTO presentProgramStats() throws NoProgramException, ProgramNotExecutedYetException;
}
