package sserver.api;

import com.google.gson.Gson;
import core.logic.architecture.Architecture;
import core.logic.execution.ChangedVariable;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import core.logic.execution.ResultCycle;
import core.logic.variable.Variable;
import exception.RunInputException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.DebugProgramDTO;
import run.ExecuteProgramDTO;
import run.RunProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.ExecutionRegistry;

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
        Integer currentDegree;
        String architecture; // ARCHITECTURE FIELD
    }

    static class ExecutionResponse {
        String executionId;
        String status;
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
        int creditsConsumed;
        int creditsRemaining;
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
            // ========== ARCHITECTURE VALIDATION ==========
            Architecture architecture = Architecture.fromString(request.architecture);
            if (architecture == null) {
                architecture = Architecture.IV;
            }
            System.out.println("Selected architecture: " + architecture.getName());

            // Get the execution DTO
            ExecuteProgramDTO executeDTO = AppContext.programs().getEngine().executeProgram();

            // ========== VALIDATE AGAINST FULL EXPANDED PROGRAM ==========
            present.program.PresentProgramDTO programToValidate = null;
            try {
                // Get the underlying SProgram from RunProgramDTO
                run.RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
                core.logic.program.SProgram currentProgram = runDTO.getProgram();

                // Get maximum degree and expand to it
                int maxDegree = currentProgram.getDegree();
                System.out.println("Expanding to max degree " + maxDegree + " for validation");

                // Expand to maximum degree to check ALL instructions
                core.logic.program.SProgram fullProgram = expansion.Expansion.expand(currentProgram, maxDegree);
                programToValidate = new present.program.PresentProgramDTO(fullProgram);

                System.out.println("Validating against FULL program (degree " + maxDegree + ") with " +
                        programToValidate.getInstructionList().size() + " instructions");

            } catch (Exception e) {
                System.err.println("Could not get program for validation: " + e.getMessage());
                e.printStackTrace();
            }

            // Validate architecture compatibility
            if (programToValidate != null) {
                List<String> unsupportedInstructions = architecture.getUnsupportedInstructions(programToValidate);

                if (!unsupportedInstructions.isEmpty()) {
                    // Find minimum required architecture
                    List<String> allInstructions = new ArrayList<>();

                    for (present.mostInstructions.PresentInstructionDTO instrDTO : programToValidate.getInstructionList()) {
                        if (instrDTO.getInstructionData() != null) {
                            String instrName = instrDTO.getInstructionData().name();
                            if (!allInstructions.contains(instrName)) {
                                allInstructions.add(instrName);
                            }
                        }
                    }

                    Architecture minRequired = Architecture.findMinimumRequired(allInstructions);

                    System.out.println("❌ BLOCKING EXECUTION - Architecture incompatible");
                    System.out.println("   Selected: " + architecture.getName());
                    System.out.println("   Required: " + minRequired.getName());
                    System.out.println("   Unsupported: " + unsupportedInstructions);

                    resp.setStatus(400);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "architecture_incompatible");
                    errorResponse.put("unsupportedInstructions", unsupportedInstructions);
                    errorResponse.put("requiredArchitecture", minRequired.getName());
                    errorResponse.put("selectedArchitecture", architecture.getName());
                    resp.getWriter().write(gson.toJson(errorResponse));
                    return;
                }
            }

            // ========== CREDIT CHECK WITH ARCHITECTURE COST ==========
            int estimatedCycles = estimateProgramCycles(executeDTO);
            int architectureCost = architecture.getCreditCost();
            int totalCreditsRequired = estimatedCycles + architectureCost;
            int currentCredits = AppContext.users().getCredits(username);

            System.out.println("=== ARCHITECTURE CREDIT CHECK ===");
            System.out.println("User: " + username);
            System.out.println("Current credits: " + currentCredits);
            System.out.println("Estimated cycles: " + estimatedCycles);
            System.out.println("Architecture cost: " + architectureCost);
            System.out.println("Total required: " + totalCreditsRequired);

            if (currentCredits < totalCreditsRequired) {
                System.out.println("❌ BLOCKING EXECUTION - Insufficient credits");
                resp.setStatus(403);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "insufficient_credits");
                errorResponse.put("creditsAvailable", currentCredits);
                errorResponse.put("creditsRequired", totalCreditsRequired);
                errorResponse.put("estimatedCycles", estimatedCycles);
                errorResponse.put("architectureCost", architectureCost);
                errorResponse.put("message",
                        String.format("This execution requires %d credits (%d cycles + %d architecture cost), but you only have %d",
                                totalCreditsRequired, estimatedCycles, architectureCost, currentCredits));
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
            String execId = AppContext.executions().createExecution(request.programId, username, executeDTO, architecture);
            ExecutionRegistry.ExecutionContext execCtx = AppContext.executions().getExecution(execId);

            System.out.println("✅ Architecture stored in context: " + architecture.getName());

            // Deduct architecture cost immediately
            deductCredits(username, architectureCost);
            execCtx.addCreditsConsumed(architectureCost);
            System.out.println("✅ Architecture cost deducted: " + architectureCost);

            // Check if we're executing a function
            try {
                present.program.PresentProgramDTO presentDTO = AppContext.programs().getEngine().presentProgram();
                if (presentDTO instanceof present.program.PresentFunctionDTO) {
                    present.program.PresentFunctionDTO funcDTO = (present.program.PresentFunctionDTO) presentDTO;
                    execCtx.setFunctionExecution(funcDTO.getUserName());
                } else if (request.functionName != null && !request.functionName.trim().isEmpty()) {
                    execCtx.setFunctionExecution(request.functionName);
                }
            } catch (Exception e) {
                System.err.println("Could not determine if function execution: " + e.getMessage());
            }

            // Store the run degree
            int runDegree = 0;
            if (request.currentDegree != null) {
                runDegree = request.currentDegree;
                System.out.println("✅ Using run degree from request: " + runDegree);
            } else {
                try {
                    present.program.PresentProgramDTO presentDTO = AppContext.programs().getEngine().presentProgram();
                    if (presentDTO != null) {
                        runDegree = presentDTO.getCurrentProgramDegree();
                        System.out.println("⚠️ Using run degree from PresentDTO: " + runDegree);
                    }
                } catch (Exception e) {
                    System.err.println("Could not get run degree: " + e.getMessage());
                }
            }
            execCtx.setCurrentDegree(runDegree);

            // Execute based on mode
            if ("regular".equalsIgnoreCase(request.mode)) {
                RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
                ResultCycle result = runDTO.runProgram();

                int cyclesUsed = result.getCycles();
                deductCredits(username, cyclesUsed);
                execCtx.addCreditsConsumed(cyclesUsed);
                int remainingCredits = AppContext.users().getCredits(username);

                AppContext.executions().markCompleted(execId, result);
                recordProgramExecution(execCtx.programName, execCtx.getCreditsConsumed());
                recordUserStatistic(username, execCtx.getActualExecutionName(), execCtx, result, execCtx.isMainProgram());

                ExecutionResponse response = new ExecutionResponse(execId, "completed");
                response.data.put("result", result.getResult());
                response.data.put("cycles", result.getCycles());
                response.data.put("creditsConsumed", execCtx.getCreditsConsumed());
                response.data.put("creditsRemaining", remainingCredits);
                response.data.put("variables", getVariablesMap(runDTO));

                resp.getWriter().write(gson.toJson(response));

            } else {
                // Debug mode
                int remainingCredits = AppContext.users().getCredits(username);

                ExecutionResponse response = new ExecutionResponse(execId, "ready");
                response.data.put("variables", getVariablesMap(executeDTO.getDebugProgramDTO()));
                response.data.put("cycles", 0);
                response.data.put("creditsRemaining", remainingCredits);
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

        if (ctx.completed) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("error", "Execution already completed")));
            return;
        }

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
                DebugFinalResult finalResult = (DebugFinalResult) debugResult;

                int totalCycles = finalResult.getCycles();
                int creditsUsed = totalCycles - ctx.getCreditsConsumed();
                deductCredits(username, creditsUsed);
                ctx.addCreditsConsumed(creditsUsed);

                AppContext.executions().markCompleted(request.executionId, finalResult);
                recordProgramExecution(ctx.programName, ctx.getCreditsConsumed());
                recordUserStatistic(username, ctx.getActualExecutionName(), ctx, finalResult, ctx.isMainProgram());

                response.status = "execution_finished";
                response.result = finalResult.getResult();
                response.totalCycles = finalResult.getCycles();
                response.creditsConsumed = ctx.getCreditsConsumed();
                response.creditsRemaining = AppContext.users().getCredits(username);
                response.variables = getVariablesMap(debugDTO);
            } else {
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

            int totalCycles = finalResult.getCycles();
            int creditsUsed = totalCycles - ctx.getCreditsConsumed();
            deductCredits(username, creditsUsed);
            ctx.addCreditsConsumed(creditsUsed);

            AppContext.executions().markCompleted(request.executionId, finalResult);
            recordProgramExecution(ctx.programName, ctx.getCreditsConsumed());
            recordUserStatistic(username, ctx.getActualExecutionName(), ctx, finalResult, ctx.isMainProgram());

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
        int cycles = 0;

        VariablesResponse response = new VariablesResponse(variables, cycles);
        resp.getWriter().write(gson.toJson(response));
    }

    private Map<String, Long> getVariablesMap(run.AbstractExecuteProgramDTO dto) {
        Map<String, Long> result = new LinkedHashMap<>();
        Set<Variable> variables = dto.getOrderedVariables();
        List<Long> values = dto.getOrderedValues();

        int i = 0;
        for (Variable var : variables) {
            Long value = (i < values.size()) ? values.get(i) : 0L;
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

    private void recordProgramExecution(String programName, int creditsConsumed) {
        try {
            AppContext.programs().recordExecution(programName, creditsConsumed);
            System.out.println("✅ Recorded execution for program: " + programName +
                    ", credits consumed: " + creditsConsumed);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to record program execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void deductCredits(String username, int credits) {
        if (credits <= 0) return;

        int currentCredits = AppContext.users().getCredits(username);
        int newCredits = Math.max(0, currentCredits - credits);

        AppContext.users().setCredits(username, newCredits);

        System.out.println("Credits deducted for user " + username +
                ": " + credits + " credits. Remaining: " + newCredits);
    }

    private int estimateProgramCycles(ExecuteProgramDTO executeDTO) {
        try {
            present.program.PresentProgramDTO presentDTO =
                    AppContext.programs().getEngine().presentProgram();

            if (presentDTO == null || presentDTO.getInstructionList() == null) {
                System.out.println("⚠️ PresentDTO is null or has no instructions, estimating minimum cycles");
                return 1;
            }

            int totalCycles = 0;
            int instructionCount = 0;

            for (var instr : presentDTO.getInstructionList()) {
                instructionCount++;

                if (instr.getInstructionData() != null) {
                    String cycleStr = instr.getInstructionData().getCycleRepresentation();

                    if (cycleStr != null && !cycleStr.isEmpty()) {
                        try {
                            int cycles = Integer.parseInt(cycleStr.trim());
                            totalCycles += cycles;
                            continue;
                        } catch (NumberFormatException e1) {
                            try {
                                String numericPart = cycleStr.replaceAll("[^0-9]", "");
                                if (!numericPart.isEmpty()) {
                                    totalCycles += Integer.parseInt(numericPart);
                                    continue;
                                }
                            } catch (NumberFormatException e2) {
                                // Assume 1 cycle
                            }
                        }
                    }
                }

                totalCycles += 1;
            }

            System.out.println("✅ Cycle estimation:");
            System.out.println("   - Instructions: " + instructionCount);
            System.out.println("   - Estimated cycles: " + totalCycles);

            return Math.max(1, totalCycles);

        } catch (Exception e) {
            System.err.println("❌ Failed to estimate cycles: " + e.getMessage());
            e.printStackTrace();
            return 10;
        }
    }

    private void recordUserStatistic(String username, String programName,
                                     ExecutionRegistry.ExecutionContext ctx,
                                     Object result, boolean isMainProgram) {
        try {
            statistic.StatisticManager statManager = statistic.StatisticManager.getInstance();

            statManager.incrementUserRunCount(username);
            int runNumber = statManager.getUserRunCount(username);

            long resultValue = 0;
            long cycles = 0;

            if (result instanceof core.logic.execution.ResultCycle) {
                core.logic.execution.ResultCycle rc = (core.logic.execution.ResultCycle) result;
                resultValue = rc.getResult();
                cycles = rc.getCycles();
            } else if (result instanceof core.logic.execution.DebugFinalResult) {
                core.logic.execution.DebugFinalResult dfr = (core.logic.execution.DebugFinalResult) result;
                resultValue = dfr.getResult();
                cycles = dfr.getCycles();
            }

            // ========== GET ARCHITECTURE TYPE FROM CONTEXT ==========
            String architectureType = "IV"; // Default
            if (ctx.getArchitecture() != null) {
                architectureType = ctx.getArchitecture().getName();
            }

            java.util.List<Long> inputValues = new java.util.ArrayList<>();
            if (ctx.dto != null && ctx.dto.getRunProgramDTO() != null) {
                try {
                    java.util.Set<core.logic.variable.Variable> inputVars = ctx.dto.getRunProgramDTO().getOrderedInputVariables();
                    if (inputVars != null) {
                        for (core.logic.variable.Variable var : inputVars) {
                            inputValues.add((long) var.getNumber());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not retrieve input values: " + e.getMessage());
                }
            }

            statistic.SingleRunStatistic stat = new statistic.SingleRunStatisticImpl(
                    runNumber,
                    isMainProgram,
                    programName,
                    architectureType, // PASS ARCHITECTURE TYPE HERE
                    ctx.getCurrentDegree(),
                    inputValues,
                    resultValue,
                    cycles
            );

            statManager.addUserRunStatistic(username, stat);

            System.out.println("✅ Recorded statistics for user: " + username +
                    ", run #" + runNumber + ", program: " + programName +
                    ", architecture: " + architectureType);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to record user statistic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}