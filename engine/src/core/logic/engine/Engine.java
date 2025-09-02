package core.logic.engine;


import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import exception.NoProgramException;
import exception.XMLUnmarshalException;
import statistic.SingleRunStatistic;

import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("unused")
public interface Engine {

    // Load program from XML path (may throw checked XMLUnmarshalException coming from JAXB layer)
    void loadProgram(Path xmlPath) throws XMLUnmarshalException;

    // Backwards-compatible method (string path) still present for internal use
    void loadProgram(String fullPath);

    PresentProgramDTO presentProgram() throws NoProgramException;

    // Expand program (by degree)
    void expandProgram(int degree) throws NoProgramException;

    // Backwards-compatible simple expand
    void expandProgram();

    // Create a run DTO to configure/run (factory) and also convenience runProgram
    RunProgramDTO createRunDTO() throws NoProgramException;

    RunProgramDTO runProgram() throws NoProgramException;

    List<SingleRunStatistic> presentProgramStats() throws NoProgramException;
}
