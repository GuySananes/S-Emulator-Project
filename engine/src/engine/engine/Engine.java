package engine.engine;


import DTO.PresentProgramDTO;
import runProgram.RunProgramDTO;
import exception.NoProgramException;
import statistic.SingleRunStatistic;

import java.util.List;

public interface Engine {

    void loadProgram(String fullPath);
    PresentProgramDTO presentProgram() throws NoProgramException;
    void expandProgram();
    RunProgramDTO runProgram() throws NoProgramException;
    List<SingleRunStatistic> presentProgramStats() throws NoProgramException;
}
