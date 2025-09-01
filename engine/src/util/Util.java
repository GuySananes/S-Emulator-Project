package util;

import core.logic.instruction.IndexedInstruction;
import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<IndexedInstruction> makeIndexedInstructionList(SProgram program){
        List<SInstruction> instructions = program.getInstructionList();
        List<IndexedInstruction> indexedInstructions = new ArrayList<>(instructions.size());

        for(int i = 0; i < instructions.size(); i++) {
            if(instructions.get(i) instanceof IndexedInstruction) {
                indexedInstructions.add(new IndexedInstruction
                        (i + 1, ((IndexedInstruction)instructions.get(i)).getInstruction()));
            }

            else {
                indexedInstructions.add(new IndexedInstruction(i + 1, instructions.get(i)));
            }
        }

        return indexedInstructions;
    }
}
