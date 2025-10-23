package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="CreditsServlet", urlPatterns="/api/credits/charge")
public class CreditsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class ChargeRequest {
        int amount;
    }

    static class CreditsResponse {
        int credits;

        CreditsResponse(int credits) {
            this.credits = credits;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String sessionId = getSessionIdFromCookie(req);

            if (sessionId == null || !AppContext.sessions().isValidSession(sessionId)) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"unauthorized\"}");
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);

            ChargeRequest request = gson.fromJson(req.getReader(), ChargeRequest.class);

            if (request == null || request.amount <= 0) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"invalid_amount\"}");
                return;
            }

            AppContext.users().addCredits(username, request.amount);
            int newCredits = AppContext.users().getCredits(username);

            resp.getWriter().write(gson.toJson(new CreditsResponse(newCredits)));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"server_error\"}");
        }
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