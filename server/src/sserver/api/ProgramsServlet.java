package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet(name="ProgramsServlet", urlPatterns="/api/programs")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
public class ProgramsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class UploadResp {
        boolean ok;
        String error;
        String programName;
        UploadResp(boolean ok, String error, String programName) {
            this.ok = ok;
            this.error = error;
            this.programName = programName;
        }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        resp.getWriter().write(gson.toJson(AppContext.programs().list()));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new UploadResp(false, "unauthorized", null)));
            return;
        }

        String username = AppContext.sessions().getUserBySession(sessionId);

        try {
            String contentType = req.getContentType();
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "multipart_required", null)));
                return;
            }

            Part filePart = req.getPart("file");
            if (filePart == null) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "file_missing", null)));
                return;
            }

            String filename = getFilename(filePart);
            if (filename == null || filename.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "filename_missing", null)));
                return;
            }

            if (!filename.toLowerCase().endsWith(".xml")) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "invalid_file_type", null)));
                return;
            }

            if (filePart.getSize() > 5 * 1024 * 1024) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "file_too_large", null)));
                return;
            }

            String xmlContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8))) {
                xmlContent = reader.lines().collect(Collectors.joining("\n"));
            }

            if (xmlContent.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "empty_file", null)));
                return;
            }

            load.LoadProgramDTO loadDto = AppContext.programs().loadProgram(xmlContent, filename, username);
            String programName = loadDto.getPresentProgramDTO().getProgramName();

            resp.getWriter().write(gson.toJson(new UploadResp(true, null, programName)));

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("unmarshal") || msg.contains("XML") || msg.contains("parse"))) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "xml_error: " + msg, null)));
            } else if (msg != null && msg.contains("validation")) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "validation_error: " + msg, null)));
            } else {
                resp.setStatus(500);
                resp.getWriter().write(gson.toJson(new UploadResp(false, "server_error", null)));
            }
        }
    }

    private String getFilename(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) return null;

        for (String token : contentDisposition.split(";")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("filename")) {
                return trimmed.substring(trimmed.indexOf('=') + 1)
                        .trim()
                        .replace("\"", "");
            }
        }
        return null;
    }
}