
package sserver.api;

import core.logic.execution.ResultCycle;
import exception.NoProgramException;
import exception.RunInputException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.ExecuteProgramDTO;
import run.RunProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name="ExecuteServlet", urlPatterns="/api/execution/regular")
public class ExecuteServlet extends BaseServlet {

    static class ExecuteRequest {
        List<Long> inputs;
    }

    static class ExecuteResponse {
        boolean success;
        Long result;
        Integer cycles;

        ExecuteResponse(boolean success, Long result, Integer cycles) {
            this.success = success;
            this.result = result;
            this.cycles = cycles;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        // Check authentication using BaseServlet method
        if (!checkAuthentication(req)) {
            sendAuthenticationError(resp);
            return;
        }

        // Get session engine
        EngineRegistry.EngineContext engineCtx = getSessionEngine(req);
        if (engineCtx == null) {
            sendAuthenticationError(resp);
            return;
        }

        String sessionId = getSessionIdFromCookie(req);
        String username = AppContext.sessions().getUserBySession(sessionId);
        logger.info(String.format("Starting program execution | SessionID: %s | User: %s", sessionId, username));

        try {
            // Parse request body
            BufferedReader reader = req.getReader();
            String body = reader.lines().collect(Collectors.joining());
            ExecuteRequest request = gson.fromJson(body, ExecuteRequest.class);

            if (request == null) {
                sendValidationError(resp, "Request body required");
                return;
            }

            // Get ExecuteProgramDTO from session engine
            ExecuteProgramDTO executeProgramDTO;
            try {
                executeProgramDTO = engineCtx.engine.executeProgram();
            } catch (NoProgramException e) {
                sendValidationError(resp, "No program loaded. Please select a program first.");
                return;
            }

            // Get RunProgramDTO and set input values
            RunProgramDTO runProgramDTO = executeProgramDTO.getRunProgramDTO();

            // Set input values if provided
            if (request.inputs != null) {
                try {
                    runProgramDTO.setInput(request.inputs);
                } catch (RunInputException e) {
                    sendValidationError(resp, "Invalid input values: " + e.getMessage());
                    return;
                }
            }

            // Execute the program
            ResultCycle result = runProgramDTO.runProgram();

            // Return result and cycle count
            ExecuteResponse response = new ExecuteResponse(true, result.getResult(), result.getCycles());
            logger.info(String.format("Program execution completed successfully: result=%d, cycles=%d | SessionID: %s | User: %s",
                result.getResult(), result.getCycles(), sessionId, username));
            resp.getWriter().write(gson.toJson(response));


        
        } catch (Exception e) {
            // All other errors are execution errors
            sendExecutionError(req, resp, "Program execution failed", e.getMessage());
        }
    }
}