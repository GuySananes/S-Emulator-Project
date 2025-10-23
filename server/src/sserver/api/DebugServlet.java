package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import run.ExecuteProgramDTO;
import run.DebugProgramDTO;
import core.logic.execution.DebugResult;
import core.logic.execution.DebugFinalResult;
import java.io.IOException;
import java.util.List;

@WebServlet(name="DebugServlet", urlPatterns="/api/debug/*")
public class DebugServlet extends HttpServlet {
    private final Gson gson = new Gson();

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

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new DebugStartResp(false, "unauthorized", null)));
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
                    resp.setStatus(404);
                    resp.getWriter().write(gson.toJson(new StepResp(false, "invalid_path")));
                }
            } else {
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(new StepResp(false, "invalid_path")));
            }
        } else {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(new StepResp(false, "invalid_path")));
        }
    }

    private void handleStartDebug(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        try {
            DebugStartReq request = gson.fromJson(req.getReader(), DebugStartReq.class);

            if (request == null || request.programName == null || request.programName.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new DebugStartResp(false, "program_name_required", null)));
                return;
            }

            ExecuteProgramDTO execDto = AppContext.programs().getEngine().executeProgram();

            if (request.inputs != null && !request.inputs.isEmpty()) {
                execDto.getDebugProgramDTO().setInput(request.inputs);
            }

            String debugId = AppContext.executions().createExecution(request.programName, username, execDto);

            resp.getWriter().write(gson.toJson(new DebugStartResp(true, null, debugId)));

        } catch (Exception e) {
            resp.setStatus(500);
            String msg = e.getMessage();
            resp.getWriter().write(gson.toJson(new DebugStartResp(false, msg != null ? msg : "debug_start_error", null)));
        }
    }

    private void handleStep(HttpServletResponse resp, String debugId) throws IOException {
        try {
            var ctx = AppContext.executions().getExecution(debugId);
            if (ctx == null) {
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(new StepResp(false, "session_not_found")));
                return;
            }

            if (ctx.completed) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new StepResp(false, "already_completed")));
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
            } else {
                stepResp.completed = false;
                stepResp.nextIndex = result.getNextIndex();
                stepResp.cycles = result.getCycles();
                if (result.getChangedVariable() != null) {
                    stepResp.changedVariable = result.getChangedVariable().getVariable().getRepresentation();
                    stepResp.newValue = result.getChangedVariable().getNewValue();
                }
            }

            resp.getWriter().write(gson.toJson(stepResp));

        } catch (Exception e) {
            resp.setStatus(500);
            String msg = e.getMessage();
            resp.getWriter().write(gson.toJson(new StepResp(false, msg != null ? msg : "step_error")));
        }
    }

    private void handleResume(HttpServletResponse resp, String debugId) throws IOException {
        try {
            var ctx = AppContext.executions().getExecution(debugId);
            if (ctx == null) {
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(new StepResp(false, "session_not_found")));
                return;
            }

            if (ctx.completed) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new StepResp(false, "already_completed")));
                return;
            }

            DebugFinalResult finalResult = ctx.debugDto.runUntilEnd();

            StepResp stepResp = new StepResp(true, null);
            stepResp.completed = true;
            stepResp.finalResult = finalResult.getResult();
            stepResp.cycles = finalResult.getCycles();

            AppContext.executions().markCompleted(debugId, finalResult);

            resp.getWriter().write(gson.toJson(stepResp));

        } catch (Exception e) {
            resp.setStatus(500);
            String msg = e.getMessage();
            resp.getWriter().write(gson.toJson(new StepResp(false, msg != null ? msg : "resume_error")));
        }
    }
}