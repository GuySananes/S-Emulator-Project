package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.ctx.AppContext;
import java.io.IOException;

@WebServlet(name = "FunctionsServlet", urlPatterns = "/api/functions")
public class FunctionsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String sessionId = req.getHeader("X-Session-Id");
        if (!AppContext.sessions().isValidSession(sessionId)) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        resp.getWriter().write(gson.toJson(new String[]{}));
    }
}