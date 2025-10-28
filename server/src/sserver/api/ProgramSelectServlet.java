package sserver.api;

import com.google.gson.Gson;
import core.logic.engine.Engine;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import load.LoadProgramDTO;
import present.program.PresentProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.ProgramsRegistry;
import sserver.registry.EngineRegistry;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name="ProgramSelectServlet", urlPatterns="/api/execution/select-program")
public class ProgramSelectServlet extends BaseServlet {

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
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = getSessionIdFromCookie(req);
        String username = sessionId != null ? AppContext.sessions().getUserBySession(sessionId) : null;

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

            SelectProgramRequest request = gson.fromJson(req.getReader(), SelectProgramRequest.class);
            
            logger.info(String.format("Selecting program: %s | SessionID: %s | User: %s", 
                request != null ? request.programName : "null", sessionId, username));

            if (request == null || request.programName == null) {
                sendValidationError(resp, "program_name_required");
                return;
            }

            // Get the program data
            ProgramsRegistry programsReg = AppContext.programs();
            String filePath = programsReg.getFilePath(request.programName);

            if (filePath == null) {
                sendError(resp, 404, "validation", "program_not_found");
                return;
            }

            // Use session-specific Engine to load and present the program
            Engine engine = engineContext.engine;
            LoadProgramDTO loadDTO = engine.loadProgram(filePath);

            // Store LoadProgramDTO in EngineContext for session state management
            // This also sets the currentDegree from the loaded program
            engineContext.setLoadedProgram(loadDTO);
            engineContext.currentProgramName = request.programName;
            engineContext.currentProgramFilePath = filePath;

            PresentProgramDTO presentDTO = loadDTO.getPresentProgramDTO();

            // Build response
            SelectProgramResponse response = new SelectProgramResponse();
            response.programName = presentDTO.getProgramName();
            // Convert Set<String> to List<String>
            response.contextPrograms = new ArrayList<>(loadDTO.getContextProgramsNames());
            response.maxDegree = presentDTO.getOriginMaxDegree();
            response.currentDegree = presentDTO.getCurrentProgramDegree();
            response.instructions = convertInstructions(presentDTO);
            response.variables = convertVariables(presentDTO);

            logger.info(String.format("Program selected successfully: %s (degree: %d/%d, instructions: %d, variables: %d) | SessionID: %s | User: %s",
                response.programName, response.currentDegree, response.maxDegree, 
                response.instructions.size(), response.variables.size(), sessionId, username));

            resp.getWriter().write(gson.toJson(response));

        } catch (XMLUnmarshalException | ProgramValidationException e) {
            sendValidationError(req, resp, e.getMessage());
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to select program", e.getMessage());
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


}