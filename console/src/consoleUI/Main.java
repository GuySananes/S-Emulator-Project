


//מיין בדיקה של התוכנית minus בהרצה בדיבאג
// לראות אם הדיבאג עובד גם בהגעה לגוטו אקזיט. עובד
/*package consoleUI;

import core.logic.execution.*;
import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*; // אם אין לך GotoInstruction כאן, הסר
import expansion.Expansion;

import java.util.*;

public class Main {

    // בונה את התוכנית מהתמונה
    private static SProgram buildProgram() {
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable x2 = new VariableImpl(VariableType.INPUT, 2);
        Variable y  = Variable.RESULT;
        Variable z1 = new VariableImpl(VariableType.WORK, 1);

        Label L1 = new LabelImpl(1);
        Label L2 = new LabelImpl(2);
        Label L3 = new LabelImpl(3);

        List<SInstruction> ins = new ArrayList<>();

        // [ ] y <- x1
        ins.add(new AssignmentInstruction(y, x1));
        // [ ] z1 <- x2
        ins.add(new AssignmentInstruction(z1, x2));

        // [L3] IF z1 != 0 GOTO L1
        ins.add(new JumpNotZeroInstruction(z1, L3, L1));

        // [ ] GOTO EXIT
        ins.add(new GotoLabel(FixedLabel.EXIT));

        // [L1] IF y != 0 GOTO L2
        ins.add(new JumpNotZeroInstruction(y, L1, L2));

        // [ ] GOTO EXIT
        ins.add(new GotoLabel(FixedLabel.EXIT));

        // [L2] y <- y - 1
        ins.add(new DecreaseInstruction(y, L2));
        // [ ] z1 <- z1 - 1
        ins.add(new DecreaseInstruction(z1));
        // [ ] GOTO L3
        ins.add(new GotoLabel(L3));

        return new SProgramImpl("GotoExitTest", null, ins);
    }

    private static boolean runDebugAndAssert(SProgram prog, List<Long> input, long expectedY) {
        Debug dbg = new Debug(prog);
        dbg.setInput(input);

        int steps = 0;
        int lastNextIndex = Integer.MIN_VALUE;
        int lastCycles = -1;

        while (true) {
            DebugResult r = dbg.nextStep();
            steps++;

            // ודא שמספר ה-cycles לא יורד (מונוטוני לא-יורד)
            if (r.getCycles() < lastCycles) {
                System.out.println("FAILED: cycles decreased (" + lastCycles + " -> " + r.getCycles() + ")");
                return false;
            }
            lastCycles = r.getCycles();

            // ודא שאין "סיום כפול": אם זה צעד סופי, אין עוד צעד אחרי זה
            if (r instanceof DebugFinalResult fin) {
                long got = fin.getResult();
                boolean ok = (got == expectedY);
                System.out.println(
                        "Input=" + input + "  RESULT=" + got + "  cycles=" + fin.getCycles()
                                + "  steps=" + steps + "  => " + (ok ? "PASSED" : "FAILED (expected " + expectedY + ")")
                );
                return ok;
            }

            // לא אמור להיות פעמיים nextIndex=-1 לפני סיום
            if (r.getNextIndex() == -1 && lastNextIndex == -1) {
                System.out.println("FAILED: saw nextIndex=-1 twice before finish");
                return false;
            }
            lastNextIndex = r.getNextIndex();
        }
    }

    public static void main(String[] args) {
        SProgram P = buildProgram();

        System.out.println("=== Program Representation ===");
        System.out.println(P.getRepresentation());

       // מקרי בדיקה:
        // 1) x1=5, x2=3  => מצופה y=2 (לולאה תרד 3 פעמים)
        boolean t1 = runDebugAndAssert(P, List.of(5L, 3L), 2L);

        // 2) x1=0, x2=4  => ב-L1 התנאי נכשל ויבוצע GOTO EXIT => מצופה y=0
        boolean t2 = runDebugAndAssert(P, List.of(0L, 4L), 0L);

        // 3) x1=7, x2=0  => ב-L3 התנאי נכשל, יתבצע GOTO EXIT => מצופה y=7
        boolean t3 = runDebugAndAssert(P, List.of(7L, 0L), 7L);

        // סיכום
        if (t1 && t2 && t3) {
            System.out.println("\nALL TESTS PASSED");
        } else {
            System.out.println("\nSOME TESTS DIDN'T PASS");
        }
    }
}*/

























