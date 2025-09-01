package expansion;

import core.logic.instruction.IndexedInstruction;
import core.logic.instruction.SInstruction;
import core.logic.program.SProgram;
import util.Util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Expansion {

    public static SProgram expand(SProgram program, int degree) {
        if(degree < 0 || degree > program.calculateMaxDegree())
            throw new IllegalArgumentException("Degree must be between 0 and " +
                    program.calculateMaxDegree() + "when expanding program");

        List<IndexedInstruction> indexedInstructions = Util.makeIndexedInstructionList(program);
        List<IndexedInstruction> expandedInstructions = new LinkedList<>(indexedInstructions);
        ExpansionContext expansionContext = new ExpansionContext(program);

        for(int i = 0; i < degree; i++){
            for(int j = 0; j < expandedInstructions.size(); j++){
                SInstruction instruction = expandedInstructions.get(j).getInstruction();
                if(instruction instanceof Expandable){
                    expandedInstructions.remove(j);
                    List<SInstruction> newInstructions = ((Expandable) instr).expand(expansionContext);
                    for(SInstruction newInstruction : newInstructions){
                        expandedInstructions.add(new IndexedInstruction(index++, newInstruction));
                    }
                }
                else{
                    expandedInstructions.add(new IndexedInstruction(index++, instruction));
                }
            }
        }




    }
}
