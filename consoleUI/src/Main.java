import java.nio.file.*;
import java.util.*;

// Minimal DTO and API stubs so this single-file console compiles and runs.
enum InstructionType { BASIC, SYNTHETIC }

final class InstructionDTO {
    private final int index1Based;
    private final InstructionType type;
    private final String labelOrEmpty;
    private final String commandText;
    private final int cycles;
    private final Integer derivedFromIndex;

    InstructionDTO(int index1Based, InstructionType type, String labelOrEmpty, String commandText, int cycles, Integer derivedFromIndex) {
        this.index1Based = index1Based;
        this.type = type;
        this.labelOrEmpty = labelOrEmpty;
        this.commandText = commandText;
        this.cycles = cycles;
        this.derivedFromIndex = derivedFromIndex;
    }

    int index1Based() { return index1Based; }
    InstructionType type() { return type; }
    String labelOrEmpty() { return labelOrEmpty; }
    String commandText() { return commandText; }
    int cycles() { return cycles; }
    Integer derivedFromIndex() { return derivedFromIndex; }
}

final class ProgramDTO {
    private final String name;
    private final List<String> inputVarsOrdered;
    private final List<String> labelsOrdered;
    private final List<InstructionDTO> instructions;

    ProgramDTO(String name, List<String> inputVarsOrdered, List<String> labelsOrdered, List<InstructionDTO> instructions) {
        this.name = name;
        this.inputVarsOrdered = inputVarsOrdered;
        this.labelsOrdered = labelsOrdered;
        this.instructions = instructions;
    }

    String name() { return name; }
    List<String> inputVarsOrdered() { return inputVarsOrdered; }
    List<String> labelsOrdered() { return labelsOrdered; }
    List<InstructionDTO> instructions() { return instructions; }
}

final class LoadResultDTO {
    private final boolean valid;
    private final List<String> errors;
    private final ProgramDTO program;

    LoadResultDTO(boolean valid, List<String> errors, ProgramDTO program) {
        this.valid = valid;
        this.errors = errors;
        this.program = program;
    }

    boolean valid() { return valid; }
    List<String> errors() { return errors; }
    ProgramDTO program() { return program; }
}

final class RunResultDTO {
    private final ProgramDTO executedProgram;
    private final Map<String, Long> finalVariables;
    private final long yValue;

    RunResultDTO(ProgramDTO executedProgram, Map<String, Long> finalVariables, long yValue) {
        this.executedProgram = executedProgram;
        this.finalVariables = finalVariables;
        this.yValue = yValue;
    }

    ProgramDTO executedProgram() { return executedProgram; }
    Map<String, Long> finalVariables() { return finalVariables; }
    long yValue() { return yValue; }
}

interface EngineFacade {
    LoadResultDTO loadFromXml(Path path);
    ProgramDTO getCurrentProgram();
    int getMaxExpansionLevel();
    ProgramDTO expandProgram(int level);
    RunResultDTO runProgram(int level, List<Long> inputs);
    List<String> getRunStatistics();
}

public class Main {
    private static final Scanner SC = new Scanner(System.in);
    private static EngineFacade engine; // set via factory or DI
    private static ProgramDTO current = null; // promoted from local main variable

    public static void main(String[] args) {
        engine = EngineFactory.create(); // or new RealEngine();
        runMenuLoop();
    }

