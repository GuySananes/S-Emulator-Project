
package sserver.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
public class ProgramFunctionsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            // Extract program name from URL path
            // URL format: /api/programs/functions/{programName}
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendValidationError(resp, "Program name is required in URL path");
                return;
            }

            // Remove leading slash to get program name
            String programName = pathInfo.substring(1);

            String sessionId = getSessionIdFromCookie(req);
            String username = AppContext.sessions().getUserBySession(sessionId);
            
            logger.info(String.format("Fetching functions for program: %s | SessionID: %s | User: %s",
                programName, sessionId, username));

            // Get program data by name
            LoadProgramDTO programData = AppContext.programs().getProgramDataByName(programName);

            if (programData == null) {
                logger.warning(String.format("Program not found: %s | SessionID: %s | User: %s",
                    programName, sessionId, username));
                sendError(resp, 404, "validation", "Program not found");
                return;
            }

            // Get the context programs (S-Functions) - these are the "functions"
            List<Map<String, Object>> functions = convertContextProgramsToFunctions(programData);

            logger.info(String.format("Found %d context programs (functions) for program: %s | SessionID: %s | User: %s",
                functions.size(), programName, sessionId, username));

            Map<String, Object> response = new HashMap<>();
            response.put("functions", functions);
            response.put("programName", programName);

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to load functions", e.getMessage());
        }
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