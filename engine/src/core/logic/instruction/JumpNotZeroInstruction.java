package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class JumpNotZeroInstruction extends AbstractInstruction{

    private final Label jnzLabel;

    /**
     * Constructs a JumpNotZeroInstruction with the specified variable and jump label.
     * Defaults to an empty label.
     *
     * @param variable the variable to check
     * @param jnzLabel the label to jump to if the variable is not zero
     */
    public JumpNotZeroInstruction(Variable variable, Label jnzLabel) {
        this(variable, jnzLabel, FixedLabel.EMPTY);
    }

    /**
     * Constructs a JumpNotZeroInstruction with the specified variable, jump label, and instruction label.
     *
     * @param variable the variable to check
     * @param jnzLabel the label to jump to if the variable is not zero
     * @param label    the label for this instruction
     */
    public JumpNotZeroInstruction(Variable variable, Label jnzLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label);
        this.jnzLabel = jnzLabel;
    }

    /**
     * Executes the instruction.
     * If the variable's value is not zero, it returns the jump label.
     * Otherwise, it returns an empty label.
     *
     * @param context the execution context
     * @return the label to jump to or an empty label
     */
    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }
}