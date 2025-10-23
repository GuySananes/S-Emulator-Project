package sserver.api;

import com.google.gson.Gson;
import core.logic.execution.ResultCycle;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.ExecuteProgramDTO;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.List;

@WebServlet(name="ExecuteServlet", urlPatterns="/api/execute/*")
public class ExecuteServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class ExecuteReq {
        String programName;
        List<Long> inputs;
    }

    static class ExecuteResp {
        boolean ok;
        String error;
        String executionId;
        Long result;
        Integer cycles;
        ExecuteResp(boolean ok, String error, String execId, Long result, Integer cycles) {
            this.ok = ok;
            this.error = error;
            this.executionId = execId;
            this.result = result;
            this.cycles = cycles;
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new ExecuteResp(false, "unauthorized", null, null, null)));
            return;
        }

        String username = AppContext.sessions().getUserBySession(sessionId);

        try {
            ExecuteReq request = gson.fromJson(req.getReader(), ExecuteReq.class);

            if (request == null || request.programName == null || request.programName.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new ExecuteResp(false, "program_name_required", null, null, null)));
                return;
            }

            ExecuteProgramDTO execDto = AppContext.programs().getEngine().executeProgram();

            if (request.inputs != null && !request.inputs.isEmpty()) {
                execDto.getRunProgramDTO().setInput(request.inputs);
            }

            ResultCycle result = execDto.getRunProgramDTO().runProgram();

            String execId = AppContext.executions().createExecution(request.programName, username, execDto);
            AppContext.executions().markCompleted(execId, result);

            resp.getWriter().write(gson.toJson(
                    new ExecuteResp(true, null, execId, result.getResult(), result.getCycles())
            ));

        } catch (Exception e) {
            resp.setStatus(500);
            String msg = e.getMessage();
            resp.getWriter().write(gson.toJson(
                    new ExecuteResp(false, msg != null ? msg : "execution_error", null, null, null)
            ));
        }
    }
}