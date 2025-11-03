package sserver.api;

import com.google.gson.Gson;
import core.logic.engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import load.LoadProgramDTO;
import present.program.PresentProgramDTO;
import run.ExecuteProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.ProgramsRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name="ProgramSelectServlet", urlPatterns="/api/execute/select-program")
public class ProgramSelectServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class SelectProgramRequest {
        String programName;
    }

    static class SelectProgramResponse {
        String programName;
        List<String> contextPrograms;
        int maxDegree;
        int currentDegree;
        List<Map<String, Object>> instructions;
        List<Map<String, Object>> variables;
        List<String> inputVariables;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            SelectProgramRequest request = gson.fromJson(req.getReader(), SelectProgramRequest.class);

            if (request == null || request.programName == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"program_name_required\"}");
                return;
            }

            // Get the program data
            ProgramsRegistry programsReg = AppContext.programs();
            String filePath = programsReg.getFilePath(request.programName);

            if (filePath == null) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"program_not_found\"}");
                return;
            }

            // Use Engine to load and present the program (just like JavaFX does)
            Engine engine = Engine.getInstance();
            LoadProgramDTO loadDTO = engine.loadProgram(filePath);

            PresentProgramDTO presentDTO = loadDTO.getPresentProgramDTO();

            // Build response
            SelectProgramResponse response = new SelectProgramResponse();
            response.programName = presentDTO.getProgramName();
            response.contextPrograms = new ArrayList<>(loadDTO.getContextProgramsNames());
            response.maxDegree = presentDTO.getOriginMaxDegree();
            response.currentDegree = presentDTO.getCurrentProgramDegree();
            response.instructions = convertInstructions(presentDTO);
            response.variables = convertVariables(presentDTO);

            // Get input variables from ExecuteProgramDTO
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            response.inputVariables = extractInputVariables(executeDTO.getRunProgramDTO());

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Failed to select program: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "failed_to_select_program: " + e.getMessage())));
        }
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

        // PresentProgramDTO.getXs() returns Set<Variable>
        // Variable has getRepresentation() for name and getNumber() for value
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