
package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="LoginServlet", urlPatterns="/api/login")
public class LoginServlet extends HttpServlet {
    private final Gson gson = new Gson();
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
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_required", null, 0)));
                return;
            }

            String username = in.username.trim();

            if (username.length() > 50) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_too_long", null, 0)));
                return;
            }

            boolean added = AppContext.users().tryAdd(username);
            if (!added) {
                resp.setStatus(409);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_taken", null, 0)));
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
            resp.getWriter().write(gson.toJson(new LoginResp(true, null, username, credits)));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new LoginResp(false, "server_error", null, 0)));
        }
    }
}