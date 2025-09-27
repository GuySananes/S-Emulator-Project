package present.program;

import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.label.Label;
import present.create.PresentDTOCreator;
import present.mostInstructions.PresentInstructionDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PresentProgramDTO {
    private final String programName;
    private final Set<Variable> Xs;
    private final Set<Label> labels;
    private final List<PresentInstructionDTO> instructionList;
    private final String representation;
    private final int currentProgramDegree;
    private final int originMaxDegree;
    private final int numOfBasicInstructions;
    private final int numOfStaticInstructions;

    public PresentProgramDTO(SProgram program) {
        this.programName = program.getName();
        this.Xs = program.getInputVariablesDeepCopy();
        this.labels = program.getOrderedLabelsDeepCopy();
        this.instructionList = program.getInstructionList().stream()
                        .map(PresentDTOCreator::createPresentInstructionDTO)
                        .collect(Collectors.toList());
        this.representation = program.getRepresentation();
        this.currentProgramDegree = program.getOriginalProgram().getDegree() - program.getDegree();
        this.originMaxDegree = program.getOriginalProgram().getDegree();
        this.numOfBasicInstructions = program.getNumOfBasicInstructions();
        this.numOfStaticInstructions = program.getNumOfStaticInstructions();
    }

    public String getProgramName() { return programName;}
    public Set<Variable> getXs() { return Xs; }
    public Set<Label> getLabels() { return labels; }
    public List<PresentInstructionDTO> getInstructionList() { return instructionList; }
    public String getRepresentation() { return representation; }
    public int getOriginMaxDegree() { return originMaxDegree; }
    public int getCurrentProgramDegree() { return currentProgramDegree; }
}
