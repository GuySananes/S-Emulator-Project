
package sserver.api.v2;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.api.dto.v2.*;
import sserver.ctx.AppBootstrapListener;
import sserver.ctx.AppContextV2;
import sserver.registry.model.UserStats;
import sserver.util.Http;
import java.io.IOException;

@WebServlet(name="CreditsServletV2", urlPatterns="/api/v2/credits/add")
public class CreditsServletV2 extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppContextV2 ctx = AppBootstrapListener.get(getServletContext());

        try {
            CreditsAddRequest request = Http.readJson(req, CreditsAddRequest.class);

            if (request == null || request.user == null || request.user.trim().isEmpty()) {
                Http.writeJson(resp, 400, new ErrorResponse("INVALID_REQUEST"));
                return;
            }

            if (request.amount <= 0) {
                Http.writeJson(resp, 400, new ErrorResponse("INVALID_AMOUNT"));
                return;
            }

            if (!ctx.users().get(request.user).isPresent()) {
                Http.writeJson(resp, 401, new ErrorResponse("UNKNOWN_USER"));
                return;
            }

            UserStats stats = ctx.users().addCredits(request.user, request.amount);
            Http.writeJson(resp, 200, new CreditsResponse(stats.getCredits(), stats.getUsed()));

        } catch (IllegalArgumentException e) {
            Http.writeJson(resp, 401, new ErrorResponse("UNKNOWN_USER"));
        } catch (Exception e) {
            Http.writeJson(resp, 500, new ErrorResponse("SERVER_ERROR"));
        }
    }
}