package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="StatisticsServlet", urlPatterns="/api/statistics/history")
public class StatisticsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class HistoryRow {
        String time;
        String user;
        String program;
        int runs;
        int used;

        HistoryRow(String time, String user, String program, int runs, int used) {
            this.time = time;
            this.user = user;
            this.program = program;
            this.runs = runs;
            this.used = used;
        }
    }

    static class StatisticsResponse {
        List<HistoryRow> rows;

        StatisticsResponse(List<HistoryRow> rows) {
            this.rows = rows;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null || !AppContext.sessions().isValidSession(sessionId)) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"unauthorized\"}");
                return;
            }

            // TODO: Get real statistics from your data store
            // For now, return empty or mock data
            List<HistoryRow> rows = new ArrayList<>();

            // Mock data example (remove this when you have real data)
            rows.add(new HistoryRow(
                    Instant.now().toString(),
                    "testuser",
                    "TestProgram",
                    5,
                    100
            ));

            StatisticsResponse response = new StatisticsResponse(rows);
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"server_error\"}");
        }
    }

    private String getSessionIdFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}