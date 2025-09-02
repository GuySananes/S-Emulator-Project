package DTO;

import core.logic.variable.Variable;
import core.logic.label.Label;

import java.util.List;
import java.util.Set;

public class PresentProgramDTO {
    private final String programName;
    private final Set<Variable> Xs;
    private final Set<Label> labels;
    private final List<PresentInstructionDTO> instructionList;
    private final String representation;

    public PresentProgramDTO(String programName, Set<Variable> Xs, Set<Label> labels,
                             List<PresentInstructionDTO> instructionList, String representation) {
        this.programName = programName;
        this.Xs = Xs;
        this.labels = labels;
        this.instructionList = instructionList;
        this.representation = representation;

    }

    public String getProgramName() { return programName;}
    public Set<Variable> getXs() { return Xs; }
    public Set<Label> getLabels() { return labels; }
    public List<PresentInstructionDTO> getInstructionList() { return instructionList; }
    public String getRepresentation() { return representation; }
}
