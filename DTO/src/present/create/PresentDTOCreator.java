package present.create;

import core.logic.instruction.*;
import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.Argument;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.QuoteProgramInstruction;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import present.quote.ArgumentDTO;
import present.quote.FunctionArgumentDTO;
import present.mostInstructions.*;
import java.util.ArrayList;
import java.util.List;

public class PresentDTOCreator {

    public static PresentInstructionDTO createPresentInstructionDTO(SInstruction instruction) {
        InstructionData type = instruction.getInstructionData();
        PresentInstructionDTO presentInstructionDTO = new PresentInstructionDTO(instruction);

        switch (type) {
            case INCREASE, DECREASE, NO_OP, ZERO_VARIABLE -> {
                return presentInstructionDTO;
            }

            case JUMP_NOT_ZERO, GOTO_LABEL, JUMP_ZERO -> {
                AbstractInstructionTwoLabels instructionTwoLabels =
                        (AbstractInstructionTwoLabels) instruction;
                return new PresentInstructionTwoLabelsDTO(
                        presentInstructionDTO,
                        instructionTwoLabels.getTargetLabelDeepCopy());
            }

            case ASSIGNMENT -> {
                AbstractInstructionTwoVariables instructionTwoVariables =
                        (AbstractInstructionTwoVariables) instruction;
                return new PresentInstructionTwoVariablesDTO(
                        presentInstructionDTO,
                        instructionTwoVariables.getSecondVariableDeepCopy());
            }

            case CONSTANT_ASSIGNMENT -> {
                ConstantAssignmentInstruction constantAssignmentInstruction =
                        (ConstantAssignmentInstruction) instruction;
                return new PresentConstantAssignmentInstructionDTO(
                        presentInstructionDTO,
                        constantAssignmentInstruction.getConstantValue());
            }

            case JUMP_EQUAL_CONSTANT -> {
                JumpEqualConstant jumpEqualConstant =
                        (JumpEqualConstant) instruction;
                return new PresentJumpEqualConstantInstructionDTO(
                        presentInstructionDTO,
                        jumpEqualConstant.getTargetLabel(),
                        jumpEqualConstant.getConstantValue());
            }

            case JUMP_EQUAL_VARIABLE -> {
                JumpEqualVariable jumpEqualVariable =
                        (JumpEqualVariable) instruction;
                return new PresentJumpEqualVariableDTO(
                        presentInstructionDTO,
                        jumpEqualVariable.getSecondVariableDeepCopy(),
                        jumpEqualVariable.getTargetLabelDeepCopy());
            }

            case QUOTE_PROGRAM -> {
                QuoteProgramInstruction quoteProgramInstruction =
                        (QuoteProgramInstruction) instruction;
                return new PresentQuoteProgramInstructionDTO(
                        presentInstructionDTO,
                        createFunctionArgumentDTO(quoteProgramInstruction.getFunctionArgument()));
            }


            default ->
                    throw new IllegalStateException("in PresentInstructionDTOCreator: unexpected instruction type: " + type);
        }
    }

    private static FunctionArgumentDTO createFunctionArgumentDTO(FunctionArgument functionArgument){
        SProgram program = functionArgument.getProgram();
        String programOrFunctionName = program instanceof SFunction ?
                ((SFunction) program).getUserName() :
                program.getName();

        List<Argument> arguments = functionArgument.getArguments();
        List<ArgumentDTO> argumentDTOList = new ArrayList<>(arguments.size());
        for (Argument argument : arguments) {
            if (argument instanceof FunctionArgument) {
                argumentDTOList.add(createFunctionArgumentDTO((FunctionArgument) argument));
            } else {
                argumentDTOList.add(((Variable) argument).deepCopy());
            }
        }

        return new FunctionArgumentDTO(programOrFunctionName, argumentDTOList, functionArgument.getRepresentation());
    }

}

