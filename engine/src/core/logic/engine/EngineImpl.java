package core.logic.engine;

import DTO.PresentProgramDTO;
import DTO.RunProgramDTO;
import DTOcreate.PresentProgramDTOCreator;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import exception.NoProgramException;
import exception.XMLUnmarshalException;
import jaxb.JAXBLoader;
import statistic.SingleRunStatistic;
import statistic.StatisticManagerImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EngineImpl implements Engine {

    private static final Engine instance = new EngineImpl();

    private SProgram program = null;

    private EngineImpl() { }

    public static Engine getInstance() {
        return instance;
    }

    @Override
    public void loadProgram(String fullPath) {
        // Backwards compatible convenience method - delegate to Path variant
        try {
            loadProgram(Path.of(fullPath));
        } catch (XMLUnmarshalException e) {
            // wrap as unchecked for this method signature
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadProgram(Path xmlPath) throws XMLUnmarshalException {
        if (xmlPath == null) throw new XMLUnmarshalException("Path is null");
        String s = xmlPath.toString();
        if (!s.toLowerCase().endsWith(".xml")) {
            throw new XMLUnmarshalException("File must have .xml extension: " + s);
        }
        if (!Files.isRegularFile(xmlPath)) {
            throw new XMLUnmarshalException("File not found: " + s);
        }

        // Minimal stub: create an SProgramImpl with the filename as a program name.
        // Replace this with proper JAXB unmarshalling and conversion later.
        String name = xmlPath.getFileName() == null ? "unnamed" : xmlPath.getFileName().toString();

        JAXBLoader jaxbLoader = new JAXBLoader();
        this.program = jaxbLoader.load(s);
        // strip extension

    }


    @Override
    public PresentProgramDTO presentProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }
        return PresentProgramDTOCreator.create(program);
    }

    @Override
    public void expandProgram() {

    }

    @Override
    public void expandProgram(int degree) throws NoProgramException {
        if (program == null) throw new NoProgramException();
        // Minimal stub: no-op. Proper expansion logic should alter program or return a view.
    }

    @Override
    public RunProgramDTO createRunDTO() throws NoProgramException {
        if (program == null) throw new NoProgramException();
        return new RunProgramDTO(program);
    }

    @Override
    public RunProgramDTO runProgram() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return new RunProgramDTO(program);

    }

    @Override
    public List<SingleRunStatistic> presentProgramStats() throws NoProgramException {
        if (program == null) {
            throw new NoProgramException();
        }

        return StatisticManagerImpl.getInstance().getStatisticsForProgramCopy(program);


    }
}