//מיין בדיקה של quotation אם עובדת או לא מבחינת דיבאג כשמגיעה
// לגוטו אקזיט. אצל גיא לא עבד אבל בקונסול עובד
/*package consoleUI;

import core.logic.execution.*;
import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import expansion.Expansion;

import java.util.*;

public class Main {

    // === Q: CONST-3 – מחזירה תמיד 3 ===
    private static SProgram buildCONST3() {
        Variable y = Variable.RESULT;
        List<SInstruction> ins = new ArrayList<>();
        ins.add(new ZeroVariableInstruction(y));     // y <- 0
        ins.add(new IncreaseInstruction(y));
        ins.add(new IncreaseInstruction(y));
        ins.add(new IncreaseInstruction(y));          // y == 3
        return new SProgramImpl("CONST-3", null, ins);
    }

    // === P: התוכנית שבתמונה ===
    // 1) IF x1 = (CONST-3) GOTO EXIT
    // 2) z1 <- (CONST-3)
    // 3) y  <- x1
    // 4) [L1] y <- y + 1
    // 5) z1 <- z1 - 1
    // 6) IF z1 != 0 GOTO L1
    private static SProgram buildProgram(SProgram CONST3) {
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable y  = Variable.RESULT;
        Variable z1 = new VariableImpl(VariableType.WORK, 1);
        Label L1 = new LabelImpl(1);

        // (CONST-3)
        FunctionArgument const3 = new FunctionArgument(CONST3, List.of());

        List<SInstruction> ins = new ArrayList<>();

        // 1) IF x1 = (CONST-3) GOTO EXIT
        ins.add(new JumpEqualFunction(x1, FixedLabel.EXIT, const3));

        // 2) z1 <- (CONST-3)
        ins.add(new QuoteProgramInstruction(z1, const3));

        // 3) y <- x1
        ins.add(new AssignmentInstruction(y, x1));

        // 4) [L1] y <- y + 1
        ins.add(new IncreaseInstruction(y, L1));

        // 5) z1 <- z1 - 1
        ins.add(new DecreaseInstruction(z1));

        // 6) IF z1 != 0 GOTO L1
        ins.add(new JumpNotZeroInstruction(z1, L1));

        return new SProgramImpl("ImgTest", null, ins);
    }

    private static void runDebugOnce(SProgram prog, List<Long> input) {
        System.out.println("=== Program ===");
        System.out.println(prog.getRepresentation());

        Debug dbg = new Debug(prog);
        dbg.setInput(input);

        while (true) {
            DebugResult r = dbg.nextStep();

            if (r instanceof DebugFinalResult fin) {
                System.out.println("FINISHED. result=" + fin.getResult()
                        + ", cycles=" + fin.getCycles());
                break;
            }

            ChangedVariable cv = r.getChangedVariable();
            if (cv != null) {
                System.out.println("Step: " + cv.getVariable().getRepresentation()
                        + " : " + cv.getOldValue() + " -> " + cv.getNewValue());
            }
            System.out.println("cycles=" + r.getCycles()
                    + ", nextIndex=" + r.getNextIndex());
            System.out.println("---");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        SProgram CONST3 = buildCONST3();
        SProgram P = buildProgram(CONST3);

        // בדיקה 1: x1=5  → מצופה y=8
*//*
        runDebugOnce(P, List.of(5L));
*//*

        // בדיקה 2 (edge): x1=3 → יציאה מיידית, מצופה y=0
        runDebugOnce(P, List.of(3L));

        // אופציונלי: בדיקת עקביות מול הרחבה
        SProgram P1 = Expansion.expand(P, 1);
        SProgram P2 = Expansion.expand(P, 2);
        System.out.println("RUN expanded deg1 (x1=5): " +
                new ProgramExecutor(P1).run(List.of(5L)).getResult());
        System.out.println("RUN expanded deg2 (x1=5): " +
                new ProgramExecutor(P2).run(List.of(5L)).getResult());
    }
}*/














