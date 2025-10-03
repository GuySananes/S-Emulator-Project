
package consoleUI;



import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import execution.ProgramExecutor;
import execution.ResultCycle;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // ========= פונקציה Q =========
        Variable qx1 = new VariableImpl(VariableType.INPUT, 1);
        Variable qx2 = new VariableImpl(VariableType.INPUT, 2);
        Variable qz1 = new VariableImpl(VariableType.WORK, 1);
        Variable qy  = Variable.RESULT;

        Label qL1 = new LabelImpl(1);

        List<SInstruction> qInstructions = new ArrayList<>();
        qInstructions.add(new JumpZero(qx1, qL1, FixedLabel.EXIT)); // IF x1 = 0 GOTO EXIT
        qInstructions.add(new DecreaseInstruction(qx1));            // x1 ← x1 - 1
        qInstructions.add(new DecreaseInstruction(qx2));            // x2 ← x2 - 1
        qInstructions.add(new IncreaseInstruction(qz1));            // z1 ← z1 + 1
        qInstructions.add(new IncreaseInstruction(qy));             // y ← y + 1
        qInstructions.add(new JumpNotZeroInstruction(qx1, qL1));    // IF x1 ≠ 0 GOTO L1

        SProgram Q = new SProgramImpl("Q", null, qInstructions);

        // ========= פונקציה Plus =========
// ========= פונקציה Plus =========
// קלטים: x1, x2
// פלט: y = x1 + x2

        Variable plusX1 = new VariableImpl(VariableType.INPUT, 1);
        Variable plusX2 = new VariableImpl(VariableType.INPUT, 2);
        Variable plusY  = Variable.RESULT;

        Label L1 = new LabelImpl(1);
        Label L2 = new LabelImpl(2);
        Label L3 = new LabelImpl(3);
        Label L4 = new LabelImpl(4);

        List<SInstruction> plusInstr = new ArrayList<>();

// אפס את y
        plusInstr.add(new ZeroVariableInstruction(plusY));

// לולאה על x1
        plusInstr.add(new JumpZero(plusX1, L3, L1));         // אם x1 = 0, קפוץ ל־L1
        plusInstr.add(new DecreaseInstruction(plusX1));  // x1 ← x1 - 1
        plusInstr.add(new IncreaseInstruction(plusY));   // y ← y + 1
        plusInstr.add(new JumpNotZeroInstruction(plusX1, L3)); // חזור אם עדיין לא אפס
        plusInstr.add(new NoOpInstruction(plusY, L1));   // L1: (סימון מיקום)

