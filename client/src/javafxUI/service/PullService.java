package javafxUI.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PullService {
    private final ApiClient apiClient = new ApiClient();
    private ScheduledExecutorService scheduler;
    private ObservableList<ApiClient.UserRow> usersTarget;
    private Long lastTimestamp = null;

    public void setUsersTarget(ObservableList<ApiClient.UserRow> target) {
        this.usersTarget = target;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // Already running
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::pullUsers, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void pullUsers() {
        try {
            ApiClient.UsersResponse response = apiClient.getUsers(lastTimestamp);

            if (response != null && response.users != null) {
                lastTimestamp = response.ts;

                Platform.runLater(() -> {
                    if (usersTarget != null) {
                        usersTarget.clear();
                        usersTarget.addAll(response.users);
                    }
                });
            }
        } catch (Exception e) {
            // Silently fail - don't spam console on polling errors
            // System.err.println("Failed to pull users: " + e.getMessage());
        }
    }
}