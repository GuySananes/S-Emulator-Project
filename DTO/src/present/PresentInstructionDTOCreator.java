package present;

import core.logic.instruction.*;

public class PresentInstructionDTOCreator {

    public static PresentInstructionDTO create(SInstruction instruction) {
        InstructionData type = instruction.getInstructionData();
        switch (type) {
            case INCREASE, DECREASE, NO_OP, ZERO_VARIABLE -> {
                return new PresentInstructionDTO(
                        instruction.getInstructionData(),
                        instruction.getVariableCopy(),
                        instruction.getLabel(),
                        instruction.getIndex(),
                        instruction.getRepresentation());
            }

            case JUMP_NOT_ZERO, GOTO_LABEL, JUMP_ZERO -> {
                AbstractInstructionTwoLabels instructionTwoLabels =
                        (AbstractInstructionTwoLabels)instruction;
                return new PresentInstructionTwoLabelsDTO(
                        instructionTwoLabels.getInstructionData(),
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
                        instructionTwoVariables.getInstructionData(),
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
                        constantAssignmentInstruction.getInstructionData(),
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
                        jumpEqualConstant.getInstructionData(),
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
                        jumpEqualVariable.getInstructionData(),
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