    // Extracted menu loop for clarity and testability
    private static void runMenuLoop() {
        while (true) {
            System.out.print(
                "=== VAT Console ===\n" +
                "1) Load program\n" +
                "2) Show program\n" +
                "3) Expand (by degree)\n" +
                "4) Run program\n" +
                "5) Show run statistics\n" +
                "6) Exit\n" +
                "Select: ");
            String pick = SC.nextLine().trim();
            try {
                switch (pick) {
                    case "1" -> handleLoad();
                    case "2" -> handleShow();
                    case "3" -> handleExpand();
                    case "4" -> handleRun();
                    case "5" -> handleShowStatistics();
                    case "6" -> { return; }
                    default -> System.out.println("Select 1-6.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void handleLoad() {
        System.out.print("Enter full path to XML: ");
        String p = SC.nextLine().trim();
        if (!p.toLowerCase().endsWith(".xml")) { System.out.println("Error: expected .xml"); return; }
        Path path = Paths.get(p);
        if (!Files.isRegularFile(path)) { System.out.println("Error: file not found"); return; }
        LoadResultDTO r = engine.loadFromXml(path);
        if (!r.valid()) {
            System.out.println("Invalid XML:");
            r.errors().forEach(e -> System.out.println("- " + e));
        } else {
            current = r.program();
            System.out.println("Loaded: " + current.name() + " (instructions: " + current.instructions().size() + ")");
        }
    }

    private static void handleShow() {
        if (current == null) { System.out.println("No program loaded."); return; }
        printHeader(current);
        printProgram(current);
    }

    private static void handleExpand() {
        if (current == null) { System.out.println("No program loaded."); return; }
        int max = engine.getMaxExpansionLevel();
        int lvl = readLevel(max);
        ProgramDTO exp = engine.expandProgram(lvl);
        printHeader(exp);
        printProgramWithProvenance(exp);
    }

    private static void handleRun() {
        if (current == null) { System.out.println("No program loaded."); return; }
        int max = engine.getMaxExpansionLevel();
        int lvl = readLevel(max);
        System.out.println("Enter inputs as CSV for " + String.join(",", current.inputVarsOrdered()) + " :");
        List<Long> inputs = parseCsvLongs(SC.nextLine());
        RunResultDTO run = engine.runProgram(lvl, inputs);
        printHeader(run.executedProgram());
        printProgramWithProvenance(run.executedProgram());
        System.out.println("y = " + run.yValue());
        System.out.println("Final variables:");
        run.finalVariables().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.print(e.getKey() + "=" + e.getValue() + ", "));
        System.out.println();
    }

    private static void handleShowStatistics() {
        try {
            List<String> stats = engine.getRunStatistics();
            if (stats == null || stats.isEmpty()) {
                System.out.println("No run statistics available.");
                return;
            }
            System.out.println("Run statistics:");
            for (String s : stats) System.out.println(s);
        } catch (Exception e) {
            System.out.println("Failed to obtain statistics: " + e.getMessage());
        }
    }

    static int readLevel(int max) {
        while (true) {
            System.out.print("Max expansion level: " + max + ". Enter level: ");
            String s = SC.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= 0 && v <= max) return v;
            } catch (NumberFormatException ignore) {}
            System.out.println("Enter integer in range 0.." + max + ".");
        }
    }

    static List<Long> parseCsvLongs(String line) {
        if (line == null || line.isBlank()) return List.of();
        String[] parts = line.split(",");
        List<Long> out = new ArrayList<>();
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            try { out.add(Long.parseLong(part)); } catch (NumberFormatException ignore) {}
        }
        return out;
    }

    static void printHeader(ProgramDTO p) {
        System.out.println("Program: " + p.name());
        System.out.println("Inputs: " + String.join(", ", p.inputVarsOrdered()));
        System.out.println("Labels: " + String.join(", ", p.labelsOrdered()));
    }

    static void printProgram(ProgramDTO p) {
        for (InstructionDTO i : p.instructions()) System.out.println(fmtInstr(i));
    }

    static void printProgramWithProvenance(ProgramDTO p) {
        // Build index → instruction map
        Map<Integer, InstructionDTO> byIdx = new HashMap<>();
        for (InstructionDTO i : p.instructions()) byIdx.put(i.index1Based(), i);

        for (InstructionDTO i : p.instructions()) {
            StringBuilder sb = new StringBuilder(fmtInstr(i));
            Integer from = i.derivedFromIndex();
            while (from != null) {
                InstructionDTO parent = byIdx.get(from);
                if (parent == null) break;
                sb.append(" >>> ").append(fmtInstr(parent));
                from = parent.derivedFromIndex();
            }
            System.out.println(sb);
        }
    }

    static String fmtInstr(InstructionDTO i) {
        String t = i.type()==InstructionType.BASIC ? "B" : "S";
        String lbl = i.labelOrEmpty()==null ? "" : i.labelOrEmpty().trim();
        return String.format("#%d (%s) [%-5s] %s (%d)", i.index1Based(), t, lbl, i.commandText(), i.cycles());
    }
}

