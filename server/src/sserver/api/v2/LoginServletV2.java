package sserver.api.v2;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.api.dto.v2.*;
import sserver.ctx.AppBootstrapListener;
import sserver.ctx.AppContextV2;
import sserver.registry.model.UserStats;
import sserver.util.Http;
import java.io.IOException;

@WebServlet(name="LoginServletV2", urlPatterns="/api/v2/login")
public class LoginServletV2 extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppContextV2 ctx = AppBootstrapListener.get(getServletContext());

        try {
            LoginRequest request = Http.readJson(req, LoginRequest.class);

            if (request == null || request.user == null || request.user.trim().isEmpty()) {
                Http.writeJson(resp, 400, new ErrorResponse("EMPTY_USER_NAME"));
                return;
            }

            String username = request.user.trim();
            boolean added = ctx.users().addUser(username);

            if (!added) {
                Http.writeJson(resp, 409, new ErrorResponse("NAME_TAKEN"));
                return;
            }

            UserStats stats = ctx.users().get(username).orElseThrow();
            Http.writeJson(resp, 200, new LoginResponse(true, username, stats.getCredits()));

        } catch (Exception e) {
            Http.writeJson(resp, 500, new ErrorResponse("SERVER_ERROR"));
        }
    }
}