// לולאה על x2
        plusInstr.add(new JumpZero(plusX2, L4, L2));         // אם x2 = 0, קפוץ ל־L2
        plusInstr.add(new DecreaseInstruction(plusX2));  // x2 ← x2 - 1
        plusInstr.add(new IncreaseInstruction(plusY));   // y ← y + 1
        plusInstr.add(new JumpNotZeroInstruction(plusX2, L4)); // חזור אם עדיין לא אפס
        plusInstr.add(new NoOpInstruction(plusY, L2));   // L2: (סימון מיקום)

        SFunction Plus = new SFunction("Plus", "+", null, plusInstr);


        // ========= תוכנית ראשית P =========
        Variable px1 = new VariableImpl(VariableType.INPUT, 1);
        Variable px2 = new VariableImpl(VariableType.INPUT, 2);
        Variable py  = Variable.RESULT;
        Variable pz1 = new VariableImpl(VariableType.WORK, 1);

        Label pL1 = new LabelImpl(1);

        List<SInstruction> pInstructions = new ArrayList<>();

        // Q מצוטט עם (Plus(x1,y), y)
        FunctionArgument innerPlusArg = new FunctionArgument(
                Plus,
                List.of(px1, py)
        );

        FunctionArgument funcArg = new FunctionArgument(Q, List.of(innerPlusArg, py));
        pInstructions.add(new QuoteProgramInstruction(pz1, pL1, funcArg));

        pInstructions.add(new IncreaseInstruction(pz1));    // z1 ← z1 + 1
        pInstructions.add(new DecreaseInstruction(px2));    // x2 ← x2 - 1
        pInstructions.add(new JumpNotZeroInstruction(px2, pL1)); // IF x2 ≠ 0 GOTO L1

        SProgram P = new SProgramImpl("P", null, pInstructions);


        // ========= תוכנית P מורחבת =========
        SProgram expandedP = Expansion.expand(P, 1);

        // ========= הדפסות =========
        System.out.println("=== Program Plus ===");
        System.out.println(Plus.getRepresentation());

        System.out.println("=== Program Q ===");
        System.out.println(Q.getRepresentation());

        System.out.println("=== Program P ===");
        System.out.println(P.getRepresentation());

        System.out.println("=== Expanded Program P ===");
        System.out.println(expandedP.getRepresentation());

        // ========= הרצות =========
        System.out.println("\n=== Run Plus(5,3) ===");
        ProgramExecutor execPlus = new ProgramExecutor(Plus);
        ResultCycle resPlus = execPlus.run(List.of(5L, 3L));
        System.out.println("Result=" + resPlus.getResult() + ", Cycles=" + resPlus.getCycles());

        System.out.println("\n=== Run Q(2,3) ===");
        ProgramExecutor execQ = new ProgramExecutor(Q);
        ResultCycle resQ = execQ.run(List.of(2L, 3L));
        System.out.println("Result=" + resQ.getResult() + ", Cycles=" + resQ.getCycles());

        System.out.println("\n=== Run P(2,3) ===");
        ProgramExecutor execP = new ProgramExecutor(P);
        ResultCycle resP = execP.run(List.of(2L, 3L));
        System.out.println("Result=" + resP.getResult() + ", Cycles=" + resP.getCycles());
        System.out.println("\n=== Run Expanded P(2,3) ===");
        ProgramExecutor execExpP = new ProgramExecutor(expandedP);
        ResultCycle resExpP = execExpP.run(List.of(2L, 3L));
        System.out.println("Result=" + resExpP.getResult() + ", Cycles=" + resExpP.getCycles());

    }
}















/*
package consoleUI;


import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import execution.ProgramExecutor;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // ========= פונקציה Q =========
        Variable qx1 = new VariableImpl(VariableType.INPUT, 1);
        Variable qx2 = new VariableImpl(VariableType.INPUT, 2);
        Variable qz1 = new VariableImpl(VariableType.WORK, 1);
        Variable qy  = Variable.RESULT; // output

        Label qL1 = new LabelImpl(1);

        List<SInstruction> qInstructions = new ArrayList<>();
        // L1: IF x1 = 0 GOTO EXIT
        qInstructions.add(new JumpZero(qx1, qL1, FixedLabel.EXIT));
        // x1 ← x1 - 1
        qInstructions.add(new DecreaseInstruction(qx1));
        // x2 ← x2 - 1
        qInstructions.add(new DecreaseInstruction(qx2));
        // z1 ← z1 + 1
        qInstructions.add(new IncreaseInstruction(qz1));
        // y ← y + 1
        qInstructions.add(new IncreaseInstruction(qy));
        // IF x1 ≠ 0 GOTO L1
        qInstructions.add(new JumpNotZeroInstruction(qx1, qL1));

        SProgram Q = new SProgramImpl("Q", null, qInstructions);

        // ========= תוכנית ראשית P =========
        Variable px1 = new VariableImpl(VariableType.INPUT, 1);
        Variable px2 = new VariableImpl(VariableType.INPUT, 2);
        Variable py  = Variable.RESULT;
        Variable pz1 = new VariableImpl(VariableType.WORK, 1);

        Label pL1 = new LabelImpl(1);

        List<SInstruction> pInstructions = new ArrayList<>();
        // L1: z1 ← (Q, x1, y)
        FunctionArgument funcArg = new FunctionArgument(Q, List.of(px1, py));
        pInstructions.add(new QuoteProgramInstruction(pz1, pL1, funcArg));
        // z1 ← z1 + 1
        pInstructions.add(new IncreaseInstruction(pz1));
        // x2 ← x2 - 1
        pInstructions.add(new DecreaseInstruction(px2));
        // IF x2 ≠ 0 GOTO L1
        pInstructions.add(new JumpNotZeroInstruction(px2, pL1));

        SProgram P = new SProgramImpl("P", null, pInstructions);

        SProgram expandedP = Expansion.expand(P, 1);

        // ========= הדפסה =========
        System.out.println("=== Program Q ===");
        System.out.println(Q.getRepresentation());

        System.out.println("=== Program P ===");
        System.out.println(P.getRepresentation());

        System.out.println("=== Expanded Program P ===");
        System.out.println(expandedP.getRepresentation());

        System.out.println("=== EXECUTION Q ===");
        ProgramExecutor executorQ = new ProgramExecutor(Q);
        System.out.println(executorQ.run(List.of(5L, 3L)).getResult());

        System.out.println("=== EXECUTION P ===");
        ProgramExecutor executorP = new ProgramExecutor(P);
        System.out.println(executorP.run(List.of(5L, 3L)).getResult());

        System.out.println("=== EXECUTION EXPANDED P ===");
        ProgramExecutor executorExpandedP = new ProgramExecutor(expandedP);
        System.out.println(executorExpandedP.run(List.of(5L, 3L)).getResult());
    }
}*/



















