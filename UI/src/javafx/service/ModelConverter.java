
package javafx.service;

import javafx.model.ui.Instruction;
import javafx.model.ui.Program;
import javafx.model.ui.SLabel;
import javafx.model.ui.Variable;
import javafx.model.ui.Statistic;
import present.PresentProgramDTO;
import present.PresentInstructionDTO;
import expand.ExpandDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;
import core.logic.program.SProgram;
import core.logic.instruction.SInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            return instructionDTO.getInstructionData().getCycles();
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

    public static List<SLabel> convertLabels(PresentProgramDTO dto) {
        List<SLabel> labels = new ArrayList<>();
        if (dto == null || dto.getLabels() == null) return labels;

        int position = 1;
        for (core.logic.label.Label label : dto.getLabels()) {
            if (label != null) {
                labels.add(new SLabel(label.getRepresentation(), position++, "label"));
            }
        }

        return labels;
    }

    public static void applyRunResult(RunProgramDTO runDTO, Program program,
                                      List<Long> orderedValues,
                                      List<core.logic.variable.Variable> orderedVars,
                                      int cycles) {
        if (runDTO == null || program == null) return;

        if (cycles >= 0) {
            program.setTotalCycles(cycles);
        }

        if (orderedVars != null && orderedValues != null &&
                orderedVars.size() == orderedValues.size()) {
            updateProgramVariables(program, orderedVars, orderedValues);
        }
    }

    public static Statistic convertStatistics(ProgramStatisticDTO stats) {
        if (stats == null) return new Statistic();

        String representation = getStatisticsRepresentation(stats);
        return new Statistic("Runs", 0, 0, representation);
    }

    public static Program expandToProgram(ExpandDTO expandDTO, int degree) throws Exception {
        PresentProgramDTO expanded = expandDTO.expand(degree);
        return convertProgram(expanded);
    }

    /* ===================== LEGACY ENGINE CONVERSIONS ===================== */

    public static Program convertProgram(SProgram engineProgram) {
        Program program = new Program();
        if (engineProgram == null) return program;

        program.setName(engineProgram.getName());
        program.setMaxDegree(engineProgram.calculateMaxDegree());
        program.setMinDegree(engineProgram.getMinDegree());
        program.setTotalCycles(engineProgram.calculateCycles());
        program.setLoaded(true);

        convertInstructions(engineProgram.getInstructionList()).forEach(program::addInstruction);
        convertVariables(engineProgram.getOrderedVariables()).forEach(program::addVariable);

        return program;
    }

    public static List<Instruction> convertInstructions(List<SInstruction> engineInstructions) {
        List<Instruction> instructions = new ArrayList<>();
        if (engineInstructions == null) return instructions;

        for (int i = 0; i < engineInstructions.size(); i++) {
            SInstruction engineInstruction = engineInstructions.get(i);
            String formattedInstruction = legacyFormatInstruction(i + 1, engineInstruction);

            instructions.add(new Instruction(
                    i + 1,
                    legacyDetermineType(engineInstruction),
                    engineInstruction.getCycles(),
                    formattedInstruction
            ));
        }

        return instructions;
    }

    public static List<Variable> convertVariables(Set<core.logic.variable.Variable> engineVariables) {
        List<Variable> variables = new ArrayList<>();
        if (engineVariables == null) return variables;

        for (core.logic.variable.Variable engineVar : engineVariables) {
            variables.add(new Variable(
                    engineVar.getRepresentation(),
                    engineVar.getNumber(),
                    engineVar.getType().toString()
            ));
        }

        return variables;
    }

    public static List<SLabel> convertLabels(Set<core.logic.label.Label> engineLabels) {
        List<SLabel> labels = new ArrayList<>();
        if (engineLabels == null) return labels;

        int position = 1;
        for (core.logic.label.Label engineLabel : engineLabels) {
            labels.add(new SLabel(engineLabel.getRepresentation(), position++, "label"));
        }

        return labels;
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

    private static void updateProgramVariables(Program program,
                                               List<core.logic.variable.Variable> orderedVars,
                                               List<Long> orderedValues) {
        program.getVariables().clear();

        for (int i = 0; i < orderedVars.size(); i++) {
            core.logic.variable.Variable engineVar = orderedVars.get(i);
            long value = orderedValues.get(i);

            program.addVariable(new Variable(
                    engineVar.getRepresentation(),
                    (int) engineVar.getNumber(),
                    engineVar.getType().name() + ":" + value
            ));
        }
    }

    private static String getStatisticsRepresentation(ProgramStatisticDTO stats) {
        try {
            return stats.getRepresentation();
        } catch (Exception e) {
            return "";
        }
    }

    private static String legacyFormatInstruction(int lineNumber, SInstruction instruction) {
        String label = instruction.getLabel() != null ?
                instruction.getLabel().getRepresentation() : "";
        String representation = legacyInstructionRepresentation(instruction);

        return String.format("[%s] %s",
                label.isEmpty() ? "  " : label,
                representation
        );
    }

    private static String legacyInstructionRepresentation(SInstruction instruction) {
        String className = instruction.getClass().getSimpleName();
        core.logic.variable.Variable variable = instruction.getVariable();

        if (className.contains("Assignment") && variable != null) {
            return variable.getRepresentation() + " <- src";
        }
        if (className.contains("Increase") && variable != null) {
            return variable.getRepresentation() + "++";
        }
        if (className.contains("Decrease") && variable != null) {
            return variable.getRepresentation() + "--";
        }
        if (className.contains("Zero") && variable != null) {
            return variable.getRepresentation() + " <- 0";
        }
        if (className.contains("Jump")) {
            return "JUMP";
        }
        if (className.contains("Goto")) {
            return "GOTO";
        }

        return instruction.getName() != null ? instruction.getName() : instruction.toString();
    }

    private static String legacyDetermineType(SInstruction instruction) {
        String name = instruction.getClass().getSimpleName();
        return (name.contains("Jump") || name.contains("Goto") || name.contains("Label")) ? "B" : "S";
    }
}