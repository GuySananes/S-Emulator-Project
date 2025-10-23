package sserver.registry;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    public String createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, username);
        sessionToUser.put(username, sessionId);
        return sessionId;
    }

    public String getUserBySession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;
        return sessions.get(sessionId);
    }

    public boolean isValidSession(String sessionId) {
        return sessionId != null && sessions.containsKey(sessionId);
    }

    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            String username = sessions.remove(sessionId);
            if (username != null) {
                sessionToUser.remove(username);
            }
        }
    }

    public String getSessionByUser(String username) {
        return sessionToUser.get(username);
    }
}