/*
package consoleUI;

import core.logic.instruction.mostInstructions.AssignmentInstruction;
import core.logic.instruction.mostInstructions.ConstantAssignmentInstruction;
import core.logic.instruction.mostInstructions.IncreaseInstruction;
import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.QuoteProgramInstruction;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import execution.ProgramExecutor;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        // --- שלב 1: פונקציה פנימית (F1) ---
        // פונקציה שתקח את x1 ותשמור אותו ב-y (RESULT)
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable z1f = new VariableImpl(VariableType.WORK, 1);
        List<SInstruction> f1Instructions = new ArrayList<>();
        f1Instructions.add(new AssignmentInstruction(z1f, x1));
        f1Instructions.add(new AssignmentInstruction(Variable.RESULT, z1f));
        SProgram f1 = new SFunction("F1", "UserFunc", null,
                f1Instructions);

        // --- שלב 2: תוכנית ראשית ---
        Variable z1p = new VariableImpl(VariableType.WORK, 1);
        Variable z2p = new VariableImpl(VariableType.WORK, 2);
        List<SInstruction> mainInstructions = new ArrayList<>();
        mainInstructions.add(new ConstantAssignmentInstruction(5, z1p));
        FunctionArgument funcArg = new FunctionArgument(f1, List.of(z1p));
        mainInstructions.add(new QuoteProgramInstruction(z2p, funcArg));
        mainInstructions.add(new AssignmentInstruction(Variable.RESULT, z2p));
        mainInstructions.add(new IncreaseInstruction(Variable.RESULT));



        SProgram mainProgram = new SProgramImpl("MainProgram", null, mainInstructions);

        SProgram expandedProgram = Expansion.expand(mainProgram, mainProgram.getDegree());

                // --- שלב 3: הדפסת הייצוג ---
        System.out.println("=== MAIN PROGRAM REPRESENTATION ===");
        System.out.println(mainProgram.getRepresentation());

        System.out.println("=== FUNCTION F1 REPRESENTATION ===");
        System.out.println(f1.getRepresentation());

        *//*

*/
/*System.out.println("=== EXPANDED PROGRAM REPRESENTATION ===");
        System.out.println(expandedProgram.getRepresentation());*//*
*/
/*


        System.out.println("=== EXECUTION PROGRAM ===");
        ProgramExecutor executor = new ProgramExecutor(mainProgram);
        System.out.println(executor.run(List.of()).getResult());

        System.out.println("=== EXECUTION FUNCTION ===");
        ProgramExecutor executorF1 = new ProgramExecutor(f1);
        System.out.println(executorF1.run(List.of(10L)).getResult());


        System.out.println("=== EXECUTION EXPANDED-PROGRAM ===");
        ProgramExecutor executor2 = new ProgramExecutor(expandedProgram);
        System.out.println(executor2.run(List.of()).getResult());
    }
}


















*//*

*/
/*package consoleUI;

public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}*/

