package expansion;

import core.logic.instruction.IndexedInstruction;
import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import util.Util;

import java.util.ArrayList;
import java.util.List;

public class Expansion {

    public static SProgram expand(SProgram program, int degree) {
        if (degree < 0 || degree > program.calculateMaxDegree())
            throw new IllegalArgumentException("Degree must be between 0 and " +
                    program.calculateMaxDegree() + " when expanding program");

        List<IndexedInstruction> indexedInstructions = Util.makeIndexedInstructionList(program);
        ExpansionContext expansionContext = new ExpansionContext(program);

        for(int d = 0; d < degree; d++) {
            indexedInstructions = expand(indexedInstructions, expansionContext);
        }

        List<SInstruction> finalInstructions = new ArrayList<>();
        for (IndexedInstruction indexedInstruction : indexedInstructions) {
            finalInstructions.add(indexedInstruction.getInstruction());
        }

        SProgram result = new SProgramImpl(program.getName() +
                (degree > 0 ? "_" + degree + "D" : ""));
        result.addInstructions(finalInstructions);
        return result;
    }

    public static List<IndexedInstruction> expand
            (List<IndexedInstruction> indexedInstructions, ExpansionContext context) {
        List<IndexedInstruction> expandedInstructions = new ArrayList<>(indexedInstructions.size());

        int index = 1;
        for (IndexedInstruction indexedInstruction : indexedInstructions) {
            context.setParentIndex(indexedInstruction.getIndex());
            SInstruction instruction = indexedInstruction.getInstruction();
            if(instruction instanceof RootedInstruction ri) {
                instruction = ri.getInstruction();
            }
            if (instruction instanceof Expandable expandable) {
                List<SInstruction> newInstructions = expandable.expand(context);
                for (SInstruction newInstr : newInstructions) {
                    expandedInstructions.add(new IndexedInstruction(index++, newInstr));
                }
            }
            else {
                expandedInstructions.add(new IndexedInstruction(index++, instruction));
            }
        }

        return expandedInstructions;
    }
}
