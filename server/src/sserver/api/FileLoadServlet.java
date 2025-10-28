package sserver.api;

import com.google.gson.Gson;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import sserver.ctx.AppContext;
import sserver.api.dto.ErrorResponse;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@WebServlet(name="FileLoadServlet", urlPatterns="/api/file/load")
@MultipartConfig
public class FileLoadServlet extends BaseServlet {

    static class FileLoadResponse {
        String loadedFilePath;
        int programsLoaded;

        FileLoadResponse(String path, int programsLoaded) {
            this.loadedFilePath = path;
            this.programsLoaded = programsLoaded;
        }
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            String sessionId = getSessionIdFromCookie(req);
            String username = AppContext.sessions().getUserBySession(sessionId);

            logger.info(String.format("File upload initiated | SessionID: %s | User: %s", sessionId, username));

            // Get uploaded file
            Part filePart = req.getPart("file");

            if (filePart == null) {
                sendValidationError(resp, "No file uploaded");
                return;
            }

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            logger.info(String.format("Uploading file: %s | SessionID: %s | User: %s", fileName, sessionId, username));

            if (!fileName.toLowerCase().endsWith(".xml")) {
                sendValidationError(resp, "Invalid file type. Must be XML file.");
                return;
            }

            // Save file to temp directory
            Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "semulator-uploads");
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(System.currentTimeMillis() + "_" + fileName);

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Read the file content
            String xmlContent = Files.readString(filePath);

            // Load the program using ProgramsRegistry
            try {
                AppContext.programs().loadProgram(xmlContent, fileName, username);

                String absolutePath = filePath.toAbsolutePath().toString();
                int programCount = AppContext.programs().list().size();

                logger.info(String.format("File loaded successfully: %s (total programs: %d) | SessionID: %s | User: %s",
                    fileName, programCount, sessionId, username));

                FileLoadResponse response = new FileLoadResponse(absolutePath, programCount);
                resp.getWriter().write(gson.toJson(response));

            } catch (XMLUnmarshalException | ProgramValidationException e) {
                sendValidationError(req, resp, e.getMessage());
            } catch (Exception e) {
                sendExecutionError(req, resp, "Program load failed", e.getMessage());
            }

        } catch (Exception e) {
            sendExecutionError(req, resp, "File upload failed", e.getMessage());
        }
    }
}