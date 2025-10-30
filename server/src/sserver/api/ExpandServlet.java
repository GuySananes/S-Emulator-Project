package sserver.api;

import com.google.gson.Gson;
import core.logic.engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import present.program.PresentProgramDTO;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ExpandServlet", urlPatterns = "/api/execute/expand")
public class ExpandServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class ExpandRequest {
        int degree;
    }

    static class ExpandResponse {
        boolean success;
        int currentDegree;
        int minDegree;
        int maxDegree;
        List<Map<String, Object>> instructions;
        List<Map<String, Object>> variables;
        String error;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"unauthorized\"}");
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);
            if (username == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"invalid_session\"}");
                return;
            }

            // Parse request
            ExpandRequest request = gson.fromJson(req.getReader(), ExpandRequest.class);

            if (request == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"degree_required\"}");
                return;
            }

            System.out.println("=== ExpandServlet ===");
            System.out.println("Requested degree: " + request.degree);

            // Get Engine instance
            Engine engine = Engine.getInstance();

            try {
                // Use Engine's built-in expandOrShrinkProgram method
                // This handles all the logic internally including validation
                PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(request.degree);

                System.out.println("Expansion successful!");
                System.out.println("Current degree: " + expandedProgram.getCurrentProgramDegree());
                System.out.println("Instructions count: " + expandedProgram.getInstructionList().size());

                // Get min/max degrees from the expanded program
                int minDegree = 1; // ExpandDTO.getMinDegree() returns program.getMinDegree() + 1
                int maxDegree = expandedProgram.getOriginMaxDegree();

                // Build success response
                ExpandResponse response = new ExpandResponse();
                response.success = true;
                response.currentDegree = expandedProgram.getCurrentProgramDegree();
                response.minDegree = minDegree;
                response.maxDegree = maxDegree;
                response.instructions = convertInstructions(expandedProgram);
                response.variables = convertVariables(expandedProgram);

                resp.getWriter().write(gson.toJson(response));

            } catch (exception.DegreeOutOfRangeException e) {
                System.err.println("Degree out of range: " + e.getMessage());

                // Try to get current program state for error details
                try {
                    PresentProgramDTO currentProgram = engine.presentProgram();

                    ExpandResponse errorResponse = new ExpandResponse();
                    errorResponse.success = false;
                    errorResponse.error = e.getMessage();
                    errorResponse.currentDegree = currentProgram.getCurrentProgramDegree();
                    errorResponse.minDegree = 1;
                    errorResponse.maxDegree = currentProgram.getOriginMaxDegree();

                    resp.setStatus(400);
                    resp.getWriter().write(gson.toJson(errorResponse));
                } catch (Exception ex) {
                    // Fallback if we can't get program state
                    resp.setStatus(400);
                    resp.getWriter().write(gson.toJson(Map.of(
                            "error", e.getMessage(),
                            "success", false
                    )));
                }

            } catch (exception.NoProgramException e) {
                System.err.println("No program loaded: " + e.getMessage());

                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(Map.of(
                        "error", "no_program_loaded",
                        "success", false
                )));

            } catch (Exception e) {
                System.err.println("Failed to expand program: " + e.getMessage());
                e.printStackTrace();

                resp.setStatus(500);
                resp.getWriter().write(gson.toJson(Map.of(
                        "error", "expansion_failed: " + e.getMessage(),
                        "success", false
                )));
            }

        } catch (Exception e) {
            System.err.println("Failed to process expand request: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of(
                    "error", "internal_error: " + e.getMessage(),
                    "success", false
            )));
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

    private List<Map<String, Object>> convertInstructions(PresentProgramDTO dto) {
        List<Map<String, Object>> instructions = new ArrayList<>();

        if (dto.getInstructionList() != null) {
            for (var instr : dto.getInstructionList()) {
                Map<String, Object> instrMap = new HashMap<>();
                instrMap.put("index", instr.getIndex());
                instrMap.put("representation", instr.getRepresentation());
                instrMap.put("type", instr.getInstructionData() != null ?
                        instr.getInstructionData().getInstructionType() : "B");
                instrMap.put("cycles", instr.getInstructionData() != null ?
                        instr.getInstructionData().getCycleRepresentation() : "1");
                instructions.add(instrMap);
            }
        }

        return instructions;
    }

    private List<Map<String, Object>> convertVariables(PresentProgramDTO dto) {
        List<Map<String, Object>> variables = new ArrayList<>();

        if (dto.getXs() != null) {
            for (var variable : dto.getXs()) {
                Map<String, Object> varMap = new HashMap<>();
                varMap.put("name", variable.getRepresentation());
                varMap.put("value", variable.getNumber());
                variables.add(varMap);
            }
        }

        return variables;
    }
}