package core.logic.instruction.quoteInstructions;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycleChangedVariable;
import core.logic.execution.ResultCycle;
import core.logic.instruction.InstructionData;
import core.logic.instruction.mostInstructions.AbstractInstructionTwoLabels;
import core.logic.instruction.mostInstructions.JumpEqualVariable;
import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.*;

public class JumpEqualFunction extends AbstractInstructionTwoLabels implements Expandable, Quotable {

    private final FunctionArgument functionArgument;

    public JumpEqualFunction(Variable variable, Label label, Label targetLabel, FunctionArgument functionArgument) {
        super(InstructionData.Jump_Equal_Function, variable, label, targetLabel);
        this.functionArgument = functionArgument;
    }

    public JumpEqualFunction(Variable variable, Label targetLabel, FunctionArgument functionArgument) {
        this(variable, FixedLabel.EMPTY, targetLabel, functionArgument);
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
    public void setVariablesInFunctionArgument(Map<Variable, Variable> xyzToz, ExpansionContext context) {
        functionArgument.setArgumentsThatAreVariable(xyzToz, context);
    }

    @Override
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        ResultCycle resultCycle = functionArgument.evaluate(context);

        if (variableValue == resultCycle.getResult()) {
            return new LabelCycleChangedVariable(getTargetLabel(),
                    resultCycle.getCycles() + getInstructionData().getCycles(),
                    null);
        }

        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                resultCycle.getCycles(),
                null);
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        List<SInstruction> expansion = new ArrayList<>(2);
        Variable z = context.generateZ();
        SInstruction toAdd = new QuoteProgramInstruction(z, getLabel(), functionArgument);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpEqualVariable(getVariable(), z, getTargetLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public SInstruction clone() {
        return new JumpEqualFunction(getVariable(), getLabel(), getTargetLabel(), functionArgument.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JumpEqualFunction that = (JumpEqualFunction) o;
        return Objects.equals(functionArgument, that.functionArgument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), functionArgument);
    }

    @Override
    protected String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = " + functionArgument.getRepresentation() + " GOTO " + getTargetLabel().getRepresentation();
    }

    @Override
    public int getDegree() {
        return functionArgument.getDegree() + 3;
    }
}
