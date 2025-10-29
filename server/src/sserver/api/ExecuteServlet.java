package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.ExecuteProgramDTO;
import run.DebugProgramDTO;
import run.RunProgramDTO;
import core.logic.execution.DebugResult;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.ResultCycle;
import core.logic.execution.ChangedVariable;
import core.logic.variable.Variable;
import sserver.ctx.AppContext;
import sserver.registry.ExecutionRegistry;
import exception.RunInputException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name="ExecuteServlet", urlPatterns={
        "/api/execute/start",
        "/api/execute/step",
        "/api/execute/resume",
        "/api/execute/stop",
        "/api/execute/status",
        "/api/execute/variables"
})
public class ExecuteServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class StartExecutionRequest {
        String programId;
        String functionName;
        String mode; // "regular" or "debug"
        Map<String, Long> inputs;
    }

    static class ExecutionResponse {
        String executionId;
        String status; // "ready", "running", "paused", "completed", "error"
        Map<String, Object> data;

        ExecutionResponse(String executionId, String status) {
            this.executionId = executionId;
            this.status = status;
            this.data = new HashMap<>();
        }
    }

    static class StepRequest {
        String executionId;
    }

    static class StepResponse {
        boolean success;
        String status; // "step_completed", "execution_finished"
        int nextIndex;
        int totalCycles;
        String changedVariable;
        Long changedValue;
        Map<String, Long> variables;
        Long result; // only if finished
    }

    static class VariablesResponse {
        Map<String, Long> variables;
        int totalCycles;

        VariablesResponse(Map<String, Long> vars, int cycles) {
            this.variables = vars;
            this.totalCycles = cycles;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(Map.of("error", "unauthorized")));
            return;
        }

        String username = AppContext.sessions().getUserBySession(sessionId);
        if (username == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(Map.of("error", "invalid session")));
            return;
        }

        String path = req.getServletPath();
        System.out.println("ExecuteServlet POST: " + path);

        try {
            switch (path) {
                case "/api/execute/start":
                    handleStartExecution(req, resp, username);
                    break;
                case "/api/execute/step":
                    handleStep(req, resp, username);
                    break;
                case "/api/execute/resume":
                    handleResume(req, resp, username);
                    break;
                case "/api/execute/stop":
                    handleStop(req, resp, username);
                    break;
                default:
                    resp.setStatus(404);
                    resp.getWriter().write(gson.toJson(Map.of("error", "not found")));
            }
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(Map.of("error", "unauthorized")));
            return;
        }

        String path = req.getServletPath();
        System.out.println("ExecuteServlet GET: " + path);

        try {
            switch (path) {
                case "/api/execute/status":
                    handleGetStatus(req, resp);
                    break;
                case "/api/execute/variables":
                    handleGetVariables(req, resp);
                    break;
                default:
                    resp.setStatus(404);
                    resp.getWriter().write(gson.toJson(Map.of("error", "not found")));
            }
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    private void handleStartExecution(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        StartExecutionRequest request = gson.fromJson(body, StartExecutionRequest.class);

        if (request.programId == null || request.programId.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Program ID required")));
            return;
        }

        try {
            // Create execution DTO using Engine - this already has the program loaded
            ExecuteProgramDTO executeDTO = AppContext.programs().getEngine().executeProgram();

            // Set inputs if provided
            if (request.inputs != null && !request.inputs.isEmpty()) {
                List<Long> inputValues = new ArrayList<>(request.inputs.values());
                try {
                    executeDTO.getDebugProgramDTO().setInput(inputValues);
                    executeDTO.getRunProgramDTO().setInput(inputValues);
                } catch (RunInputException e) {
                    resp.setStatus(400);
                    resp.getWriter().write(gson.toJson(Map.of("error", "Invalid input: " + e.getMessage())));
                    return;
                }
            }

            // Create execution context
            String execId = AppContext.executions().createExecution(request.programId, username, executeDTO);

            // If regular mode, execute immediately
            if ("regular".equalsIgnoreCase(request.mode)) {
                RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
                ResultCycle result = runDTO.runProgram();

                AppContext.executions().markCompleted(execId, result);

                ExecutionResponse response = new ExecutionResponse(execId, "completed");
                response.data.put("result", result.getResult());
                response.data.put("cycles", result.getCycles());
                response.data.put("variables", getVariablesMap(runDTO));

                resp.getWriter().write(gson.toJson(response));
            } else {
                // Debug mode - return ready status
                ExecutionResponse response = new ExecutionResponse(execId, "ready");
                response.data.put("variables", getVariablesMap(executeDTO.getDebugProgramDTO()));
                response.data.put("cycles", 0);

                resp.getWriter().write(gson.toJson(response));
            }

        } catch (Exception e) {
            System.err.println("Failed to start execution: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution failed: " + e.getMessage())));
        }
    }

    private void handleStep(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        StepRequest request = gson.fromJson(body, StepRequest.class);

        ExecutionRegistry.ExecutionContext ctx = AppContext.executions().getExecution(request.executionId);
        if (ctx == null) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution not found")));
            return;
        }

        if (ctx.completed) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution already completed")));
            return;
        }

        try {
            DebugProgramDTO debugDTO = ctx.debugDto;
            DebugResult debugResult = debugDTO.nextStep();

            StepResponse response = new StepResponse();
            response.success = true;

            if (debugResult instanceof DebugFinalResult) {
                // Execution finished
                DebugFinalResult finalResult = (DebugFinalResult) debugResult;
                AppContext.executions().markCompleted(request.executionId, finalResult);

                response.status = "execution_finished";
                response.result = finalResult.getResult();
                response.totalCycles = finalResult.getCycles();
                response.variables = getVariablesMap(debugDTO);
            } else {
                // Step completed
                response.status = "step_completed";
                response.nextIndex = debugResult.getNextIndex();
                response.totalCycles = debugResult.getCycles();
                response.variables = getVariablesMap(debugDTO);

                ChangedVariable changed = debugResult.getChangedVariable();
                if (changed != null) {
                    response.changedVariable = changed.getVariable().getRepresentation();
                    response.changedValue = changed.getNewValue();
                }
            }

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Step execution failed: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "Step failed: " + e.getMessage())));
        }
    }

    private void handleResume(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        StepRequest request = gson.fromJson(body, StepRequest.class);

        ExecutionRegistry.ExecutionContext ctx = AppContext.executions().getExecution(request.executionId);
        if (ctx == null) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution not found")));
            return;
        }

        if (ctx.completed) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution already completed")));
            return;
        }

        try {
            DebugProgramDTO debugDTO = ctx.debugDto;
            DebugFinalResult finalResult = debugDTO.runUntilEnd();

            AppContext.executions().markCompleted(request.executionId, finalResult);

            StepResponse response = new StepResponse();
            response.success = true;
            response.status = "execution_finished";
            response.result = finalResult.getResult();
            response.totalCycles = finalResult.getCycles();
            response.variables = getVariablesMap(debugDTO);

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Resume execution failed: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "Resume failed: " + e.getMessage())));
        }
    }

    private void handleStop(HttpServletRequest req, HttpServletResponse resp, String username) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        StepRequest request = gson.fromJson(body, StepRequest.class);

        ExecutionRegistry.ExecutionContext ctx = AppContext.executions().getExecution(request.executionId);
        if (ctx != null) {
            AppContext.executions().removeExecution(request.executionId);
        }

        resp.getWriter().write(gson.toJson(Map.of("success", true)));
    }

    private void handleGetStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String execId = req.getParameter("executionId");

        ExecutionRegistry.ExecutionContext ctx = AppContext.executions().getExecution(execId);
        if (ctx == null) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution not found")));
            return;
        }

        String status = ctx.completed ? "completed" : "running";
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("completed", ctx.completed);

        if (ctx.completed && ctx.result != null) {
            response.put("result", ctx.result);
        }

        resp.getWriter().write(gson.toJson(response));
    }

    private void handleGetVariables(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String execId = req.getParameter("executionId");

        ExecutionRegistry.ExecutionContext ctx = AppContext.executions().getExecution(execId);
        if (ctx == null) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution not found")));
            return;
        }

        DebugProgramDTO debugDTO = ctx.debugDto;
        Map<String, Long> variables = getVariablesMap(debugDTO);
        int cycles = 0; // You can track this if needed

        VariablesResponse response = new VariablesResponse(variables, cycles);
        resp.getWriter().write(gson.toJson(response));
    }

    /**
     * Converts DTO variables to a map using Variable.getRepresentation() for names
     */
    private Map<String, Long> getVariablesMap(run.AbstractExecuteProgramDTO dto) {
        Map<String, Long> result = new LinkedHashMap<>();
        Set<Variable> variables = dto.getOrderedVariables();
        List<Long> values = dto.getOrderedValues();

        int i = 0;
        for (Variable var : variables) {
            Long value = (i < values.size()) ? values.get(i) : 0L;
            // Use getRepresentation() instead of getName() - this is the correct DTO method
            result.put(var.getRepresentation(), value);
            i++;
        }

        return result;
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
}