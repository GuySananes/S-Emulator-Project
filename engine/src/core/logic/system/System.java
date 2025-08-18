package core.logic.system;


import DTO.PresentProgramDTO;

public interface System {

    void laodProg(String fullPath);
    PresentProgramDTO presentProg();
    void expandProg();
    void runProg();
    void presentProgStats();
}
