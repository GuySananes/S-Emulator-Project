package sserver.registry;

import core.logic.engine.Engine;
import load.LoadProgramDTO;
import present.program.FunctionSummary;
import present.program.ProgramSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgramsRegistry {
    private final Engine engine;
    private final CopyOnWriteArrayList<ProgramSummary> programs = new CopyOnWriteArrayList<>();
    private final Map<String, LoadProgramDTO> programData = new ConcurrentHashMap<>();
    private final Path uploadDir;
    private final Map<String, String> programFilePaths = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<FunctionSummary> functions = new CopyOnWriteArrayList<>();
    private final Map<String, String> functionToParentProgram = new ConcurrentHashMap<>();

    // New: Track execution statistics
    private final Map<String, ProgramStats> programStats = new ConcurrentHashMap<>();

    // Inner class to track program statistics
    private static class ProgramStats {
        int executionCount = 0;
        long totalCreditsConsumed = 0;

        synchronized void recordExecution(int creditsConsumed) {
            executionCount++;
            totalCreditsConsumed += creditsConsumed;
        }

        synchronized double getAvgCreditCost() {
            return executionCount > 0 ? (double) totalCreditsConsumed / executionCount : 0.0;
        }

        synchronized int getExecutionCount() {
            return executionCount;
        }
    }

    // ======= METHODS MOVED HERE (OUTSIDE THE INNER CLASS) =======

    /**
     * Returns list of all functions across all programs
     */
    public List<FunctionSummary> listAllFunctions() {
        return functions;
    }

    /**
     * Extract and register all functions from a loaded program
     */
    private void extractAndRegisterFunctions(LoadProgramDTO dto, String programName, String owner) {
        // Remove old functions from this program (in case of re-upload)
        functions.removeIf(f -> f.getParentProgramName().equals(programName));
        functionToParentProgram.entrySet().removeIf(e -> e.getValue().equals(programName));

        // Get function names from context programs
        java.util.Set<String> contextPrograms = dto.getContextProgramsNames();

        if (contextPrograms == null || contextPrograms.isEmpty()) {
            System.out.println("No functions found in program: " + programName);
            return;
        }

        System.out.println("Extracting " + contextPrograms.size() + " functions from program: " + programName);

        // Remember current program context
        String originalProgramName = programName;

        for (String funcName : contextPrograms) {
            try {
                System.out.println("  Processing function: " + funcName);

                // Switch to function context
                present.program.PresentProgramDTO funcDTO = engine.chooseContextProgram(funcName);

                // Extract function details
                int instrCount = funcDTO.getInstructionList() != null ?
                        funcDTO.getInstructionList().size() : 0;
                int maxDeg = funcDTO.getOriginMaxDegree();

                // Create FunctionSummary
                FunctionSummary funcSummary = new FunctionSummary(
                        java.util.UUID.randomUUID().toString(),
                        funcName,
                        originalProgramName,
                        owner,
                        instrCount,
                        maxDeg
                );

                // Store it
                functions.add(funcSummary);
                functionToParentProgram.put(funcName, originalProgramName);

                System.out.println("    ✓ Registered function: " + funcName +
                        " (instructions: " + instrCount + ", maxDegree: " + maxDeg + ")");

            } catch (Exception e) {
                System.err.println("    ✗ Failed to extract function " + funcName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Switch back to main program
        try {
            engine.chooseContextProgram(originalProgramName);
            System.out.println("Switched back to main program: " + originalProgramName);
        } catch (Exception e) {
            System.err.println("Warning: Could not switch back to main program: " + e.getMessage());
        }
    }


    public ProgramsRegistry(Engine engine) {
        this.engine = engine;
        try {
            this.uploadDir = Files.createTempDirectory("sprograms");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public List<ProgramSummary> list() {
        return programs;
    }

    public LoadProgramDTO getProgramData(String programId) {
        return programData.get(programId);
    }

    public String getFilePath(String programName) {
        return programFilePaths.get(programName);
    }

    public LoadProgramDTO getProgramDataByName(String programName) {
        for (ProgramSummary summary : programs) {
            if (summary.getName().equals(programName)) {
                return programData.get(summary.getId());
            }
        }
        return null;
    }

    /**
     * Records execution completion for statistics tracking
     */
    public void recordExecution(String programName, int creditsConsumed) {
        ProgramStats stats = programStats.computeIfAbsent(programName, k -> new ProgramStats());
        stats.recordExecution(creditsConsumed);

        // Update the summary with new statistics
        updateProgramSummary(programName);
    }

    /**
     * Get current execution statistics for a program
     */
    public int getExecutionCount(String programName) {
        ProgramStats stats = programStats.get(programName);
        return stats != null ? stats.getExecutionCount() : 0;
    }

    /**
     * Get average credit cost for a program
     */
    public double getAvgCreditCost(String programName) {
        ProgramStats stats = programStats.get(programName);
        return stats != null ? stats.getAvgCreditCost() : 0.0;
    }

    /**
     * Updates a program summary with current statistics
     */
    private void updateProgramSummary(String programName) {
        for (int i = 0; i < programs.size(); i++) {
            ProgramSummary oldSummary = programs.get(i);
            if (oldSummary.getName().equals(programName)) {
                ProgramStats stats = programStats.get(programName);
                int execCount = stats != null ? stats.getExecutionCount() : 0;
                double avgCost = stats != null ? stats.getAvgCreditCost() : 0.0;

                ProgramSummary newSummary = new ProgramSummary(
                        oldSummary.getId(),
                        oldSummary.getName(),
                        oldSummary.getOwner(),
                        oldSummary.getInstructionCount(),
                        oldSummary.getMaxDegree(),
                        execCount,
                        avgCost
                );

                programs.set(i, newSummary);
                break;
            }
        }
    }

    public LoadProgramDTO loadProgram(String xmlContent, String filename, String owner)
            throws Exception {

        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new Exception("XML content is empty");
        }

        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            throw new Exception("Invalid filename: must end with .xml");
        }

        Path tempFile = uploadDir.resolve(System.currentTimeMillis() + "_" + filename);
        Files.writeString(tempFile, xmlContent);

        try {
            LoadProgramDTO dto = engine.loadProgram(tempFile.toString());

            String programName = dto.getPresentProgramDTO().getProgramName();
            int instructionCount = dto.getPresentProgramDTO().getInstructionList().size();
            int maxDegree = dto.getPresentProgramDTO().getOriginMaxDegree();
            String programId = UUID.randomUUID().toString();

            // Get existing statistics if re-uploading
            int executionCount = getExecutionCount(programName);
            double avgCreditCost = getAvgCreditCost(programName);

            ProgramSummary summary = new ProgramSummary(
                    programId,
                    programName,
                    owner != null ? owner : "unknown",
                    instructionCount,
                    maxDegree,
                    executionCount,
                    avgCreditCost
            );

            // Remove old program with same name and add new one
            programs.removeIf(p -> p.getName().equals(programName));
            programs.add(summary);

            // Store the program data for execution
            programData.put(programId, dto);

            // Store the file path
            programFilePaths.put(programName, tempFile.toString());

            // NEW: Extract and register functions
            extractAndRegisterFunctions(dto, programName, owner);

            return dto;

        } catch (Exception e) {
            // Don't delete on error - keep for debugging
            throw e;
        }
    }
}