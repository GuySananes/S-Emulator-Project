
package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="LoginServlet", urlPatterns="/api/login")
public class LoginServlet extends BaseServlet {
    static class LoginReq { String username; }
    static class LoginResp {
        boolean ok;
        String error;
        String username;
        int credits;

        LoginResp(boolean ok, String e, String username, int credits){
            this.ok=ok;
            this.error=e;
            this.username=username;
            this.credits=credits;
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            LoginReq in = gson.fromJson(req.getReader(), LoginReq.class);

            if (in == null || in.username == null || in.username.trim().isEmpty()) {
                sendValidationError(resp, "username_required");
                return;
            }

            String username = in.username.trim();
            logger.info(String.format("Login attempt for user: %s", username));

            if (username.length() > 50) {
                sendValidationError(resp, "username_too_long");
                return;
            }

            boolean added = AppContext.users().tryAdd(username);
            if (!added) {
                logger.warning(String.format("Login failed - username already taken: %s", username));
                sendError(resp, 409, "validation", "username_taken");
                return;
            }

            String sessionId = AppContext.sessions().createSession(username);

            // Set cookie
            Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(30 * 60); // 30 minutes
            resp.addCookie(sessionCookie);

            int credits = AppContext.users().getCredits(username);
            logger.info(String.format("Login successful: %s | SessionID: %s | Credits: %d", username, sessionId, credits));
            resp.getWriter().write(gson.toJson(new LoginResp(true, null, username, credits)));

        } catch (Exception e) {
            sendExecutionError(req, resp, "Login failed", e.getMessage());
        }
    }
}