//טסט לתוכנית קומפוזישן. כשהיא מורחבת היא עובדת
// ברמת הקונסול. אך לא אצל גיא ביו איי של ג'אווה אף איקס
/*package consoleUI;

import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import core.logic.execution.ProgramExecutor;
import expansion.Expansion;

import java.util.*;

public class Main {

    // ===== Program: CONST7 =====
    // קלט: ללא. תוצאה: תמיד 7
    private static SProgram buildCONST7() {
        Variable y = Variable.RESULT;
        List<SInstruction> ins = new ArrayList<>();
        ins.add(new ZeroVariableInstruction(y));     // y <- 0
        for (int i = 0; i < 7; i++) ins.add(new IncreaseInstruction(y)); // y++ ×7
        return new SProgramImpl("CONST-7", null, ins);
    }

    // ===== Program: S (successor) =====
    // קלט: x1. תוצאה: x1 + 1
    private static SProgram buildS() {
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable y  = Variable.RESULT;
        List<SInstruction> ins = new ArrayList<>();
        ins.add(new AssignmentInstruction(y, x1)); // y <- x1
        ins.add(new IncreaseInstruction(y));       // y++
        return new SProgramImpl("S", null, ins);
    }

    // ===== Program: MINUS =====
    // קלט: a=x1, b=x2. תוצאה: a - b (מניחים a ≥ b)
    private static SProgram buildMINUS() {
        Variable a  = new VariableImpl(VariableType.INPUT, 1); // x1
        Variable b  = new VariableImpl(VariableType.INPUT, 2); // x2
        Variable y  = Variable.RESULT;
        Variable z1 = new VariableImpl(VariableType.WORK, 1);  // עותק של a
        Variable z2 = new VariableImpl(VariableType.WORK, 2);  // מונה b

        Label L = new LabelImpl(1);

        List<SInstruction> ins = new ArrayList<>();
        ins.add(new AssignmentInstruction(z1, a));         // z1 <- a
        ins.add(new AssignmentInstruction(z2, b));         // z2 <- b
        ins.add(new ZeroVariableInstruction(y));          // y <- 0 (לא חובה, רק לניקיון)
        ins.add(new JumpZero(z2, L, FixedLabel.EXIT));     // if z2==0 goto EXIT
        ins.add(new DecreaseInstruction(z1));              // z1--
        ins.add(new DecreaseInstruction(z2));              // z2--
        ins.add(new JumpNotZeroInstruction(z2, L));        // if z2!=0 goto L
        ins.add(new AssignmentInstruction(y, z1));         // y <- z1
        return new SProgramImpl("-", null, ins);
    }

    // ===== Program: P =====
    // הוראה יחידה: y <- (-,(CONST-7),(S,x1))
    private static SProgram buildP(SProgram CONST7, SProgram S, SProgram MINUS) {
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable y  = Variable.RESULT;

        // ארגומנט ראשון: (CONST-7)
        FunctionArgument argConst7 = new FunctionArgument(CONST7, List.of());

        // ארגומנט שני: (S, x1)
        FunctionArgument argSx1 = new FunctionArgument(S, List.of(x1));

        // פונקציה חיצונית: (-, <const7>, <S(x1)>)
        FunctionArgument minusCall = new FunctionArgument(MINUS, List.of(argConst7, argSx1));

        List<SInstruction> pIns = new ArrayList<>();
        pIns.add(new QuoteProgramInstruction(y, minusCall));

        return new SProgramImpl("P_minus_const7_minus_Sx1", null, pIns);
    }

    public static void main(String[] args) {
        SProgram CONST7 = buildCONST7();
        SProgram S      = buildS();
        SProgram MINUS  = buildMINUS();
        SProgram P      = buildP(CONST7, S, MINUS);

        // נריץ עם x1=3 → S(x1)=4 → 7-4=3
        List<Long> input = List.of(3L);

        System.out.println("=== Program P (original) ===");
        System.out.println(P.getRepresentation());
        ProgramExecutor execP = new ProgramExecutor(P);
        System.out.println("RUN P  (x1=3) => " + execP.run(input).getResult());

        SProgram P1 = Expansion.expand(P, 4);
        System.out.println("=== Program P expanded to degree 4 ===");
        System.out.println(P1.getRepresentation());
        ProgramExecutor execP1 = new ProgramExecutor(P1);
        System.out.println("RUN P4 (x1=3) => " + execP1.run(input).getResult());

        SProgram P2 = Expansion.expand(P, 2);
        System.out.println("=== Program P expanded to degree 2 ===");
        System.out.println(P2.getRepresentation());
        ProgramExecutor execP2 = new ProgramExecutor(P2);
        System.out.println("RUN P2 (x1=3) => " + execP2.run(input).getResult());

        // בדיקת קצה: x1=6 → S(x1)=7 → 7-7=0
        List<Long> edge = List.of(6L);
        System.out.println("\n=== Edge case x1=6 (expect 0) ===");
        System.out.println("RUN P  => "  + new ProgramExecutor(P).run(edge).getResult());
        System.out.println("RUN P1 => "  + new ProgramExecutor(P1).run(edge).getResult());
        System.out.println("RUN P2 => "  + new ProgramExecutor(P2).run(edge).getResult());
    }
}

 */

















































