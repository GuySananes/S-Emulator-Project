package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="LogoutServlet", urlPatterns="/api/logout")
public class LogoutServlet extends BaseServlet {

    static class LogoutResp {
        boolean ok;
        LogoutResp(boolean ok){ this.ok = ok; }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String sessionId = req.getHeader("X-Session-Id");
            if (sessionId != null && !sessionId.isEmpty()) {
                // Get the username before invalidating the session
                String username = AppContext.sessions().getUserBySession(sessionId);
                
                logger.info(String.format("Logout initiated | SessionID: %s | User: %s", sessionId, username));
                
                // Invalidate the session
                AppContext.sessions().invalidateSession(sessionId);
                
                // Clean up the associated engine context
                AppContext.engines().removeEngine(sessionId);
                
                // Clean up any associated execution contexts for this user
                if (username != null) {
                    AppContext.executions().removeExecutionsByUser(username);
                }
                
                logger.info(String.format("Logout completed successfully | SessionID: %s | User: %s", sessionId, username));
            } else {
                logger.warning("Logout attempted without valid session ID");
            }

            resp.getWriter().write(gson.toJson(new LogoutResp(true)));
        } catch (Exception e) {
            sendExecutionError(req, resp, "Logout failed", e.getMessage());
        }
    }
}