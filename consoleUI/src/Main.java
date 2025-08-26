import DTO.RunProgramDTO;
import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;

import java.util.Scanner;

import static java.sql.DriverManager.println;

public class Main {

        Engine engine = new EngineImpl();
        Scanner scanner = new Scanner(System.in);

        public void runProgram() {
            RunProgramDTO runProgramDTO = null;
            try {
                runProgramDTO = engine.runProgram();
            } catch (Exception e) {
                println(e.getMessage());
                //UI presents menu again
            }

            println("max program degree: " + runProgramDTO.getMaxDegree());
            //get input from user
            int maxDegree = scanner.nextInt();

            println("these are the inputs: " + runProgramDTO.getInputs());
            //get inputs from user
            //should now create a Long list with unknown size and fill it with user inputs

            runProgramDTO.execute(Long inputs);



        }


}