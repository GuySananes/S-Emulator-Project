package sserver.registry.v2;

import sserver.registry.model.UserStats;
import sserver.api.dto.v2.UserRow;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UsersRegistryV2 {
    private final ConcurrentHashMap<String, UserStats> users = new ConcurrentHashMap<>();

    public boolean addUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return users.putIfAbsent(name, new UserStats(name)) == null;
    }

    public Optional<UserStats> get(String name) {
        return Optional.ofNullable(users.get(name));
    }

    public UserStats addCredits(String name, int amount) {
        UserStats stats = users.get(name);
        if (stats == null) {
            throw new IllegalArgumentException("User not found: " + name);
        }
        stats.addCredits(amount);
        return stats;
    }

    public List<UserRow> snapshot() {
        return users.values().stream()
                .map(u -> new UserRow(
                        u.getName(),
                        u.getMainCount(),
                        u.getFuncCount(),
                        u.getCredits(),
                        u.getUsed(),
                        u.getRuns()
                ))
                .collect(Collectors.toList());
    }
}