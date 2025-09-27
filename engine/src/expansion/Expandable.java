package expansion;

import core.logic.instruction.mostInstructions.SInstruction;

import java.util.List;

public interface Expandable {
    List<SInstruction> expand(ExpansionContext context);
}
