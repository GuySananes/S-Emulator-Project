package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
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
public class FileLoadServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class FileLoadResponse {
        String loadedFilePath;
        int programsLoaded;

        FileLoadResponse(String path, int programsLoaded) {
            this.loadedFilePath = path;
            this.programsLoaded = programsLoaded;
        }
    }

    static class ErrorResponse {
        String error;

        ErrorResponse(String error) {
            this.error = error;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null) {
                resp.setStatus(401);
                resp.getWriter().write(gson.toJson(new ErrorResponse("unauthorized - no session")));
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);
            if (username == null) {
                resp.setStatus(401);
                resp.getWriter().write(gson.toJson(new ErrorResponse("unauthorized - invalid session")));
                return;
            }

            // Get uploaded file
            Part filePart = req.getPart("file");

            if (filePart == null) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new ErrorResponse("no_file_uploaded")));
                return;
            }

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            if (!fileName.toLowerCase().endsWith(".xml")) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new ErrorResponse("invalid_file_type - must be XML")));
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

                System.out.println("File loaded successfully: " + fileName);
                System.out.println("Total programs now: " + programCount);

                FileLoadResponse response = new FileLoadResponse(absolutePath, programCount);
                resp.getWriter().write(gson.toJson(response));

            } catch (Exception e) {
                System.err.println("Failed to load program: " + e.getMessage());
                e.printStackTrace();
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new ErrorResponse("program_load_failed: " + e.getMessage())));
            }

        } catch (Exception e) {
            System.err.println("File upload error: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new ErrorResponse("file_upload_failed: " + e.getMessage())));
        }
    }

    private String getSessionIdFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}