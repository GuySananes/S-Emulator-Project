package core.logic.instruction.quoteInstruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.execution.ResultCycle;
import core.logic.instruction.*;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteProgramInstruction extends AbstractInstruction implements Expandable {

    FunctionArgument functionArgument;

    public QuoteProgramInstruction(Variable variable, FunctionArgument functionArgument) {
        this(variable, FixedLabel.EMPTY, functionArgument);
    }

    public QuoteProgramInstruction(Variable variable, Label label, FunctionArgument functionArgument) {
        super(InstructionData.QUOTE_PROGRAM, variable, label);
        this.functionArgument = functionArgument;
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {
        ResultCycle result = functionArgument.evaluate(context);
        context.updateVariable(getVariable(), result.getResult());
        return new LabelCycle(FixedLabel.EMPTY, result.getCycles());
    }

    @Override
    public String getCommandRepresentation() {

        StringBuilder sb = new StringBuilder();
        sb.append(getVariable().getRepresentation());
        sb.append(" <- ");
        sb.append(functionArgument.getRepresentation());

        return sb.toString();
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
                Variable secondaryVar = twoVariables.getSecondaryVariable();
                if(secondaryVar != null) {
                    Variable z;
                    if (!xyToz.containsKey(secondaryVar)) {
                        z = context.generateZ();
                        xyToz.put(secondaryVar, z);
                    } else {
                        z = xyToz.get(secondaryVar);
                    }

                    twoVariables.setSecondaryVariable(z);
                }
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
        toAdd = new AssignmentInstruction(getVariable(), xyToz.get(Variable.RESULT));
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }
}
