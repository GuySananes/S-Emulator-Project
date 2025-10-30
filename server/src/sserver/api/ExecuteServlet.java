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
        String status;
        int nextIndex;
        int totalCycles;
        String changedVariable;
        Long changedValue;
        Map<String, Long> variables;
        Long result;
        int creditsConsumed;    // ← ADD THIS LINE
        int creditsRemaining;   // ← ADD THIS LINE
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

        //program ID verification
        if (request.programId == null || request.programId.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Program ID required")));
            return;
        }

        try {
            // Create execution DTO using Engine - this already has the program loaded
            ExecuteProgramDTO executeDTO = AppContext.programs().getEngine().executeProgram();

            // ✅ ESTIMATE PROGRAM CYCLES
            int estimatedCycles = estimateProgramCycles(executeDTO);
            int currentCredits = AppContext.users().getCredits(username);

            System.out.println("=== CREDIT CHECK ===");
            System.out.println("User: " + username);
            System.out.println("Current credits: " + currentCredits);
            System.out.println("Estimated cycles: " + estimatedCycles);
            System.out.println("Has enough? " + (currentCredits >= estimatedCycles));

            // ✅ CHECK IF USER HAS ENOUGH CREDITS FOR ESTIMATED CYCLES
            if (currentCredits < estimatedCycles) {
                System.out.println("❌ BLOCKING EXECUTION - Insufficient credits");
                resp.setStatus(403);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "insufficient_credits");
                errorResponse.put("creditsAvailable", currentCredits);
                errorResponse.put("creditsRequired", estimatedCycles);
                errorResponse.put("message",
                        String.format("This program requires approximately %d credits, but you only have %d",
                                estimatedCycles, currentCredits));
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            System.out.println("✅ Credit check passed - proceeding with execution");

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

                //Deduct credits based on actual cycles used
                int cyclesUsed = result.getCycles();
                deductCredits(username, cyclesUsed);
                int remainingCredits = AppContext.users().getCredits(username);

                AppContext.executions().markCompleted(execId, result);

                ExecutionResponse response = new ExecutionResponse(execId, "completed");
                response.data.put("result", result.getResult());
                response.data.put("cycles", result.getCycles());
                response.data.put("creditsConsumed", cyclesUsed);
                response.data.put("creditsRemaining", remainingCredits);
                response.data.put("variables", getVariablesMap(runDTO));

                resp.getWriter().write(gson.toJson(response));

            } else {
                // Debug mode - return ready status
                ExecutionResponse response = new ExecutionResponse(execId, "ready");
                response.data.put("variables", getVariablesMap(executeDTO.getDebugProgramDTO()));
                response.data.put("cycles", 0);
                response.data.put("creditsRemaining", currentCredits);
                response.data.put("estimatedCycles", estimatedCycles);

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

        //check if completed
        if (ctx.completed) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution already completed")));
            return;
        }

        //Check credits before step
        int currentCredits = AppContext.users().getCredits(username);
        if (currentCredits <= 0) {
            resp.setStatus(403);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "insufficient_credits");
            errorResponse.put("message", "Ran out of credits during execution");
            errorResponse.put("creditsRemaining", 0);
            resp.getWriter().write(gson.toJson(errorResponse));
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

                int totalCycles = finalResult.getCycles();
                int creditsUsed = totalCycles - ctx.getCreditsConsumed();
                deductCredits(username, creditsUsed);
                ctx.addCreditsConsumed(creditsUsed);

                AppContext.executions().markCompleted(request.executionId, finalResult);

                response.status = "execution_finished";
                response.result = finalResult.getResult();
                response.totalCycles = finalResult.getCycles();
                response.creditsConsumed = ctx.getCreditsConsumed();
                response.creditsRemaining = AppContext.users().getCredits(username);
                response.variables = getVariablesMap(debugDTO);
            } else {
                // Deduct 1 credit per step
                deductCredits(username, 1);
                ctx.addCreditsConsumed(1);

                response.status = "step_completed";
                response.nextIndex = debugResult.getNextIndex();
                response.totalCycles = debugResult.getCycles();
                response.creditsConsumed = ctx.getCreditsConsumed();
                response.creditsRemaining = AppContext.users().getCredits(username);
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

        //Check credits before resume
        int currentCredits = AppContext.users().getCredits(username);
        if (currentCredits <= 0) {
            resp.setStatus(403);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "insufficient_credits");
            errorResponse.put("message", "Cannot resume: insufficient credits");
            errorResponse.put("creditsRemaining", 0);
            resp.getWriter().write(gson.toJson(errorResponse));
            return;
        }

        try {
            DebugProgramDTO debugDTO = ctx.debugDto;
            DebugFinalResult finalResult = debugDTO.runUntilEnd();

            //Deduct remaining credits
            int totalCycles = finalResult.getCycles();
            int creditsUsed = totalCycles - ctx.getCreditsConsumed();
            deductCredits(username, creditsUsed);
            ctx.addCreditsConsumed(creditsUsed);

            AppContext.executions().markCompleted(request.executionId, finalResult);

            StepResponse response = new StepResponse();
            response.success = true;
            response.status = "execution_finished";
            response.result = finalResult.getResult();
            response.totalCycles = finalResult.getCycles();
            response.creditsConsumed = ctx.getCreditsConsumed();
            response.creditsRemaining = AppContext.users().getCredits(username);
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

    /**
     * Check if user has at least 1 credit to start execution
     */
    private boolean hasCreditsForExecution(String username) {
        return AppContext.users().getCredits(username) > 0;
    }


    /**
     * Safely deduct credits from a user (never goes below 0)
     */
    private synchronized void deductCredits(String username, int credits) {
        if (credits <= 0) return;

        int currentCredits = AppContext.users().getCredits(username);
        int newCredits = Math.max(0, currentCredits - credits);

        AppContext.users().setCredits(username, newCredits);

        System.out.println("Credits deducted for user " + username +
                ": " + credits + " credits. Remaining: " + newCredits);
    }

    /**
     * Estimate the total cycles required for the program.
     * This returns the sum of all instruction cycles (worst-case scenario).
     */
    private int estimateProgramCycles(ExecuteProgramDTO executeDTO) {
        try {
            // Try to get presentation DTO from Engine
            present.program.PresentProgramDTO presentDTO =
                    AppContext.programs().getEngine().presentProgram();

            if (presentDTO == null || presentDTO.getInstructionList() == null) {
                System.out.println("⚠️ PresentDTO is null or has no instructions, estimating minimum cycles");
                return 1; // Minimum estimate if we can't get instructions
            }

            int totalCycles = 0;
            int instructionCount = 0;

            for (var instr : presentDTO.getInstructionList()) {
                instructionCount++;

                if (instr.getInstructionData() != null) {
                    String cycleStr = instr.getInstructionData().getCycleRepresentation();

                    // Handle different cycle representations
                    if (cycleStr != null && !cycleStr.isEmpty()) {
                        // Try to parse as integer
                        try {
                            int cycles = Integer.parseInt(cycleStr.trim());
                            totalCycles += cycles;
                            continue;
                        } catch (NumberFormatException e1) {
                            // Not a simple number, might be "n+1" or similar
                            // Extract numeric part
                            try {
                                // Remove non-numeric characters and try to parse
                                String numericPart = cycleStr.replaceAll("[^0-9]", "");
                                if (!numericPart.isEmpty()) {
                                    totalCycles += Integer.parseInt(numericPart);
                                    continue;
                                }
                            } catch (NumberFormatException e2) {
                                // If still fails, assume 1 cycle
                            }
                        }
                    }
                }

                // Default: assume 1 cycle if we couldn't parse
                totalCycles += 1;
            }

            System.out.println("✅ Cycle estimation:");
            System.out.println("   - Instructions: " + instructionCount);
            System.out.println("   - Estimated cycles: " + totalCycles);

            // Safety: ensure we return at least 1 if program has instructions
            return Math.max(1, totalCycles);

        } catch (Exception e) {
            System.err.println("❌ Failed to estimate cycles: " + e.getMessage());
            e.printStackTrace();
            // Return a conservative estimate if estimation fails
            return 10; // Assume at least 10 cycles if we can't estimate
        }
    }
}