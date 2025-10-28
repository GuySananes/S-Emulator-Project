package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="SessionServlet", urlPatterns="/api/session/me")
public class SessionServlet extends BaseServlet {

    static class SessionResponse {
        String username;
        int credits;

        SessionResponse(String username, int credits) {
            this.username = username;
            this.credits = credits;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            String sessionId = getSessionIdFromCookie(req);
            String username = AppContext.sessions().getUserBySession(sessionId);
            int credits = AppContext.users().getCredits(username);

            logger.info(String.format("Session info retrieved | SessionID: %s | User: %s | Credits: %d",
                sessionId, username, credits));

            SessionResponse response = new SessionResponse(username, credits);
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to retrieve session information", e.getMessage());
        }
    }
}