package expansion;

import core.logic.instruction.SInstruction;

import java.util.List;

public class Utils {

    private Utils() {
    }

    public static void registerInstruction(SInstruction instruction,
                                           List<SInstruction> parents,
                                           List<SInstruction> expansionList) {
        instruction.setParents(parents);
        expansionList.add(instruction);
    }

    public static void registerInstructions(List<SInstruction> instructions,
                                            List<SInstruction> parents,
                                            List<SInstruction> expansionList) {
        for (SInstruction instruction : instructions) {
            registerInstruction(instruction, parents, expansionList);
        }
    }






}
