package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="SessionServlet", urlPatterns="/api/session/me")
public class SessionServlet extends HttpServlet {
    private final Gson gson = new Gson();

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

        String sessionId = getSessionIdFromCookie(req);

        if (sessionId == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"no_session\"}");
            return;
        }

        String username = AppContext.sessions().getUserBySession(sessionId);

        if (username == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"invalid_session\"}");
            return;
        }

        int credits = AppContext.users().getCredits(username);

        SessionResponse response = new SessionResponse(username, credits);
        resp.getWriter().write(gson.toJson(response));
    }

    private String getSessionIdFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}