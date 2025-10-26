
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
public class ProgramsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        if (!isAuthorized(req)) {
            sendUnauthorizedResponse(resp);
            return;
        }

        List<ProgramSummary> programList = AppContext.programs().list();
        List<Map<String, Object>> programs = convertToFrontendFormat(programList);
        Map<String, Object> response = buildSuccessResponse(programs);

        resp.getWriter().write(gson.toJson(response));
    }

    private boolean isAuthorized(HttpServletRequest req) {
        // Get session ID from cookie (same way SessionServlet does it)
        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            return false;
        }

        // Check if session is valid
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