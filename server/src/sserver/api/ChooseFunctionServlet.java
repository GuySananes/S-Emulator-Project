package sserver.api;

import core.logic.engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import present.program.PresentProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ChooseFunctionServlet", urlPatterns = "/api/execution/choose-function")
public class ChooseFunctionServlet extends BaseServlet {

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
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            // Get session engine context
            EngineRegistry.EngineContext engineContext = getSessionEngine(req);
            if (engineContext == null) {
                sendAuthenticationError(resp);
                return;
            }

            ChooseFunctionRequest request = gson.fromJson(req.getReader(), ChooseFunctionRequest.class);

            if (request == null || request.functionName == null) {
                sendValidationError(resp, "function_name_required");
                return;
            }

            String sessionId = getSessionIdFromCookie(req);
            String username = AppContext.sessions().getUserBySession(sessionId);
            
            logger.info(String.format("Selecting function: %s from program: %s | SessionID: %s | User: %s", 
                request.functionName, request.programName, sessionId, username));

            // Use session-specific Engine to switch to the context program (function)
            // This is equivalent to what JavaFX does with engine.chooseContextProgram()
            Engine engine = engineContext.engine;

            try {
                PresentProgramDTO presentDTO = engine.chooseContextProgram(request.functionName);

                logger.info(String.format("Function selected successfully: %s with %d instructions | SessionID: %s | User: %s",
                    presentDTO.getProgramName(),
                    presentDTO.getInstructionList() != null ? presentDTO.getInstructionList().size() : 0,
                    sessionId, username));

                // Build response
                ChooseFunctionResponse response = new ChooseFunctionResponse();
                response.programName = request.programName;
                response.functionName = request.functionName;
                response.maxDegree = presentDTO.getOriginMaxDegree();
                response.currentDegree = presentDTO.getCurrentProgramDegree();
                response.instructions = convertInstructions(presentDTO);
                response.variables = convertVariables(presentDTO);

                resp.getWriter().write(gson.toJson(response));

            } catch (Exception e) {
                sendExecutionError(req, resp, "Failed to choose function", e.getMessage());
            }

        } catch (Exception e) {
            sendExecutionError(req, resp, "Request processing failed", e.getMessage());
        }
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