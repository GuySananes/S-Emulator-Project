
package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import present.program.ProgramSummary;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name = "ProgramsServlet", urlPatterns = "/api/programs")
public class ProgramsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        String sessionId = getSessionIdFromCookie(req);
        String username = sessionId != null ? AppContext.sessions().getUserBySession(sessionId) : null;

        try {
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            logger.info(String.format("Retrieving program list | SessionID: %s | User: %s", sessionId, username));

            List<ProgramSummary> programList = AppContext.programs().list();
            List<Map<String, Object>> programs = convertToFrontendFormat(programList);
            Map<String, Object> response = buildSuccessResponse(programs);

            logger.info(String.format("Program list retrieved: %d programs | SessionID: %s | User: %s",
                programs.size(), sessionId, username));

            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to retrieve programs", e.getMessage());
        }
    }

    private List<Map<String, Object>> convertToFrontendFormat(List<ProgramSummary> programList) {
        return programList.stream()
                .map(this::mapProgramToFrontendFormat)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapProgramToFrontendFormat(ProgramSummary program) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", program.getId());
        map.put("name", program.getName());
        map.put("owner", program.getOwner());
        map.put("functions", program.getInstructionCount());
        map.put("grade", program.getGrade());
        map.put("executionCount", program.getExecutionCount());
        map.put("avgExecutionTime", program.getAvgExecutionTime());
        return map;
    }

    private Map<String, Object> buildSuccessResponse(List<Map<String, Object>> programs) {
        Map<String, Object> response = new HashMap<>();
        response.put("programs", programs);
        response.put("loadedFilePath", "");
        return response;
    }
}