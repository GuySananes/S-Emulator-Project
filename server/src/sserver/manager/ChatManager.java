package sserver.manager;

import sserver.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatManager {
    private final List<ChatMessage> messages;
    private static final int MAX_MESSAGES = 100;

    public ChatManager() {
        this.messages = new CopyOnWriteArrayList<>();
    }

    public synchronized void addMessage(String username, String message) {
        if (messages.size() >= MAX_MESSAGES) {
            messages.remove(0);
        }
        messages.add(new ChatMessage(username, message));
    }

    public synchronized List<ChatMessage> getMessages(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= messages.size()) {
            return messages;
        }
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }

    public synchronized List<ChatMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public synchronized int getMessageCount() {
        return messages.size();
    }
}