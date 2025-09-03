package consoleUI;
import java.util.*;

import core.logic.variable.Variable;
import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import expand.ExpandDTO;
import present.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;

public class ConsoleUI {
    private final Engine engine;
    private final Scanner scanner;

    public ConsoleUI() {
        this.engine = EngineImpl.getInstance();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Program Engine Console UI!");
        while (true) {
            System.out.println("Please choose an option:");
            System.out.println("1. Load Program");
            System.out.println("2. Present Program");
            System.out.println("3. Expand Program");
            System.out.println("4. Run Program");
            System.out.println("5. Show Statistics");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("Enter the full path of the program to load: ");
                    String path = scanner.nextLine();
                    engine.loadProgram(path);
                    System.out.println("Program loaded successfully.");
                    break;
                case "2":
                    try {
                        PresentProgramDTO programPresent = engine.presentProgram();
                        System.out.println("Program Name: " + programPresent.getProgramName());
                        System.out.println("Variables: " + programPresent.getXs());
                        System.out.println("Labels: " + programPresent.getLabels());
                        System.out.println("Representation: \n" + programPresent.getRepresentation());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "3":
                    try {
                        ExpandDTO expandDTO = engine.expandProgram();
                        System.out.print("Enter the degree of expansion ("
                                + expandDTO.getMinDegree() + " - " + expandDTO.getMaxDegree() + "): ");
                        int degree = Integer.parseInt(scanner.nextLine());
                        PresentProgramDTO expandedProgram = expandDTO.expand(degree);
                        System.out.println("Expanded Program Representation: \n" +
                                expandedProgram.getRepresentation());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "4":
                    try {
                        RunProgramDTO runDTO = engine.runProgram();
                        System.out.print("Enter the degree of program to run ("
                                + runDTO.getMinDegree() + " - " + runDTO.getMaxDegree() + "): ");
                        int degree = Integer.parseInt(scanner.nextLine());
                        runDTO.setDegree(degree);
                        System.out.println("Input Variables: " + runDTO.getInputs());
                        System.out.print("Enter input values (comma or space separated): ");
                        String inputLine = scanner.nextLine();
                        List<Long> inputValues = Arrays.stream(inputLine.split("[,\\s]+"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Long::parseLong)
                                .toList();
                        runDTO.setInputs(inputValues);
                        long result = runDTO.runProgram();
                        PresentProgramDTO programPresent = runDTO.getPresentProgramDTO();
                        System.out.println("Program Representation: \n" + programPresent.getRepresentation());
                        System.out.println("Result: " + result);
                        Set<Variable> programVariables = runDTO.getOrderedVariablesCopy();
                        List<Long> variableValues = runDTO.getOrderedValuesCopy();
                        Iterator<Variable> varIterator = programVariables.iterator();
                        for (int i = 0; i < variableValues.size() && varIterator.hasNext(); i++) {
                            Variable var = varIterator.next();
                            Long value = variableValues.get(i);
                            System.out.println(var + " = " + value);
                        }
                        System.out.println("Execution Cycles: " + runDTO.getCycles());
                    } catch (Exception e) {
                        System.out.println("Error in UI in Run Program: " + e.getMessage());
                    }
                    break;
                case "5":
                    try {
                        ProgramStatisticDTO statsDTO = engine.presentProgramStats();
                        System.out.println(statsDTO.getRepresentation());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "6":
                    System.out.println("Exiting the Program Engine Console UI. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
