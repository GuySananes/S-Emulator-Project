
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

@WebServlet(name = "ProgramsServlet", urlPatterns = "/api/programs")
public class ProgramsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            List<ProgramSummary> programsList = AppContext.programs().list();

            // Convert to frontend-friendly format
            List<Map<String, Object>> programsData = new java.util.ArrayList<>();
            for (ProgramSummary program : programsList) {
                programsData.add(mapProgramToFrontendFormat(program));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("programs", programsData);

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Failed to get programs: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "failed_to_get_programs")));
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

    /**
     * Maps ProgramSummary to frontend format with all required fields
     */
    private Map<String, Object> mapProgramToFrontendFormat(ProgramSummary program) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", program.getId());
        map.put("name", program.getName());
        map.put("owner", program.getOwner());
        map.put("instructionCount", program.getInstructionCount());
        map.put("maxDegree", program.getMaxDegree());
        map.put("executionCount", program.getExecutionCount());
        map.put("avgCreditCost", program.getAvgCreditCost());
        return map;
    }
}