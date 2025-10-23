
package sserver.registry;

import core.logic.engine.Engine;
import load.LoadProgramDTO;
import present.program.ProgramSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgramsRegistry {
    private final Engine engine;
    private final CopyOnWriteArrayList<ProgramSummary> programs = new CopyOnWriteArrayList<>();
    private final Path uploadDir;

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

    public void seedDummy() {
        programs.add(new ProgramSummary("Demo", "system", 12, "I", 0, 0.0));
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

            ProgramSummary summary = new ProgramSummary(
                    programName,
                    owner != null ? owner : "unknown",
                    instructionCount,
                    grade,
                    0,
                    0.0
            );

            programs.removeIf(p -> p.name.equals(programName));
            programs.add(summary);

            return dto;

        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {}
        }
    }
}