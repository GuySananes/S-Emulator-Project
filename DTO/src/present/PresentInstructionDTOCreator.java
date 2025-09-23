package present;

import core.logic.instruction.*;

public class PresentInstructionDTOCreator {

    public static PresentInstructionDTO create(SInstruction instruction) {
        InstructionData type = instruction.getInstructionData();
        switch (type) {
            case INCREASE, DECREASE, NO_OP, ZERO_VARIABLE -> {
                return new PresentInstructionDTO(
                        type,
                        instruction.getVariableCopy(),
                        instruction.getLabel(),
                        instruction.getIndex(),
                        instruction.getRepresentation());
            }

            case JUMP_NOT_ZERO, GOTO_LABEL, JUMP_ZERO -> {
                AbstractInstructionTwoLabels instructionTwoLabels =
                        (AbstractInstructionTwoLabels)instruction;
                return new PresentInstructionTwoLabelsDTO(
                        type,
                        instructionTwoLabels.getVariableCopy(),
                        instructionTwoLabels.getLabel(),
                        instructionTwoLabels.getTargetLabel(),
                        instructionTwoLabels.getIndex(),
                        instructionTwoLabels.getRepresentation());
            }

            case ASSIGNMENT -> {
                AbstractInstructionTwoVariables instructionTwoVariables =
                        (AbstractInstructionTwoVariables)instruction;
                return new PresentInstructionTwoVariablesDTO(
                        type,
                        instructionTwoVariables.getVariableCopy(),
                        instructionTwoVariables.getSecondaryVariable(),
                        instructionTwoVariables.getLabel(),
                        instructionTwoVariables.getIndex(),
                        instructionTwoVariables.getRepresentation());
            }

            case CONSTANT_ASSIGNMENT -> {
                ConstantAssignmentInstruction constantAssignmentInstruction =
                        (ConstantAssignmentInstruction)instruction;
                return new PresentConstantAssignmentInstructionDTO(
                        type,
                        constantAssignmentInstruction.getVariableCopy(),
                        constantAssignmentInstruction.getLabel(),
                        constantAssignmentInstruction.getConstantValue(),
                        constantAssignmentInstruction.getIndex(),
                        constantAssignmentInstruction.getRepresentation());
            }

            case JUMP_EQUAL_CONSTANT -> {
                JumpEqualConstant jumpEqualConstant =
                        (JumpEqualConstant)instruction;
                return new PresentJumpEqualConstantInstructionDTO(
                        type,
                        jumpEqualConstant.getVariableCopy(),
                        jumpEqualConstant.getLabel(),
                        jumpEqualConstant.getTargetLabel(),
                        jumpEqualConstant.getConstantValue(),
                        jumpEqualConstant.getIndex(),
                        jumpEqualConstant.getRepresentation());
            }

            case JUMP_EQUAL_VARIABLE -> {
                JumpEqualVariable jumpEqualVariable =
                        (JumpEqualVariable)instruction;
                return new PresentJumpEqualVariableDTO(
                        type,
                        jumpEqualVariable.getVariableCopy(),
                        jumpEqualVariable.getSecondaryVariable(),
                        jumpEqualVariable.getLabel(),
                        jumpEqualVariable.getTargetLabel(),
                        jumpEqualVariable.getIndex(),
                        jumpEqualVariable.getRepresentation());
            }

            default -> throw new IllegalStateException("in PresentInstructionDTOCreator: unexpected instruction type: " + type);
        }
    }
}
