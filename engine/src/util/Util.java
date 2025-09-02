package util;

import core.logic.instruction.IndexedInstruction;
import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<IndexedInstruction> makeIndexedInstructionList(List<SInstruction> instructions) {
        List<IndexedInstruction> indexedInstructions = new ArrayList<>(instructions.size());

        for(int i = 0; i < instructions.size(); i++) {
            SInstruction current = instructions.get(i);

            if(current instanceof IndexedInstruction ii) {
                indexedInstructions.add(new IndexedInstruction(i, ii.getInstruction()));
            } else {
                indexedInstructions.add(new IndexedInstruction(i, instructions.get(i)));
            }
        }

        return indexedInstructions;
    }

    public static List<IndexedInstruction> makeIndexedInstructionList(SProgram program) {
        return makeIndexedInstructionList(program.getInstructionList());
    }

    public static SProgram makeIndexedProgram(SProgram program) {
        List<IndexedInstruction> indexedInstructions = makeIndexedInstructionList(program);
        SProgram indexedProgram = new SProgramImpl(program.getName() + "_indexed");
        indexedProgram.addInstructions(new ArrayList<>(indexedInstructions));
        return indexedProgram;
    }
}