//כאן למטה טסטים פחות חשובים, שאין צורך לבחון//////




//בדיקת דיבאג- את המתודה נקסט סטפ ורזיום. עובד
/*package consoleUI;

import core.logic.execution.*;
import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // ====== בניית תוכנית בסיסית ======
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable y  = Variable.RESULT;

        Label L1 = new LabelImpl(1);

        List<SInstruction> instructions = List.of(
                new DecreaseInstruction(x1, L1),    // x1--
                new IncreaseInstruction(y),         // y++
                new NoOpInstruction(y),
                new JumpNotZeroInstruction(x1, L1)  // אם x1 != 0 -> קפוץ ל-L1, אחרת סיים (EXIT)
        );

        SProgram program = new SProgramImpl("DebugTest", null, instructions);

        // ====== טסט 0: ריצה צעד-אחר-צעד (כבר היה אצלך) ======
        System.out.println("=== Test 0: step-by-step ===");
        Debug debug0 = new Debug(program);
        debug0.setInput(List.of(2L)); // x1 = 2
        while (true) {
            DebugResult result = debug0.nextStep();

            if (result.getChangedVariable() != null) {
                ChangedVariable cv = result.getChangedVariable();
                System.out.println("Step: Variable " + cv.getVariable().getRepresentation()
                        + " changed from " + cv.getOldValue() + " to " + cv.getNewValue());
            }

            System.out.println("Total Cycles so far: " + result.getCycles());
            System.out.println("Next instruction index: " + result.getNextIndex());
            System.out.println("---");

            if (result instanceof DebugFinalResult finalResult) {
                System.out.println("Program finished. Final result: " + finalResult.getResult());
                System.out.println("Total Cycles: " + finalResult.getCycles());
                break;
            }
        }

        // ====== טסט A: resume() מהריצה ההתחלתית ======
        System.out.println("\n=== Test A: resume() from start ===");
        Debug debugA = new Debug(program);
        debugA.setInput(List.of(2L)); // x1 = 2
        DebugFinalResult finalA = debugA.resume();
        long expectedA = 2L;
        System.out.println("resume() -> result = " + finalA.getResult() + ", cycles = " + finalA.getCycles());
        System.out.println("Test A " + (finalA.getResult() == expectedA ? "PASSED" : "FAILED (expected " + expectedA + ")"));

        // ====== טסט B: כמה צעדים ידניים ואז resume() ======
        System.out.println("\n=== Test B: few nextStep() then resume() ===");
        Debug debugB = new Debug(program);
        debugB.setInput(List.of(3L)); // x1 = 3

        // שני צעדים ידניים (לא משנה איזו הוראה – אנחנו רק רוצים להתקדם קצת)
        for (int i = 0; i < 2; i++) {
            DebugResult r = debugB.nextStep();
            if (r instanceof DebugFinalResult finEarly) {
                System.out.println("Finished earlier than expected. result=" + finEarly.getResult());
                break;
            }
        }

        // המשך עד הסוף עם resume()
        DebugFinalResult finalB = debugB.resume();
        long expectedB = 3L; // בסוף y אמור להיות שווה לערך ההתחלתי של x1
        System.out.println("after partial steps, resume() -> result = " + finalB.getResult() + ", cycles = " + finalB.getCycles());
        System.out.println("Test B " + (finalB.getResult() == expectedB ? "PASSED" : "FAILED (expected " + expectedB + ")"));
    }
}*/