// Provide EngineFactory or replace with your real engine construction.
class EngineFactory {
    static EngineFacade create() {
        // Obtain core engine singleton
        core.logic.engine.Engine coreEngine = core.logic.engine.EngineImpl.getInstance();

        return new EngineFacade() {
            @Override
            public LoadResultDTO loadFromXml(Path path) {
                try {
                    // use Path-based API; this may throw checked XMLUnmarshalException
                    coreEngine.loadProgram(path);
                    DTO.PresentProgramDTO coreProg = coreEngine.presentProgram();
                    ProgramDTO prog = convertPresent(coreProg);
                    return new LoadResultDTO(true, List.of(), prog);
                } catch (exception.XMLUnmarshalException e) {
                    // validation/unmarshal errors - return messages to UI for re-prompt
                    String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                    return new LoadResultDTO(false, List.of(msg), null);
                } catch (Exception e) {
                    String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                    return new LoadResultDTO(false, List.of(msg), null);
                }
            }

            @Override
            public ProgramDTO getCurrentProgram() {
                try {
                    DTO.PresentProgramDTO coreProg = coreEngine.presentProgram();
                    return convertPresent(coreProg);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public int getMaxExpansionLevel() {
                // Core engine does not expose expansion levels directly; return 0 as default.
                return 0;
            }

            @Override
            public ProgramDTO expandProgram(int level) {
                // Expansion not exposed via core engine API here; return current program as-is.
                return getCurrentProgram();
            }

            @Override
            public RunResultDTO runProgram(int level, List<Long> inputs) {
                try {
                    // Obtain core run DTO and configure inputs/degree
                    DTO.RunProgramDTO coreRun = null;
                    try {
                        coreRun = coreEngine.createRunDTO();
                    } catch (Exception ex) {
                        // fallback
                        coreRun = coreEngine.runProgram();
                    }
                    // set degree if supported
                    try {
                        coreRun.setDegree(level);
                    } catch (Exception ignore) { /* ignore if not supported */ }
                    coreRun.setInputs(inputs);
                    long y = coreRun.runProgram();

                    ProgramDTO executed = getCurrentProgram();

                    // Build final variables map using ordered variables & values
                    Map<String, Long> finalVars = new TreeMap<>();
                    try {
                        java.util.Set<core.logic.variable.Variable> vars = coreRun.getOrderedVariablesCopy();
                        java.util.List<Long> values = coreRun.getOrderedValuesCopy();
                        List<core.logic.variable.Variable> varList = new ArrayList<>(vars);
                        // Attempt to preserve order if the set is ordered; otherwise sort by number
                        varList.sort(Comparator.comparingInt(core.logic.variable.Variable::getNumber));
                        for (int i = 0; i < Math.min(varList.size(), values.size()); i++) {
                            finalVars.put(varList.get(i).getRepresentation(), values.get(i));
                        }
                    } catch (Exception ignore) { /* best-effort mapping */ }

                    return new RunResultDTO(executed, finalVars, y);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<String> getRunStatistics() {
                try {
                    java.util.List<statistic.SingleRunStatistic> stats = coreEngine.presentProgramStats();
                    if (stats == null || stats.isEmpty()) return List.of();
                    List<String> out = new ArrayList<>();
                    for (statistic.SingleRunStatistic s : stats) {
                        out.add(String.format("run=%d degree=%d inputs=%s result=%d cycles=%d",
                                s.getRunNumber(), s.getRunDegree(), s.getInputCopy(), s.getResult(), s.getCycles()));
                    }
                    return out;
                } catch (Exception e) {
                    return List.of(e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }

            // Helpers
            private ProgramDTO convertPresent(DTO.PresentProgramDTO p) {
                if (p == null) return null;
                String name = p.getProgramName();

                List<String> inputs = new ArrayList<>();
                try {
                    java.util.Set<core.logic.variable.Variable> xs = p.getXs();
                    List<core.logic.variable.Variable> xlist = new ArrayList<>(xs);
                    xlist.sort(Comparator.comparingInt(core.logic.variable.Variable::getNumber));
                    for (core.logic.variable.Variable v : xlist) inputs.add(v.getRepresentation());
                } catch (Exception ignore) {}

                List<String> labels = new ArrayList<>();
                try {
                    java.util.Set<core.logic.label.Label> labs = p.getLabels();
                    for (core.logic.label.Label l : labs) labels.add(l.getRepresentation());
                } catch (Exception ignore) {}

                List<InstructionDTO> instrs = new ArrayList<>();
                try {
                    List<DTO.PresentInstructionDTO> coreInstrs = p.getInstructionList();
                    for (int i = 0; i < coreInstrs.size(); i++) {
                        DTO.PresentInstructionDTO ci = coreInstrs.get(i);
                        core.logic.instruction.InstructionData id = ci.getInstructionData();
                        String typeStr = id == null ? "B" : id.getInstructionType();
                        InstructionType t = "B".equals(typeStr) ? InstructionType.BASIC : InstructionType.SYNTHETIC;
                        String lbl = null;
                        try { if (ci.getLabel() != null) lbl = ci.getLabel().getRepresentation(); } catch (Exception ignore) {}
                        String cmd = id == null ? "" : id.getName();
                        int cycles = id == null ? 0 : id.getCycles();
                        instrs.add(new InstructionDTO(i + 1, t, lbl == null ? "" : lbl, cmd, cycles, null));
                    }
                } catch (Exception ignore) {}

                return new ProgramDTO(name, inputs, labels, instrs);
            }
        };
    }
}