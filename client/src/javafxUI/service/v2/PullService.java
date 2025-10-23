package javafxUI.service.v2;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PullService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "PullService");
        t.setDaemon(true);
        return t;
    });

    private final ApiClient apiClient = new ApiClient();
    private Long usersSinceTs = null;
    private ObservableList<ApiClient.UserRow> usersTarget;
    private ScheduledFuture<?> pollingTask = null;

    public void setUsersTarget(ObservableList<ApiClient.UserRow> target) {
        this.usersTarget = target;
    }

    public void start() {
        if (pollingTask != null && !pollingTask.isCancelled()) {
            System.out.println("PullService already running, ignoring start()");
            return;
        }

        System.out.println("Starting PullService polling...");
        pollingTask = scheduler.scheduleWithFixedDelay(this::pullUsers, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        System.out.println("Stopping PullService...");
        if (pollingTask != null) {
            pollingTask.cancel(true);
        }
        scheduler.shutdown();
    }

    private void pullUsers() {
        try {
            ApiClient.UsersResponse resp = apiClient.getUsers(usersSinceTs);
            usersSinceTs = resp.ts;

            if (usersTarget != null && resp.users != null) {
                Platform.runLater(() -> {
                    usersTarget.clear();
                    usersTarget.addAll(resp.users);
                });
            }
        } catch (Exception e) {
            System.err.println("Pull error: " + e.getMessage());
        }
    }
}