//גם ב-Q יש ציטוט תוכנית... עובד
/*package consoleUI;

import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // ========= פונקציה Plus =========
        Variable plusX1 = new VariableImpl(VariableType.INPUT, 1);
        Variable plusX2 = new VariableImpl(VariableType.INPUT, 2);
        Variable plusY  = Variable.RESULT;

        Label L1 = new LabelImpl(1);
        Label L2 = new LabelImpl(2);
        Label L3 = new LabelImpl(3);
        Label L4 = new LabelImpl(4);

        List<SInstruction> plusInstr = new ArrayList<>();
        plusInstr.add(new ZeroVariableInstruction(plusY));
        plusInstr.add(new JumpZero(plusX1, L3, L1));
        plusInstr.add(new DecreaseInstruction(plusX1));
        plusInstr.add(new IncreaseInstruction(plusY));
        plusInstr.add(new JumpNotZeroInstruction(plusX1, L3));
        plusInstr.add(new NoOpInstruction(plusY, L1));
        plusInstr.add(new JumpZero(plusX2, L4, L2));
        plusInstr.add(new DecreaseInstruction(plusX2));
        plusInstr.add(new IncreaseInstruction(plusY));
        plusInstr.add(new JumpNotZeroInstruction(plusX2, L4));
        plusInstr.add(new NoOpInstruction(plusY, L2));

        SFunction Plus = new SFunction("Plus", "+", null, plusInstr);

        // ========= פונקציה Q =========
        Variable qx1 = new VariableImpl(VariableType.INPUT, 1);
        Variable qx2 = new VariableImpl(VariableType.INPUT, 2);
        Variable qz1 = new VariableImpl(VariableType.WORK, 1);
        Variable qy  = Variable.RESULT;
        Variable qZero = new VariableImpl(VariableType.WORK, 2); // משתנה חדש שמכיל 0

        Label qL1 = new LabelImpl(1);

        List<SInstruction> qInstructions = new ArrayList<>();
        qInstructions.add(new ZeroVariableInstruction(qZero, qL1)); // qZero ← 0
        FunctionArgument innerPlusArgInQ = new FunctionArgument(Plus, List.of(qx1, qx2));
        qInstructions.add(new JumpEqualFunction(qZero, FixedLabel.EXIT, innerPlusArgInQ)); // אם Plus(x1,x2) == 0 → EXIT
        qInstructions.add(new DecreaseInstruction(qx1));
        qInstructions.add(new DecreaseInstruction(qx2));
        qInstructions.add(new IncreaseInstruction(qz1));
        qInstructions.add(new IncreaseInstruction(qy));
        qInstructions.add(new JumpNotZeroInstruction(qx1, qL1));

        SProgram Q = new SProgramImpl("Q", null, qInstructions);

        // ========= תוכנית ראשית P =========
        Variable px1 = new VariableImpl(VariableType.INPUT, 1);
        Variable px2 = new VariableImpl(VariableType.INPUT, 2);
        Variable py  = Variable.RESULT;
        Variable pz1 = new VariableImpl(VariableType.WORK, 1);

        Label pL1 = new LabelImpl(1);

        List<SInstruction> pInstructions = new ArrayList<>();
        FunctionArgument funcArg = new FunctionArgument(Q, List.of(px1, px2));
        pInstructions.add(new IncreaseInstruction(py, pL1));             // y ← y + 1
        pInstructions.add(new QuoteProgramInstruction(pz1, funcArg));    // z1 ← (Q, x1, x2)
        pInstructions.add(new IncreaseInstruction(pz1));                 // z1 ← z1 + 1
        pInstructions.add(new AssignmentInstruction(py, pz1));           // y ← z1
        pInstructions.add(new DecreaseInstruction(px2));                 // x2 ← x2 - 1
        pInstructions.add(new JumpNotZeroInstruction(px2, pL1));         // IF x2 ≠ 0 GOTO L1

        SProgram P = new SProgramImpl("P", null, pInstructions);

        // ========= תוכנית P מורחבת =========
        SProgram expanded_1P = Expansion.expand(P, 1);
        SProgram expanded_2P = Expansion.expand(P, 2);
        SProgram expanded_3P = Expansion.expand(P, 3);

        // ========= הדפסות =========
        System.out.println("=== Program Plus ===");
        System.out.println(Plus.getRepresentation());

        System.out.println("=== Program Q ===");
        System.out.println(Q.getRepresentation());

        System.out.println("=== Program P ===");
        System.out.println(P.getRepresentation());

        System.out.println("=== Expanded_1 Program P ===");
        System.out.println(expanded_1P.getRepresentation());

        System.out.println("=== Expanded_2 Program P ===");
        System.out.println(expanded_2P.getRepresentation());

        System.out.println("=== Expanded_3 Program P ===");
        System.out.println(expanded_3P.getRepresentation());

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
        ProgramExecutor execExpP1 = new ProgramExecutor(expanded_1P);
        ResultCycle resExpP = execExpP1.run(List.of(2L, 3L));
        System.out.println("Result=" + resExpP.getResult() + ", Cycles=" + resExpP.getCycles());

        System.out.println("\n=== Run Expanded_2 P(2,3) ===");
        ProgramExecutor execExp2P = new ProgramExecutor(expanded_2P);
        ResultCycle resExp2P = execExp2P.run(List.of(2L, 3L));
        System.out.println("Result=" + resExp2P.getResult() + ", Cycles=" + resExp2P.getCycles());

        System.out.println("\n=== Run Expanded_3 P(2,3) ===");
        ProgramExecutor execExp3P = new ProgramExecutor(expanded_3P);
        ResultCycle resExp3P = execExp3P.run(List.of(2L, 3L));
        System.out.println("Result=" + resExp3P.getResult() + ", Cycles=" + resExp3P.getCycles());
    }
}*/












