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

        FileLoadResponse(String path) {
            this.loadedFilePath = path;
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
            if (sessionId == null || !AppContext.sessions().isValidSession(sessionId)) {
                resp.setStatus(401);
                resp.getWriter().write(gson.toJson(new ErrorResponse("unauthorized")));
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

            // Save file to temp directory
            Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "semulator-uploads");
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName);

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // TODO: Process the XML file and load programs
            // For now, just return the path
            String absolutePath = filePath.toAbsolutePath().toString();

            FileLoadResponse response = new FileLoadResponse(absolutePath);
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
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