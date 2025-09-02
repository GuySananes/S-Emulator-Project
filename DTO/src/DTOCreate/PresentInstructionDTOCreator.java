package DTOCreate;

import DTO.*;
import core.logic.instruction.*;

public class PresentInstructionDTOCreator {

    public static PresentInstructionDTO create(IndexedInstruction indexedInstruction) {
        InstructionData type = indexedInstruction.getInstructionData();
        switch (type) {
            case INCREASE, DECREASE, NO_OP, ZERO_VARIABLE -> {
                return new PresentInstructionDTO(
                        indexedInstruction.getInstructionData(),
                        indexedInstruction.getVariableCopy(),
                        indexedInstruction.getLabel(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex());
            }

            case JUMP_NOT_ZERO, GOTO_LABEL, JUMP_ZERO -> {
                AbstractInstructionTwoLabels instructionTwoLabels = (AbstractInstructionTwoLabels)
                        indexedInstruction.getInstruction();
                return new PresentInstructionTwoLabelsDTO(
                        instructionTwoLabels.getInstructionData(),
                        instructionTwoLabels.getVariableCopy(),
                        instructionTwoLabels.getLabel(),
                        instructionTwoLabels.getTargetLabel(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex()
                );
            }

            case ASSIGNMENT -> {
                AbstractInstructionTwoVariables instructionTwoVariables = (AbstractInstructionTwoVariables)
                        indexedInstruction.getInstruction();
                return new PresentInstructionTwoVariablesDTO(
                        instructionTwoVariables.getInstructionData(),
                        instructionTwoVariables.getVariableCopy(),
                        instructionTwoVariables.getSecondaryVariable(),
                        instructionTwoVariables.getLabel(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex()
                );
            }

            case CONSTANT_ASSIGNMENT -> {
                ConstantAssignmentInstruction constantAssignmentInstruction = (ConstantAssignmentInstruction)
                        indexedInstruction.getInstruction();
                return new PresentConstantAssignmentInstructionDTO(
                        constantAssignmentInstruction.getInstructionData(),
                        constantAssignmentInstruction.getVariableCopy(),
                        constantAssignmentInstruction.getLabel(),
                        constantAssignmentInstruction.getConstantValue(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex()
                );
            }

            case JUMP_EQUAL_CONSTANT -> {
                JumpEqualConstant jumpEqualConstant = (JumpEqualConstant)
                        indexedInstruction.getInstruction();
                return new PresentJumpEqualConstantInstructionDTO(
                        jumpEqualConstant.getInstructionData(),
                        jumpEqualConstant.getVariableCopy(),
                        jumpEqualConstant.getLabel(),
                        jumpEqualConstant.getTargetLabel(),
                        jumpEqualConstant.getConstantValue(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex()
                );
            }

            case JUMP_EQUAL_VARIABLE -> {
                JumpEqualVariable jumpEqualVariable = (JumpEqualVariable)
                        indexedInstruction.getInstruction();
                return new PresentJumpEqualVariableDTO(
                        jumpEqualVariable.getInstructionData(),
                        jumpEqualVariable.getVariableCopy(),
                        jumpEqualVariable.getSecondaryVariable(),
                        jumpEqualVariable.getLabel(),
                        jumpEqualVariable.getTargetLabel(),
                        indexedInstruction.getRepresentation(),
                        indexedInstruction.getIndex()
                );
            }

            default -> throw new IllegalStateException("in PresentInstructionDTOCreator: unexpected value: " + type);
        }
    }
}
