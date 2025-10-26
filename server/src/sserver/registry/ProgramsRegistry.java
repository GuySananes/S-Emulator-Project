package sserver.registry;

import core.logic.engine.Engine;
import load.LoadProgramDTO;
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

    public void seedDummy() {
        String id = UUID.randomUUID().toString();
        ProgramSummary dummy = new ProgramSummary(id, "Demo", "system", 12, "I", 0, 0.0);
        programs.add(dummy);
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
            String grade = "I";
            String programId = UUID.randomUUID().toString();

            ProgramSummary summary = new ProgramSummary(
                    programId,
                    programName,
                    owner != null ? owner : "unknown",
                    instructionCount,
                    grade,
                    0,
                    0.0
            );

            // Remove old program with same name and add new one
            programs.removeIf(p -> p.getName().equals(programName));
            programs.add(summary);

            // Store the program data for execution
            programData.put(programId, dto);

            // Store the file path
            programFilePaths.put(programName, tempFile.toString());

            return dto;

        } catch (Exception e) {
            // Don't delete on error - keep for debugging
            throw e;
        }
    }
}