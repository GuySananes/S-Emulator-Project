
package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import load.LoadProgramDTO;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ProgramFunctionsServlet", urlPatterns = "/api/programs/functions/*")
public class ProgramFunctionsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        if (!isAuthorized(req)) {
            sendUnauthorizedResponse(resp);
            return;
        }

        // Extract program name from URL path
        // URL format: /api/programs/functions/{programName}
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "program_name_required")));
            return;
        }

        // Remove leading slash to get program name
        String programName = pathInfo.substring(1);

        System.out.println("=== ProgramFunctionsServlet ===");
        System.out.println("Fetching functions for program: " + programName);

        try {
            // Get program data by name
            LoadProgramDTO programData = AppContext.programs().getProgramDataByName(programName);

            if (programData == null) {
                System.err.println("Program not found: " + programName);
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(Map.of("error", "program_not_found")));
                return;
            }

            // Get the context programs (S-Functions) - these are the "functions"
            List<Map<String, Object>> functions = convertContextProgramsToFunctions(programData);

            System.out.println("Found " + functions.size() + " context programs (functions)");

            Map<String, Object> response = new HashMap<>();
            response.put("functions", functions);
            response.put("programName", programName);

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Failed to get functions for program: " + programName);
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "failed_to_load_functions: " + e.getMessage())));
        }
    }

    private boolean isAuthorized(HttpServletRequest req) {
        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            return false;
        }
        String username = AppContext.sessions().getUserBySession(sessionId);
        return username != null;
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

    private void sendUnauthorizedResponse(HttpServletResponse resp) throws IOException {
        resp.setStatus(401);
        resp.getWriter().write(gson.toJson(Map.of("error", "unauthorized")));
    }

    private List<Map<String, Object>> convertContextProgramsToFunctions(LoadProgramDTO dto) {
        List<Map<String, Object>> functions = new ArrayList<>();

        // Get the context program names (S-Functions)
        if (dto.getContextProgramsNames() != null) {
            for (String functionName : dto.getContextProgramsNames()) {
                Map<String, Object> functionMap = new HashMap<>();
                functionMap.put("name", functionName);
                functions.add(functionMap);
            }
        }

        return functions;
    }
}