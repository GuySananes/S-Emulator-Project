
package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.ctx.AppContext;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "UsersServlet", urlPatterns = "/api/users/live")
public class UsersServlet extends BaseServlet {

    static class UsersResponse {
        long ts;
        java.util.List<UserRow> users;

        UsersResponse(long ts, java.util.List<UserRow> users) {
            this.ts = ts;
            this.users = users;
        }
    }

    static class UserRow {
        String name;
        int mainCount;
        int funcCount;
        int credits;
        int used;
        int runs;

        UserRow(String name, int credits) {
            this.name = name;
            this.mainCount = 0;
            this.funcCount = 0;
            this.credits = credits;
            this.used = 0;
            this.runs = 0;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            logger.info("Retrieving live users list");
            
            // Optional: Add authentication if needed
            // For now, we'll return all users without authentication
            Set<String> usernames = AppContext.users().list();
            java.util.List<UserRow> userRows = new java.util.ArrayList<>();

            for (String username : usernames) {
                int credits = AppContext.users().getCredits(username);
                userRows.add(new UserRow(username, credits));
            }

            logger.info(String.format("Live users list retrieved: %d users", userRows.size()));

            UsersResponse response = new UsersResponse(System.currentTimeMillis(), userRows);
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            sendExecutionError(resp, "Failed to retrieve users", e.getMessage());
        }
    }
}