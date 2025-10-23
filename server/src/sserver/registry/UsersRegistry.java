package sserver.registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UsersRegistry {
    private final ConcurrentHashMap<String, Boolean> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> credits = new ConcurrentHashMap<>();

    public boolean tryAdd(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        boolean added = users.putIfAbsent(username, true) == null;
        if (added) {
            credits.putIfAbsent(username, 0); // Initialize with 0 credits
        }
        return added;
    }

    public Set<String> list() {
        return users.keySet();
    }

    public int getCredits(String username) {
        return credits.getOrDefault(username, 0);
    }

    public void addCredits(String username, int amount) {
        credits.merge(username, amount, Integer::sum);
    }

    public void setCredits(String username, int amount) {
        credits.put(username, amount);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }
}