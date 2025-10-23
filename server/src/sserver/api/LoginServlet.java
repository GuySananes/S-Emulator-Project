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
        String sessionId;
        LoginResp(boolean ok, String e, String sid){
            this.ok=ok;
            this.error=e;
            this.sessionId=sid;
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            LoginReq in = gson.fromJson(req.getReader(), LoginReq.class);

            if (in == null || in.username == null || in.username.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_required", null)));
                return;
            }

            String username = in.username.trim();

            if (username.length() > 50) {
                resp.setStatus(400);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_too_long", null)));
                return;
            }

            boolean added = AppContext.users().tryAdd(username);
            if (!added) {
                resp.setStatus(409);
                resp.getWriter().write(gson.toJson(new LoginResp(false, "username_taken", null)));
                return;
            }

            String sessionId = AppContext.sessions().createSession(username);
            resp.getWriter().write(gson.toJson(new LoginResp(true, null, sessionId)));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new LoginResp(false, "server_error", null)));
        }
    }
}