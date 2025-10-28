package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="CreditsServlet", urlPatterns="/api/credits/charge")
public class CreditsServlet extends BaseServlet {

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
            // Check authentication using BaseServlet method
            if (!checkAuthentication(req)) {
                sendAuthenticationError(resp);
                return;
            }

            String sessionId = getSessionIdFromCookie(req);
            String username = AppContext.sessions().getUserBySession(sessionId);

            ChargeRequest request = gson.fromJson(req.getReader(), ChargeRequest.class);

            if (request == null || request.amount <= 0) {
                sendValidationError(resp, "Invalid amount. Amount must be greater than 0.");
                return;
            }

            logger.info(String.format("Charging credits: %d | SessionID: %s | User: %s",
                request.amount, sessionId, username));

            AppContext.users().addCredits(username, request.amount);
            int newCredits = AppContext.users().getCredits(username);

            logger.info(String.format("Credits charged successfully: new balance=%d | SessionID: %s | User: %s",
                newCredits, sessionId, username));

            resp.getWriter().write(gson.toJson(new CreditsResponse(newCredits)));

        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to charge credits", e.getMessage());
        }
    }
}