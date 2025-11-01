package sserver.registry;

import run.DebugProgramDTO;
import run.ExecuteProgramDTO;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionRegistry {
    private final ConcurrentHashMap<String, ExecutionContext> executions = new ConcurrentHashMap<>();

    public static class ExecutionContext {
        public String programName;
        public String actualExecutionName;  // stores function name if executing function
        public boolean isMainProgram;        // true if main, false if function
        public String username;
        public ExecuteProgramDTO dto;
        public DebugProgramDTO debugDto;
        public long startTime;
        public boolean completed;
        public Object result;
        private int creditsConsumed = 0;
        private int currentDegree = 0;

        public ExecutionContext(String programName, String username, ExecuteProgramDTO dto) {
            this.programName = programName;
            this.actualExecutionName = programName;  // Default to program name
            this.isMainProgram = true;                // Default to main program
            this.username = username;
            this.dto = dto;
            this.debugDto = dto.getDebugProgramDTO();
            this.startTime = System.currentTimeMillis();
            this.completed = false;
        }


        public int getCreditsConsumed() {
            return creditsConsumed;
        }

        public void addCreditsConsumed(int amount) {
            this.creditsConsumed += amount;
        }

        public int getCurrentDegree() {
            return currentDegree;
        }

        public void setCurrentDegree(int degree) {
            this.currentDegree = degree;
        }


        public void setFunctionExecution(String functionName) {
            this.actualExecutionName = functionName;
            this.isMainProgram = false;
        }

        public String getActualExecutionName() {
            return actualExecutionName;
        }

        public boolean isMainProgram() {
            return isMainProgram;
        }
    }

    public String createExecution(String programName, String username, ExecuteProgramDTO dto) {
        String execId = UUID.randomUUID().toString();
        executions.put(execId, new ExecutionContext(programName, username, dto));
        return execId;
    }

    public ExecutionContext getExecution(String execId) {
        return executions.get(execId);
    }

    public void markCompleted(String execId, Object result) {
        ExecutionContext ctx = executions.get(execId);
        if (ctx != null) {
            ctx.completed = true;
            ctx.result = result;
        }
    }

    public void removeExecution(String execId) {
        executions.remove(execId);
    }
}