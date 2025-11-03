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
import run.ExecuteProgramDTO;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ChooseFunctionServlet", urlPatterns = "/api/execute/choose-function")
public class ChooseFunctionServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class ChooseFunctionRequest {
        String programName;
        String functionName;
    }

    static class ChooseFunctionResponse {
        String programName;
        String functionName;
        int maxDegree;
        int currentDegree;
        List<Map<String, Object>> instructions;
        List<Map<String, Object>> variables;
        List<String> inputVariables; // ADD THIS FIELD
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

            ChooseFunctionRequest request = gson.fromJson(req.getReader(), ChooseFunctionRequest.class);

            if (request == null || request.functionName == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"function_name_required\"}");
                return;
            }

            System.out.println("=== ChooseFunctionServlet ===");
            System.out.println("Selecting function: " + request.functionName);
            System.out.println("From program: " + request.programName);

            // Use Engine to switch to the context program (function)
            Engine engine = Engine.getInstance();

            try {
                PresentProgramDTO presentDTO = engine.chooseContextProgram(request.functionName);

                System.out.println("Function selected successfully: " + presentDTO.getProgramName());
                System.out.println("Instructions count: " +
                        (presentDTO.getInstructionList() != null ? presentDTO.getInstructionList().size() : 0));

                // Build response
                ChooseFunctionResponse response = new ChooseFunctionResponse();
                response.programName = request.programName;
                response.functionName = request.functionName;
                response.maxDegree = presentDTO.getOriginMaxDegree();
                response.currentDegree = presentDTO.getCurrentProgramDegree();
                response.instructions = convertInstructions(presentDTO);
                response.variables = convertVariables(presentDTO);

                // ADD THIS: Get input variables from ExecuteProgramDTO
                ExecuteProgramDTO executeDTO = engine.executeProgram();
                response.inputVariables = extractInputVariables(executeDTO.getRunProgramDTO());

                resp.getWriter().write(gson.toJson(response));

            } catch (Exception e) {
                System.err.println("Failed to choose context program: " + e.getMessage());
                e.printStackTrace();
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(Map.of(
                        "error", "failed_to_choose_function: " + e.getMessage()
                )));
            }

        } catch (Exception e) {
            System.err.println("Failed to process choose-function request: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of(
                    "error", "internal_error: " + e.getMessage()
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
            int lineNumber = 1; // Start numbering from 1
            for (var instr : dto.getInstructionList()) {
                Map<String, Object> instrMap = new HashMap<>();
                // Use lineNumber instead of getIndex() which might be -1
                instrMap.put("index", lineNumber);
                instrMap.put("representation", instr.getRepresentation());
                instrMap.put("type", instr.getInstructionData() != null ?
                        instr.getInstructionData().getInstructionType() : "B");
                instrMap.put("cycles", instr.getInstructionData() != null ?
                        instr.getInstructionData().getCycleRepresentation() : "1");
                instructions.add(instrMap);
                lineNumber++; // Increment for next instruction
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

    // ADD THIS NEW METHOD - Same as in ProgramSelectServlet
    private List<String> extractInputVariables(run.RunProgramDTO runDTO) {
        List<String> inputVars = new ArrayList<>();

        // Use getOrderedInputVariables() - this matches what JavaFX uses
        java.util.Set<core.logic.variable.Variable> requiredInputs = runDTO.getOrderedInputVariables();

        if (requiredInputs != null) {
            for (core.logic.variable.Variable variable : requiredInputs) {
                inputVars.add(variable.getRepresentation());
            }
        }

        return inputVars;
    }
}