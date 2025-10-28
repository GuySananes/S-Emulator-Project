package sserver.registry;

import core.logic.engine.Engine;
import load.LoadProgramDTO;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing Engine instances per user session.
 * Each session gets its own Engine instance to maintain proper program state.
 */
public class EngineRegistry {
    private final ConcurrentHashMap<String, EngineContext> engines = new ConcurrentHashMap<>();

    public static class EngineContext {
        public final Engine engine;
        public String currentProgramName;
        public String currentProgramFilePath;
        public LoadProgramDTO loadedProgramDTO;
        public int currentDegree;
        public long lastAccessTime;

        public EngineContext() {
            this.engine = new Engine();
            this.currentDegree = 0; // Default degree
            this.lastAccessTime = System.currentTimeMillis();
        }

        public void updateAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        /**
         * Check if a program is currently loaded in this context
         */
        public boolean isProgramLoaded() {
            return loadedProgramDTO != null;
        }

        /**
         * Get the current degree of the loaded program
         */
        public int getCurrentDegree() {
            return currentDegree;
        }

        /**
         * Update the current degree (used by expand/collapse operations)
         */
        public void setCurrentDegree(int degree) {
            this.currentDegree = degree;
        }

        /**
         * Set the loaded program and reset degree to default
         */
        public void setLoadedProgram(LoadProgramDTO programDTO) {
            this.loadedProgramDTO = programDTO;
            this.currentDegree = programDTO != null ? programDTO.getPresentProgramDTO().getCurrentProgramDegree() : 0;
        }
    }

    /**
     * Get or create an Engine context for the given session
     */
    public EngineContext getOrCreateEngine(String sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }

        EngineContext context = engines.get(sessionId);
        if (context == null) {
            context = new EngineContext();
            engines.put(sessionId, context);
        }
        context.updateAccess();
        return context;
    }

    /**
     * Get an existing Engine context for the session, or null if none exists
     */
    public EngineContext getEngine(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        EngineContext context = engines.get(sessionId);
        if (context != null) {
            context.updateAccess();
        }
        return context;
    }

    /**
     * Remove the Engine context for a session (e.g., on logout)
     */
    public void removeEngine(String sessionId) {
        if (sessionId != null) {
            engines.remove(sessionId);
        }
    }

    /**
     * Clean up old Engine contexts that haven't been accessed in a while
     */
    public void cleanupStaleEngines(long maxIdleTimeMs) {
        long currentTime = System.currentTimeMillis();
        engines.entrySet().removeIf(entry ->
            (currentTime - entry.getValue().lastAccessTime) > maxIdleTimeMs
        );
    }

    /**
     * Get the number of active Engine contexts
     */
    public int getActiveEngineCount() {
        return engines.size();
    }
}
