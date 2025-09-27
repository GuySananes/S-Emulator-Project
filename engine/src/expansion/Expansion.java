package expansion;


import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;


import java.util.ArrayList;
import java.util.List;

public class Expansion {

    public static SProgram expand(SProgram program, int degree) {
        if (degree < program.getMinDegree() + 1 || degree > program.getDegree()) {
            throw new IllegalArgumentException("Degree must be between 1 and "
                    + program.getDegree() + " when expanding program");
        }

        List<SInstruction> instructions = program.getInstructionList();
        ExpansionContext expansionContext = new ExpansionContext(program);

        for(int d = 0; d < degree; d++) {
            instructions = expand(instructions, expansionContext);
        }

        SProgram expandedProgram;
        String programName = program.getName() + "_" + degree + "D";

        if(program instanceof SFunction sf) {
            expandedProgram = new SFunction(programName, sf.getUserName(), program);
        }
        else {
            expandedProgram = new SProgramImpl(programName, program);
        }

        expandedProgram.addInstructions(instructions);
        return expandedProgram;
    }

    public static List<SInstruction> expand(List<SInstruction> instructions, ExpansionContext context) {
        List<SInstruction> expandedList = new ArrayList<>(instructions.size());
        for (SInstruction instruction : instructions) {
            if (instruction instanceof Expandable expandable) {
                expandedList.addAll(expandable.expand(context));
            }
            else {
                expandedList.add(instruction);
            }
        }

        int index = 1;
        for (SInstruction instruction : expandedList) {
            instruction.setIndex(index++);
        }

        return expandedList;
    }
}
