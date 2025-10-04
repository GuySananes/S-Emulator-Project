
package javafxUI.service;

import javafxUI.model.ui.Instruction;
import javafxUI.model.ui.Program;
import javafxUI.model.ui.Variable;
import present.mostInstructions.PresentInstructionDTO;
import present.program.PresentProgramDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO + legacy converter.
 * Converts between different program representations:
 * - PresentProgramDTO to UI Program
 * - Legacy SProgram to UI Program  
 * - Statistics and execution results
 */
public class ModelConverter {

    /* ===================== DTO-BASED CONVERSIONS ===================== */

    public static Program convertProgram(PresentProgramDTO dto) {
        Program program = new Program();
        if (dto == null) return program;

        program.setName(dto.getProgramName());
        program.setLoaded(true);

        convertInstructions(dto).forEach(program::addInstruction);
        convertVariables(dto).forEach(program::addVariable);

        return program;
    }

    public static List<Instruction> convertInstructions(PresentProgramDTO dto) {
        List<Instruction> instructions = new ArrayList<>();
        if (dto == null || dto.getInstructionList() == null) return instructions;

        int lineNumber = 1;
        for (PresentInstructionDTO instructionDTO : dto.getInstructionList()) {
            if (instructionDTO == null) continue;

            int line = instructionDTO.getIndex() > 0 ? instructionDTO.getIndex() : lineNumber++;
            String display = formatDTOInstruction(instructionDTO);
            String type = deriveTypeFromRepresentation(instructionDTO.getRepresentation());

            // FIXED: Get actual cycles from the DTO instead of hardcoding 1
            int cycles = getInstructionCycles(instructionDTO);

            instructions.add(new Instruction(line, type, cycles, display));
        }

        return instructions;
    }

    // this helper method extracts cycles from the DTO
    private static int getInstructionCycles(PresentInstructionDTO instructionDTO) {
        // Try to get cycles from InstructionData if available
        if (instructionDTO.getInstructionData() != null) {
            try {
                String cycleString = instructionDTO.getInstructionData().getCycleRepresentation();
                if (cycleString != null && !cycleString.trim().isEmpty()) {
                    return Integer.parseInt(cycleString.trim());
                }
            } catch (NumberFormatException e) {
                // If parsing fails, fall through to fallback logic
            }
        }


                // Fallback: try to derive cycles from instruction type if InstructionData is null
        String representation = instructionDTO.getRepresentation();
        if (representation != null) {
            String lower = representation.toLowerCase();

            // Different instruction types have different cycle costs
            if (lower.contains("jump") || lower.contains("goto")) {
                return 1; // Branch instructions typically cost 1 cycle
            } else if (lower.contains("increase") || lower.contains("decrease")) {
                return 1; // Simple arithmetic operations
            } else if (lower.contains("assignment")) {
                return 1; // Assignment operations
            }
        }

        // Default fallback
        return 1;
    }

    public static List<Variable> convertVariables(PresentProgramDTO dto) {
        List<Variable> variables = new ArrayList<>();
        if (dto == null || dto.getXs() == null) return variables;

        for (core.logic.variable.Variable engineVar : dto.getXs()) {
            if (engineVar != null) {
                variables.add(new Variable(
                        engineVar.getRepresentation(),
                        engineVar.getNumber(),
                        engineVar.getType().name()
                ));
            }
        }

        return variables;
    }

    /* ===================== PRIVATE HELPER METHODS ===================== */

    private static String formatDTOInstruction(PresentInstructionDTO instructionDTO) {
        String label = extractLabel(instructionDTO);
        String coreInstruction = extractCoreInstruction(instructionDTO);

        return String.format("[%s] %s",
                label.isBlank() ? "  " : label,
                coreInstruction
        );
    }

    private static String extractLabel(PresentInstructionDTO instructionDTO) {
        return instructionDTO.getLabel() != null ?
                instructionDTO.getLabel().getRepresentation() : "";
    }

    private static String extractCoreInstruction(PresentInstructionDTO instructionDTO) {
        String representation = instructionDTO.getRepresentation();
        if (representation == null) return "";

        return representation
                .replaceFirst("^#\\d+\\s*\\([A-Za-z]\\)\\s*", "")
                .replaceFirst("^\\[\\s*[^]]*\\]\\s*", "")
                .replaceFirst("\\(\\d+\\)\\s*$", "")
                .trim();
    }

    private static String deriveTypeFromRepresentation(String representation) {
        if (representation == null) return "S";

        String lower = representation.toLowerCase();
        return (lower.contains("jump") || lower.contains("goto")) ? "B" : "S";
    }
}