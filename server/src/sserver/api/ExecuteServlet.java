package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import load.LoadProgramDTO;
import sserver.ctx.AppContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name="ExecuteServlet", urlPatterns="/api/execute/*")
public class ExecuteServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class ExecuteReq {
        String programName;
        List<Long> inputs;
    }

    static class ExecuteResp {
        boolean ok;
        String error;
        String executionId;
        Long result;
        Integer cycles;
        ExecuteResp(boolean ok, String error, String execId, Long result, Integer cycles) {
            this.ok = ok;
            this.error = error;
            this.executionId = execId;
            this.result = result;
            this.cycles = cycles;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(Map.of("error", "unauthorized")));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/start")) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(Map.of("error", "not found")));
            return;
        }

        try {
            BufferedReader reader = req.getReader();
            String body = reader.lines().collect(Collectors.joining());
            @SuppressWarnings("unchecked")
            Map<String, Object> request = gson.fromJson(body, Map.class);

            String programId = (String) request.get("programId");
            String mode = (String) request.get("mode");

            if (programId == null || programId.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(Map.of("error", "Program ID required")));
                return;
            }

            // Get program data from registry
            LoadProgramDTO programData = AppContext.programs().getProgramData(programId);
            if (programData == null) {
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(Map.of("error", "Program not found")));
                return;
            }

            // Return program data for execution
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("programData", programData);
            response.put("mode", mode != null ? mode : "normal");

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", e.getMessage())));
        }
    }
}