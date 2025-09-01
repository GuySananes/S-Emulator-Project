import DTO.RunProgramDTO;
import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;

import java.util.Scanner;

import static java.sql.DriverManager.println;

public class Main {

        Engine engine = EngineImpl.getInstance();
        Scanner scanner = new Scanner(System.in);

        public void runProgram() {
            RunProgramDTO runProgramDTO = null;
            try {
                runProgramDTO = engine.runProgram();
            } catch (Exception e) {
                println(e.getMessage());
                //UI presents menu again
            }
        }


}