//עם ציטוט תוכנית שבתוכה יש ציטוט תוכנית. עובד
/*package consoleUI;

import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import expansion.Expansion;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // ========= פונקציה Plus =========
        Variable plusX1 = new VariableImpl(VariableType.INPUT, 1);
        Variable plusX2 = new VariableImpl(VariableType.INPUT, 2);
        Variable plusY  = Variable.RESULT;

        Label L1 = new LabelImpl(1);
        Label L2 = new LabelImpl(2);
        Label L3 = new LabelImpl(3);
        Label L4 = new LabelImpl(4);

        List<SInstruction> plusInstr = new ArrayList<>();
        plusInstr.add(new ZeroVariableInstruction(plusY));
        plusInstr.add(new JumpZero(plusX1, L3, L1));
        plusInstr.add(new DecreaseInstruction(plusX1));
        plusInstr.add(new IncreaseInstruction(plusY));
        plusInstr.add(new JumpNotZeroInstruction(plusX1, L3));
        plusInstr.add(new NoOpInstruction(plusY, L1));
        plusInstr.add(new JumpZero(plusX2, L4, L2));
        plusInstr.add(new DecreaseInstruction(plusX2));
        plusInstr.add(new IncreaseInstruction(plusY));
        plusInstr.add(new JumpNotZeroInstruction(plusX2, L4));
        plusInstr.add(new NoOpInstruction(plusY, L2));

        SFunction Plus = new SFunction("Plus", "+", null, plusInstr);

        // ========= פונקציה Q =========
        Variable qx1 = new VariableImpl(VariableType.INPUT, 1);
        Variable qx2 = new VariableImpl(VariableType.INPUT, 2);
        Variable qz1 = new VariableImpl(VariableType.WORK, 1);
        Variable qy  = Variable.RESULT;

        Label qL1 = new LabelImpl(1);

        List<SInstruction> qInstructions = new ArrayList<>();
        // כאן נבצע את ציטוט Plus בתוך Q (כשהיא תשתמש ב־qx1 ו־qx2)
        FunctionArgument innerPlusArgInQ = new FunctionArgument(Plus, List.of(qx1, qx2));
        qInstructions.add(new QuoteProgramInstruction(qz1, innerPlusArgInQ)); // z1 ← (Plus, x1, x2)
        qInstructions.add(new JumpZero(qx1, qL1, FixedLabel.EXIT)); // IF x1 = 0 GOTO EXIT
        qInstructions.add(new DecreaseInstruction(qx1));
        qInstructions.add(new DecreaseInstruction(qx2));
        qInstructions.add(new IncreaseInstruction(qz1));
        qInstructions.add(new IncreaseInstruction(qy));
        qInstructions.add(new JumpNotZeroInstruction(qx1, qL1));

        SProgram Q = new SProgramImpl("Q", null, qInstructions);

        // ========= תוכנית ראשית P =========
        Variable px1 = new VariableImpl(VariableType.INPUT, 1);
        Variable px2 = new VariableImpl(VariableType.INPUT, 2);
        Variable py  = Variable.RESULT;
        Variable pz1 = new VariableImpl(VariableType.WORK, 1);

        Label pL1 = new LabelImpl(1);

        List<SInstruction> pInstructions = new ArrayList<>();
        FunctionArgument funcArg = new FunctionArgument(Q, List.of(px1, px2));
        pInstructions.add(new IncreaseInstruction(py, pL1));             // y ← y + 1
        pInstructions.add(new QuoteProgramInstruction(pz1, funcArg));    // z1 ← (Q, x1, x2)
        pInstructions.add(new IncreaseInstruction(pz1));                 // z1 ← z1 + 1
        pInstructions.add(new AssignmentInstruction(py, pz1));           // y ← z1
        pInstructions.add(new DecreaseInstruction(px2));                 // x2 ← x2 - 1
        pInstructions.add(new JumpNotZeroInstruction(px2, pL1));         // IF x2 ≠ 0 GOTO L1

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
















//אביעד מהחלק של הרכבה רק הכנסתי הוראה של וואי מקבל זד אחד, כדי לסבך. עובד
/*package consoleUI;




import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
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
        pInstructions.add(new IncreaseInstruction(Variable.RESULT, pL1));  // y ← y + 1
        pInstructions.add(new QuoteProgramInstruction(pz1, funcArg)); // z1 ← (Q, (Plus, x1, y), y)

        pInstructions.add(new IncreaseInstruction(pz1));    // z1 ← z1 + 1
        pInstructions.add(new AssignmentInstruction(Variable.RESULT, pz1)); // y ← z1
        pInstructions.add(new DecreaseInstruction(px2));    // x2 ← x2 - 1
        //pInstructions.add(new AssignmentInstruction(Variable.RESULT, pz1)); // y ← z1
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
}*/






























