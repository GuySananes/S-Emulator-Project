package javafx.service;

import javafx.model.Instruction;
import javafx.model.Program;
import javafx.model.SLabel;
import javafx.model.Variable;

// DTOs actually present per user
import present.PresentProgramDTO;
import present.PresentInstructionDTO;
import expand.ExpandDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;

// Engine legacy (to phase out later)
import core.logic.program.SProgram;
import core.logic.instruction.SInstruction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DTO + legacy converter.
 * Updated to match real DTO definitions: PresentProgramDTO exposes
 *   - programName
 *   - Set<Variable> Xs (variables)
 *   - Set<Label> labels
 *   - List<PresentInstructionDTO> instructionList
 *   - representation
 * PresentInstructionDTO exposes
 *   - instructionData (ignored here for now)
 *   - variable (optional, for context)
 *   - label
 *   - index (line number)
 *   - representation (core textual form)
 */
public class ModelConverter {

    /* ===================== DTO-BASED PATH ===================== */
    public static Program convertProgram(PresentProgramDTO dto) {
        Program ui = new Program();
        if (dto == null) return ui;
        ui.setName(dto.getProgramName());
        ui.setLoaded(true);
        // Fill instructions / variables / labels
        for (Instruction ins : convertInstructions(dto)) ui.addInstruction(ins);
        for (Variable v : convertVariables(dto)) ui.addVariable(v);
        return ui;
    }

    public static List<Instruction> convertInstructions(PresentProgramDTO dto) {
        List<Instruction> list = new ArrayList<>();
        if (dto == null || dto.getInstructionList() == null) return list;
        int fallback = 1;
        for (PresentInstructionDTO pi : dto.getInstructionList()) {
            if (pi == null) continue;
            int line = pi.getIndex() > 0 ? pi.getIndex() : fallback++;
            String display = formatDTOInstruction(pi);
            String type = deriveTypeFromRepresentation(pi.getRepresentation());
            int cycles = 1; // Not supplied in DTO; could derive from instructionData later.
            list.add(new Instruction(line, type, cycles, display));
        }
        return list;
    }

    public static List<Variable> convertVariables(PresentProgramDTO dto) {
        List<Variable> out = new ArrayList<>();
        if (dto == null) return out;
        Set<core.logic.variable.Variable> engineVars = dto.getXs();
        if (engineVars == null) return out;
        for (core.logic.variable.Variable ev : engineVars) {
            if (ev == null) continue;
            out.add(new Variable(ev.getRepresentation(), ev.getNumber(), ev.getType().name()));
        }
        return out;
    }

    public static List<SLabel> convertLabels(PresentProgramDTO dto) {
        List<SLabel> out = new ArrayList<>();
        if (dto == null) return out;
        Set<core.logic.label.Label> labels = dto.getLabels();
        if (labels == null) return out;
        int pos = 1;
        for (core.logic.label.Label l : labels) {
            if (l == null) continue;
            out.add(new SLabel(l.getRepresentation(), pos++, "label"));
        }
        return out;
    }

    /**
     * Apply execution result info from RunProgramDTO.
     * RunProgramDTO does not retain variable values internally beyond program state; we
     * rely on ordered variables + ordered values (exposed after run) if caller provides them.
     */
    public static void applyRunResult(RunProgramDTO runDTO, Program uiProgram, List<Long> orderedValues, List<core.logic.variable.Variable> orderedVars, int cycles) {
        if (runDTO == null || uiProgram == null) return;
        if (cycles >= 0) uiProgram.setTotalCycles(cycles);
        if (orderedVars != null && orderedValues != null && orderedVars.size() == orderedValues.size()) {
            uiProgram.getVariables().clear();
            for (int i = 0; i < orderedVars.size(); i++) {
                core.logic.variable.Variable ev = orderedVars.get(i);
                long val = orderedValues.get(i);
                uiProgram.addVariable(new Variable(ev.getRepresentation(), (int) ev.getNumber(), ev.getType().name() + ":" + val));
            }
        }
    }

    /**
     * Convert ProgramStatisticDTO representation to a single Statistic row.
     * ProgramStatisticDTO only exposes a list + representation; we present one aggregated row.
     */
    public static javafx.model.Statistic convertStatistics(ProgramStatisticDTO stats) {
        if (stats == null) return new javafx.model.Statistic();
        String rep;
        try { rep = stats.getRepresentation(); } catch (Exception e) { rep = ""; }
        // We lack discrete cycle / instruction counts here; store representation length heuristics.
        return new javafx.model.Statistic("Runs", 0, 0, rep); // executionTime field reused to show representation text.
    }

    /**
     * Expansion helper: returns a Program for a chosen degree via ExpandDTO.
     */
    public static Program expandToProgram(ExpandDTO expandDTO, int degree) throws Exception {
        PresentProgramDTO expanded = expandDTO.expand(degree);
        return convertProgram(expanded);
    }

