package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="LogoutServlet", urlPatterns="/api/logout")
public class LogoutServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class LogoutResp {
        boolean ok;
        LogoutResp(boolean ok){ this.ok = ok; }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId != null && !sessionId.isEmpty()) {
            AppContext.sessions().invalidateSession(sessionId);
        }

        resp.getWriter().write(gson.toJson(new LogoutResp(true)));
    }
}