//של אביעד, מהחלק של הרכבה. עובד
/*package consoleUI;



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
        pInstructions.add(new IncreaseInstruction(Variable.RESULT, pL1));  // y ← y + 1
        pInstructions.add(new QuoteProgramInstruction(pz1, funcArg)); // z1 ← (Q, (Plus, x1, y), y)

        pInstructions.add(new IncreaseInstruction(pz1));    // z1 ← z1 + 1
        pInstructions.add(new DecreaseInstruction(px2));    // x2 ← x2 - 1
        //pInstructions.add(new AssignmentInstruction(Variable.RESULT, pz1)); // y ← z1
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
}*/










//של אביעד מהחלק של ציטוט תוכנית רק הכנסתי הוראה של וואי מקבל זד אחד. עובד
/*package consoleUI;


import core.logic.program.*;
import core.logic.variable.*;
import core.logic.label.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.*;
import core.logic.execution.ProgramExecutor;
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
        //y <- z1
        pInstructions.add(new AssignmentInstruction(Variable.RESULT, pz1));
        // x2 ← x2 - 1
        pInstructions.add(new DecreaseInstruction(px2));
        // IF x2 ≠ 0 GOTO L1
        pInstructions.add(new JumpNotZeroInstruction(px2, pL1));

        SProgram P = new SProgramImpl("P", null, pInstructions);

        SProgram expandedP = Expansion.expand(P, 1);

        // ========= הדפסה =========
        System.out.println("=== Program P ===");
        System.out.println(P.getRepresentation());

        System.out.println("=== Program Q ===");
        System.out.println(Q.getRepresentation());

        System.out.println("=== Expanded Program P ===");
        System.out.println(expandedP.getRepresentation());

        System.out.println("=== EXECUTION Q ===");
        ProgramExecutor executorQ = new ProgramExecutor(Q);
        System.out.println(executorQ.run(List.of(5L, 5L)).getResult());

        System.out.println("=== EXECUTION P ===");
        ProgramExecutor executorP = new ProgramExecutor(P);
        System.out.println(executorP.run(List.of(5L, 5L)).getResult());

        System.out.println("=== EXECUTION EXPANDED P ===");
        ProgramExecutor executorExpandedP = new ProgramExecutor(expandedP);
        System.out.println(executorExpandedP.run(List.of(5L, 5L)).getResult());
    }
}*/















