package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.ctx.AppContext;
import java.io.IOException;

@WebServlet(name = "FunctionsServlet", urlPatterns = "/api/functions")
public class FunctionsServlet extends BaseServlet {

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String sessionId = req.getHeader("X-Session-Id");
            if (sessionId == null || !AppContext.sessions().isValidSession(sessionId)) {
                sendAuthenticationError(resp);
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);
            logger.info(String.format("Retrieving functions list | SessionID: %s | User: %s", sessionId, username));

            resp.getWriter().write(gson.toJson(new String[]{}));
        } catch (Exception e) {
            sendExecutionError(resp, "Failed to retrieve functions", e.getMessage());
        }
    }
}