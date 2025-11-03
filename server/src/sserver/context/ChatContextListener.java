package sserver.context;

import sserver.manager.ChatManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ChatContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing ChatManager...");
        ChatManager chatManager = new ChatManager();
        sce.getServletContext().setAttribute("chatManager", chatManager);
        System.out.println("ChatManager initialized successfully!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Destroying ChatManager...");
        sce.getServletContext().removeAttribute("chatManager");
    }
}