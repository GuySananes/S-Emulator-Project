
package sserver.ctx;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppBootstrapListener implements ServletContextListener {
    private ScheduledExecutorService cleanupScheduler;
    
    // Cleanup configuration
    private static final long CLEANUP_INTERVAL_MINUTES = 15; // Run cleanup every 15 minutes
    private static final long ENGINE_MAX_IDLE_TIME_MS = 60 * 60 * 1000; // 1 hour
    private static final long EXECUTION_MAX_IDLE_TIME_MS = 24 * 60 * 60 * 1000; // 24 hours

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // AppContext is a static utility class, no instantiation needed
        // Just initialize any registries if needed
        System.out.println("AppContext initialized (static registries ready)");
        
        // Start periodic cleanup task
        startPeriodicCleanup();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application context destroyed");
        
        // Shutdown cleanup scheduler
        if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Start a periodic cleanup task to remove stale engine contexts and executions
     */
    private void startPeriodicCleanup() {
        cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SessionCleanupThread");
            t.setDaemon(true);
            return t;
        });
        
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running periodic cleanup of stale sessions and executions...");
                
                // Clean up stale engine contexts
                int enginesBefore = AppContext.engines().getActiveEngineCount();
                AppContext.engines().cleanupStaleEngines(ENGINE_MAX_IDLE_TIME_MS);
                int enginesAfter = AppContext.engines().getActiveEngineCount();
                
                // Clean up stale executions
                AppContext.executions().cleanupStaleExecutions(EXECUTION_MAX_IDLE_TIME_MS);
                
                if (enginesBefore > enginesAfter) {
                    System.out.println("Cleaned up " + (enginesBefore - enginesAfter) + " stale engine contexts");
                }
            } catch (Exception e) {
                System.err.println("Error during periodic cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }, CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        
        System.out.println("Periodic cleanup scheduled to run every " + CLEANUP_INTERVAL_MINUTES + " minutes");
    }
}