    /* ===================== LEGACY ENGINE PATH (retain) ===================== */
    public static Program convertProgram(SProgram engineProgram) {
        Program uiProgram = new Program();
        if (engineProgram == null) return uiProgram;
        uiProgram.setName(engineProgram.getName());
        uiProgram.setMaxDegree(engineProgram.calculateMaxDegree());
        uiProgram.setMinDegree(engineProgram.getMinDegree());
        uiProgram.setTotalCycles(engineProgram.calculateCycles());
        uiProgram.setLoaded(true);
        for (Instruction instruction : convertInstructions(engineProgram.getInstructionList())) uiProgram.addInstruction(instruction);
        for (Variable variable : convertVariables(engineProgram.getOrderedVariables())) uiProgram.addVariable(variable);
        return uiProgram;
    }

    public static List<Instruction> convertInstructions(List<SInstruction> engineInstructions) {
        List<Instruction> uiInstructions = new ArrayList<>();
        if (engineInstructions == null) return uiInstructions;
        for (int i = 0; i < engineInstructions.size(); i++) {
            SInstruction engineInstr = engineInstructions.get(i);
            String formattedInstruction = legacyFormatInstruction(i + 1, engineInstr);
            uiInstructions.add(new Instruction(
                i + 1,
                legacyDetermineType(engineInstr),
                engineInstr.getCycles(),
                formattedInstruction
            ));
        }
        return uiInstructions;
    }

    public static List<Variable> convertVariables(Set<core.logic.variable.Variable> engineVariables) {
        List<Variable> uiVariables = new ArrayList<>();
        if (engineVariables == null) return uiVariables;
        for (core.logic.variable.Variable engineVar : engineVariables) {
            uiVariables.add(new Variable(engineVar.getRepresentation(), engineVar.getNumber(), engineVar.getType().toString()));
        }
        return uiVariables;
    }

    public static List<SLabel> convertLabels(Set<core.logic.label.Label> engineLabels) {
        List<SLabel> uiLabels = new ArrayList<>();
        if (engineLabels == null) return uiLabels;
        int position = 1;
        for (core.logic.label.Label engineLabel : engineLabels) {
            uiLabels.add(new SLabel(engineLabel.getRepresentation(), position++, "label"));
        }
        return uiLabels;
    }

    /* ===================== Private helpers (DTO) ===================== */
    private static String formatDTOInstruction(PresentInstructionDTO pi) {
        String label = pi.getLabel() == null ? "" : pi.getLabel().getRepresentation();
        String rep = pi.getRepresentation() == null ? "" : pi.getRepresentation();

        // Strip leading line number + type pattern e.g. "#1 (B) "
        rep = rep.replaceFirst("^#\\d+\\s*\\([A-Za-z]\\)\\s*", "");
        // Strip a leading label in brackets inside the representation (e.g. "[ L1 ] ") to avoid duplication
        rep = rep.replaceFirst("^\\[\\s*[^]]*\\]\\s*", "");
        // Strip trailing cycles count e.g. "(3)" at end
        rep = rep.replaceFirst("\\(\\d+\\)\\s*$", "");

        // Final trimmed core textual instruction
        String core = rep.trim();
        // Compose with external label (only once) — keep blank placeholder to preserve alignment if needed
        return "[" + (label.isBlank() ? "  " : label) + "] " + core;
    }

    private static String deriveTypeFromRepresentation(String repr) {
        if (repr == null) return "S";
        String r = repr.toLowerCase();
        if (r.contains("jump") || r.contains("goto")) return "B";
        return "S";
    }

    /* ===================== Private helpers (legacy) ===================== */
    private static String legacyFormatInstruction(int lineNumber, SInstruction instruction) {
        String labelText = instruction.getLabel() == null ? "" : instruction.getLabel().getRepresentation();
        String rep = legacyInstructionRepresentation(instruction);
        return "[" + (labelText.isEmpty() ? "  " : labelText) + "] " + rep;
    }

    private static String legacyInstructionRepresentation(SInstruction instruction) {
        String className = instruction.getClass().getSimpleName();
        if (className.contains("Assignment")) {
            core.logic.variable.Variable variable = instruction.getVariable();
            if (variable != null) return variable.getRepresentation() + " <- src"; // placeholder
        }
        if (className.contains("Increase")) {
            core.logic.variable.Variable variable = instruction.getVariable();
            if (variable != null) return variable.getRepresentation() + "++";
        }
        if (className.contains("Decrease")) {
            core.logic.variable.Variable variable = instruction.getVariable();
            if (variable != null) return variable.getRepresentation() + "--";
        }
        if (className.contains("Zero")) {
            core.logic.variable.Variable variable = instruction.getVariable();
            if (variable != null) return variable.getRepresentation() + " <- 0";
        }
        if (className.contains("Jump")) return "JUMP";
        if (className.contains("Goto")) return "GOTO";
        return instruction.getName() != null ? instruction.getName() : instruction.toString();
    }

    private static String legacyDetermineType(SInstruction instruction) {
        String name = instruction.getClass().getSimpleName();
        if (name.contains("Jump") || name.contains("Goto") || name.contains("Label")) return "B";
        return "S";
    }

    /* ===================== Reflection helpers (only where needed) ===================== */
    private static Integer reflectiveInt(Object target, String method, int def) {
        try { Method m = target.getClass().getMethod(method); Object v = m.invoke(target); return v == null ? def : ((Number) v).intValue(); } catch (Exception e) { return def; }
    }
}
