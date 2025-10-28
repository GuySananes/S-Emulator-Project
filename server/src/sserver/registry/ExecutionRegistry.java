package sserver.registry;

import run.DebugProgramDTO;
import run.ExecuteProgramDTO;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionRegistry {
    private final ConcurrentHashMap<String, ExecutionContext> executions = new ConcurrentHashMap<>();

    public static class ExecutionContext {
        public String programName;
        public String username;
        public ExecuteProgramDTO dto;
        public DebugProgramDTO debugDto;
        public long startTime;
        public boolean completed;
        public Object result;

        public ExecutionContext(String programName, String username, ExecuteProgramDTO dto) {
            this.programName = programName;
            this.username = username;
            this.dto = dto;
            this.debugDto = dto.getDebugProgramDTO();
            this.startTime = System.currentTimeMillis();
            this.completed = false;
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

    /**
     * Remove all executions for a specific user (e.g., on logout)
     */
    public void removeExecutionsByUser(String username) {
        if (username != null) {
            executions.entrySet().removeIf(entry -> 
                username.equals(entry.getValue().username)
            );
        }
    }

    /**
     * Clean up old completed executions that haven't been accessed in a while
     */
    public void cleanupStaleExecutions(long maxIdleTimeMs) {
        long currentTime = System.currentTimeMillis();
        executions.entrySet().removeIf(entry -> {
            ExecutionContext ctx = entry.getValue();
            return ctx.completed && (currentTime - ctx.startTime) > maxIdleTimeMs;
        });
    }
}