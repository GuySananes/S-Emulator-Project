package sserver.api;

import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.ExecuteProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.IOException;
import java.util.List;

@WebServlet(name="DebugServlet", urlPatterns="/api/debug/*")
public class DebugServlet extends BaseServlet {

    static class DebugStartReq {
        String programName;
        List<Long> inputs;
    }

    static class DebugStartResp {
        boolean ok;
        String error;
        String debugSessionId;
        DebugStartResp(boolean ok, String error, String sid) {
            this.ok = ok;
            this.error = error;
            this.debugSessionId = sid;
        }
    }

    static class StepResp {
        boolean ok;
        String error;
        boolean completed;
        Integer nextIndex;
        Integer cycles;
        String changedVariable;
        Long newValue;
        Long finalResult;
        StepResp(boolean ok, String error) {
            this.ok = ok;
            this.error = error;
        }
    }

    /**
     * Get session engine context from X-Session-Id header (DebugServlet uses header auth)
     */
    private EngineRegistry.EngineContext getSessionEngineFromHeader(HttpServletRequest req) {
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null || !AppContext.sessions().isValidSession(sessionId)) {
            return null;
        }
        return AppContext.engines().getOrCreateEngine(sessionId);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            sendAuthenticationError(resp);
            return;
        }

        String username = AppContext.sessions().getUserBySession(sessionId);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleStartDebug(req, resp, username);
        } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
            String debugId = pathInfo.substring(1);
            if (debugId.contains("/")) {
                String[] parts = debugId.split("/");
                if (parts.length == 2 && "step".equals(parts[1])) {
                    handleStep(resp, parts[0]);
                } else if (parts.length == 2 && "resume".equals(parts[1])) {
                    handleResume(resp, parts[0]);
                } else {
                    sendError(resp, 404, "validation", "Invalid debug path");
                }
            } else {
                sendError(resp, 404, "validation", "Invalid debug path");
            }
        } else {
            sendError(resp, 404, "validation", "Invalid debug path");
        }
    }

    private void handleStartDebug(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        String sessionId = req.getHeader("X-Session-Id");
        
        try {
            DebugStartReq request = gson.fromJson(req.getReader(), DebugStartReq.class);

            if (request == null || request.programName == null || request.programName.trim().isEmpty()) {
                sendValidationError(resp, "program_name_required");
                return;
            }

            logger.info(String.format("Starting debug session for program: %s | SessionID: %s | User: %s",
                request.programName, sessionId, username));

            // Get session-specific engine context
            EngineRegistry.EngineContext engineContext = getSessionEngineFromHeader(req);
            if (engineContext == null) {
                sendAuthenticationError(resp);
                return;
            }

            // Use session engine to create debug execution
            ExecuteProgramDTO execDto = engineContext.engine.executeProgram();

            if (request.inputs != null && !request.inputs.isEmpty()) {
                execDto.getDebugProgramDTO().setInput(request.inputs);
            }

            String debugId = AppContext.executions().createExecution(request.programName, username, execDto);

            logger.info(String.format("Debug session created successfully: %s | SessionID: %s | User: %s | DebugID: %s",
                request.programName, sessionId, username, debugId));

            resp.getWriter().write(gson.toJson(new DebugStartResp(true, null, debugId)));


        } catch (Exception e) {
            sendExecutionError(req, resp, "Debug start failed", e.getMessage());
        }
    }

    private void handleStep(HttpServletResponse resp, String debugId) throws IOException {
        logger.info(String.format("Debug step requested | DebugID: %s", debugId));
        
        try {
            var ctx = AppContext.executions().getExecution(debugId);
            if (ctx == null) {
                sendError(resp, 404, "validation", "Debug session not found");
                return;
            }

            if (ctx.completed) {
                sendValidationError(resp, "Debug session already completed");
                return;
            }

            DebugResult result = ctx.debugDto.nextStep();

            StepResp stepResp = new StepResp(true, null);

            if (result instanceof DebugFinalResult) {
                DebugFinalResult finalResult = (DebugFinalResult) result;
                stepResp.completed = true;
                stepResp.finalResult = finalResult.getResult();
                stepResp.cycles = finalResult.getCycles();
                AppContext.executions().markCompleted(debugId, finalResult);
                logger.info(String.format("Debug session completed: result=%d, cycles=%d | DebugID: %s",
                    finalResult.getResult(), finalResult.getCycles(), debugId));
            } else {
                stepResp.completed = false;
                stepResp.nextIndex = result.getNextIndex();
                stepResp.cycles = result.getCycles();
                if (result.getChangedVariable() != null) {
                    stepResp.changedVariable = result.getChangedVariable().getVariable().getRepresentation();
                    stepResp.newValue = result.getChangedVariable().getNewValue();
                }
                logger.fine(String.format("Debug step executed: nextIndex=%d, cycles=%d | DebugID: %s",
                    result.getNextIndex(), result.getCycles(), debugId));
            }

            resp.getWriter().write(gson.toJson(stepResp));

        } catch (Exception e) {
            sendExecutionError(resp, "Debug step failed", e.getMessage());
        }
    }

    private void handleResume(HttpServletResponse resp, String debugId) throws IOException {
        logger.info(String.format("Debug resume requested | DebugID: %s", debugId));
        
        try {
            var ctx = AppContext.executions().getExecution(debugId);
            if (ctx == null) {
                sendError(resp, 404, "validation", "Debug session not found");
                return;
            }

            if (ctx.completed) {
                sendValidationError(resp, "Debug session already completed");
                return;
            }

            DebugFinalResult finalResult = ctx.debugDto.runUntilEnd();

            StepResp stepResp = new StepResp(true, null);
            stepResp.completed = true;
            stepResp.finalResult = finalResult.getResult();
            stepResp.cycles = finalResult.getCycles();

            AppContext.executions().markCompleted(debugId, finalResult);

            logger.info(String.format("Debug session resumed to completion: result=%d, cycles=%d | DebugID: %s",
                finalResult.getResult(), finalResult.getCycles(), debugId));

            resp.getWriter().write(gson.toJson(stepResp));

        } catch (Exception e) {
            sendExecutionError(resp, "Debug resume failed", e.getMessage());
        }
    }
}