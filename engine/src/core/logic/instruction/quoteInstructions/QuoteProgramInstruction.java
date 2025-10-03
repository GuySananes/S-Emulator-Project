package core.logic.instruction.quoteInstructions;

import execution.ChangedVariable;
import execution.ExecutionContext;
import execution.LabelCycleChangedVariable;
import execution.ResultCycle;
import core.logic.instruction.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.*;

public class QuoteProgramInstruction extends AbstractInstruction implements Expandable, Quotable {

    private final FunctionArgument functionArgument;

    public QuoteProgramInstruction(Variable variable, FunctionArgument functionArgument) {
        this(variable, FixedLabel.EMPTY, functionArgument);
    }

    public QuoteProgramInstruction(Variable variable, Label label, FunctionArgument functionArgument) {
        super(InstructionData.QUOTE_PROGRAM, variable, label);
        this.functionArgument = functionArgument;
    }

    @Override
    public FunctionArgument getFunctionArgument() {
        return functionArgument;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> variables = super.getVariables();
        variables.addAll(functionArgument.getVariablesInArgumentList());
        return variables;
    }

    @Override
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        ResultCycle result = functionArgument.evaluate(context);
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = result.getResult();
        context.updateVariable(toChange, newValue);
        return new LabelCycleChangedVariable(FixedLabel.EMPTY, result.getCycles(),
                newValue == oldValue ? null :
                        new ChangedVariable(toChange, oldValue, newValue));
    }

    @Override
    public String getCommandRepresentation() {

        return getVariable().getRepresentation() +
                " <- " +
                functionArgument.getRepresentation();
    }

    @Override
    public SInstruction clone() {
        return new QuoteProgramInstruction(getVariable(), getLabel(), functionArgument);
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        SProgram toExpand = functionArgument.getProgram();
        toExpand = toExpand.clone();
        List<SInstruction> toChange = toExpand.getInstructionList();
        Map<Variable, Variable> xyToz = new HashMap<>();
        Map<Label, Label> oldLToNewL = new HashMap<>();
        for (SInstruction instruction : toChange) {
            Variable var = instruction.getVariable();
            if(var != null) {
                Variable z;
                if (!xyToz.containsKey(var)) {
                    z = context.generateZ();
                    xyToz.put(var, z);
                } else {
                    z = xyToz.get(var);
                }

                instruction.setVariable(z);
            }

            Label label = instruction.getLabel();
            if(label != FixedLabel.EMPTY) {
                Label newLabel;
                if(!oldLToNewL.containsKey(label)) {
                    newLabel = context.generateLabel();
                    oldLToNewL.put(label, newLabel);
                }
                else {
                    newLabel = oldLToNewL.get(label);
                }

                instruction.setLabel(newLabel);
            }

            if(instruction instanceof AbstractInstructionTwoLabels twoLabels) {
                Label targetLabel = twoLabels.getTargetLabel();
                if(targetLabel != FixedLabel.EMPTY) {
                    Label newTargetLabel;
                    if(!oldLToNewL.containsKey(targetLabel)) {
                        newTargetLabel = context.generateLabel();
                        oldLToNewL.put(targetLabel, newTargetLabel);
                    }
                    else {
                        newTargetLabel = oldLToNewL.get(targetLabel);
                    }

                    twoLabels.setTargetLabel(newTargetLabel);
                }
            }

            if(instruction instanceof AbstractInstructionTwoVariables twoVariables) {
                Variable secondaryVar = twoVariables.getSecondVariable();
                if(secondaryVar != null) {
                    Variable z;
                    if (!xyToz.containsKey(secondaryVar)) {
                        z = context.generateZ();
                        xyToz.put(secondaryVar, z);
                    } else {
                        z = xyToz.get(secondaryVar);
                    }

                    twoVariables.setSecondVariable(z);
                }
            }

            if(instruction instanceof JumpEqualVariable jeqv) {
                Label targetLabel = jeqv.getTargetLabel();
                if(targetLabel != FixedLabel.EMPTY) {
                    Label newTargetLabel;
                    if(!oldLToNewL.containsKey(targetLabel)) {
                        newTargetLabel = context.generateLabel();
                        oldLToNewL.put(targetLabel, newTargetLabel);
                    }
                    else {
                        newTargetLabel = oldLToNewL.get(targetLabel);
                    }

                    jeqv.setTargetLabel(newTargetLabel);
                }
            }
        }

        List<SInstruction> expansion = new ArrayList<>(toChange.size() + 2);
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd = new NoOpInstruction(Variable.RESULT, getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        List<Argument> arguments = functionArgument.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            Variable x = new VariableImpl(VariableType.INPUT, i + 1);
            if(xyToz.containsKey(x)) {
                Variable z = xyToz.get(x);
                Variable zDeepCopy = new VariableImpl(VariableType.WORK, z.getNumber());
                if(arguments.get(i) instanceof Variable var) {
                    toAdd = new AssignmentInstruction(zDeepCopy, new VariableImpl(var.getType(), var.getNumber()));
                    Utils.registerInstruction(toAdd, parentChain, expansion);
                }

                else {
                    toAdd = new QuoteProgramInstruction(zDeepCopy, (FunctionArgument) arguments.get(i));
                    Utils.registerInstruction(toAdd, parentChain, expansion);
                }
            }
        }

        Utils.registerInstructions(toChange, parentChain, expansion);
        if(oldLToNewL.containsKey(FixedLabel.EXIT)) {
            Label newExit = oldLToNewL.get(FixedLabel.EXIT);
            toAdd = new AssignmentInstruction(getVariable(), xyToz.get(Variable.RESULT), newExit);
        } else {
            toAdd = new AssignmentInstruction(getVariable(), xyToz.get(Variable.RESULT));
        }

        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public int getDegree() {
        return functionArgument.getDegree();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        QuoteProgramInstruction that = (QuoteProgramInstruction) o;
        return Objects.equals(functionArgument, that.functionArgument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), functionArgument);
    }
}