//של אביעד מהחלק של ציטוט תוכנית. עובד
/*package consoleUI;


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




















//תוכנית עם הוראות ללא ציטוט כדי לראות איך מתנהג. מתנהג טוב
/*package consoleUI;

import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.instruction.mostInstructions.*;
import core.logic.label.LabelImpl;
import core.logic.label.FixedLabel;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import execution.ProgramExecutor;
import execution.ResultCycle;
import expansion.Expansion;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // משתנים
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable x2 = new VariableImpl(VariableType.INPUT, 2);
        Variable y  = new VariableImpl(VariableType.RESULT, 0); // תוצאת הפונקציה
        Variable z1 = new VariableImpl(VariableType.WORK, 1);

        // לייבלים
        LabelImpl L1 = new LabelImpl(1);
        LabelImpl L2 = new LabelImpl(2);

        // הוראות: כולל בסיסיות וסינתטיות, ונוגעים ב-y במקומות שונים
        List<SInstruction> instr = List.of(
                // אפס את y (סינתטי)
                new ZeroVariableInstruction(y),

                // L1: y <- x1  (סינתטי)
                new AssignmentInstruction(y, x1, L1),

                // y <- y + 1 (בסיסי)
                new IncreaseInstruction(y),

                // x2 <- x2 - 1 (בסיסי)
                new DecreaseInstruction(x2),

                // אם x2 ≠ 0 חזור ל-L1
                new JumpNotZeroInstruction(x2, L1),

                // z1 <- 3 (סינתטי)
                new AssignmentInstruction(z1, y),

                // z1 <- z1 + 1 (בסיסי)
                new IncreaseInstruction(z1),

                // אם z1 = 0 לך ל-L2 (סינתטי עם לייבל)
                new JumpZero(z1, L2),

                // y <- z1 (סינתטי)
                new AssignmentInstruction(y, z1),

                // L2: y <- y + 1 (בסיסי)
                new IncreaseInstruction(y, L2)
        );

        // בניית התוכנית
        SProgram P = new SProgramImpl("P", null, instr);
        //בניית התוכנית המורחבת
        SProgram P_expanded = Expansion.expand(P, 1);


        // הדפסה
        System.out.println("=== Program P ===");
        System.out.println(P.getRepresentation());

        // הרצה עם קלט
        ProgramExecutor executor = new ProgramExecutor(P);
        ResultCycle result = executor.run(List.of(4L, 2L)); // למשל x1=4, x2=2
        System.out.println("=== Run P(4,2) ===");
        System.out.println("Result = " + result.getResult());
        System.out.println("Cycles = " + result.getCycles());

        // הדפסת המורחבת
        System.out.println("=== Expanded Program (degree 1) ===");
        System.out.println(P_expanded.getRepresentation());

        ProgramExecutor execExpanded = new ProgramExecutor(P_expanded);
        ResultCycle resExpanded = execExpanded.run(List.of(4L, 2L));
        System.out.println("=== Run Expanded P(4,2) ===");
        System.out.println("Result = " + resExpanded.getResult());
        System.out.println("Cycles = " + resExpanded.getCycles());
    }
}*/













