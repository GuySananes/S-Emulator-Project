package consoleUI;

import core.logic.instruction.mostInstructions.AssignmentInstruction;
import core.logic.instruction.mostInstructions.ConstantAssignmentInstruction;
import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.QuoteProgramInstruction;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // --- שלב 1: פונקציה פנימית (F1) ---
        // פונקציה שתקח את x1 ותשמור אותו ב-y (RESULT)
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);

        List<SInstruction> f1Instructions = new ArrayList<>();
        f1Instructions.add(new AssignmentInstruction(Variable.RESULT, x1));

        SProgram f1 = new SFunction("F1", "UserFunc", null,
                f1Instructions);

        // --- שלב 2: תוכנית ראשית ---
        Variable z1 = new VariableImpl(VariableType.WORK, 1);
        Variable z2 = new VariableImpl(VariableType.WORK, 2);

        List<SInstruction> mainInstructions = new ArrayList<>();

        // הוראה 1: z1 <- 5
        mainInstructions.add(new ConstantAssignmentInstruction(5, z1));

        // הוראה 2: z2 <- (F1, z1)
        FunctionArgument funcArg = new FunctionArgument(f1, List.of(z1));
        mainInstructions.add(new QuoteProgramInstruction(z2, funcArg));



        SProgram mainProgram = new SProgramImpl("MainProgram", null, mainInstructions);

        System.out.println((mainProgram.getDegree()));

        SProgram expandedProgram = Expansion.expand(mainProgram, 2);
        System.out.println((expandedProgram.getRepresentation()));

                // --- שלב 3: הדפסת הייצוג ---
        System.out.println("=== MAIN PROGRAM REPRESENTATION ===");
        System.out.println(mainProgram.getRepresentation());

        System.out.println("=== FUNCTION F1 REPRESENTATION ===");
        System.out.println(f1.getRepresentation());
    }
}

/*package consoleUI;

public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}*/