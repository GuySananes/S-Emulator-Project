package sserver.ctx;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {
    private static final String ATTR_KEY = "APP_CTX";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppContextV2 ctx = new AppContextV2();
        sce.getServletContext().setAttribute(ATTR_KEY, ctx);
        System.out.println("AppContextV2 initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(ATTR_KEY);
        System.out.println("AppContextV2 destroyed");
    }

    public static AppContextV2 get(jakarta.servlet.ServletContext ctx) {
        return (AppContextV2) ctx.getAttribute(ATTR_KEY);
    }
}