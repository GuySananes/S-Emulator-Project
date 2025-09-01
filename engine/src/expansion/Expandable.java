package expansion;

import core.logic.instruction.SInstruction;

import java.util.List;

public interface Expandable {
    List<SInstruction> expand(ExpansionContext context);
}
