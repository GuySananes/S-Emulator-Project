package sserver.api;

import com.google.gson.Gson;
import exception.ProgramNotExecutedYetException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sserver.ctx.AppContext;
import statistic.SingleRunStatisticDTO;
import statistic.UserStatisticsDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "UserStatisticsServlet", urlPatterns = "/api/user-statistics")
public class UserStatisticsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    static class UserStatisticsResponse {
        String username;
        List<StatRow> statistics;

        UserStatisticsResponse(String username, List<StatRow> statistics) {
            this.username = username;
            this.statistics = statistics;
        }
    }

    static class StatRow {
        int runNumber;
        boolean isMainProgram;
        String programName;
        String architectureType;
        int runDegree;  // ← MAKE SURE THIS IS NAMED runDegree
        long finalYValue;
        long cyclesConsumed;

        StatRow(int runNumber, boolean isMainProgram, String programName,
                String architectureType, int runDegree, long finalYValue, long cyclesConsumed) {
            this.runNumber = runNumber;
            this.isMainProgram = isMainProgram;
            this.programName = programName;
            this.architectureType = architectureType;
            this.runDegree = runDegree;  // ← MAKE SURE THIS IS ASSIGNED
            this.finalYValue = finalYValue;
            this.cyclesConsumed = cyclesConsumed;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            // Check authentication
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"unauthorized\"}");
                return;
            }

            String currentUser = AppContext.sessions().getUserBySession(sessionId);
            if (currentUser == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"invalid_session\"}");
                return;
            }

            // Get username from query parameter, default to current user
            String targetUsername = req.getParameter("username");
            if (targetUsername == null || targetUsername.trim().isEmpty()) {
                targetUsername = currentUser;
            }

            System.out.println("=== UserStatisticsServlet ===");
            System.out.println("Fetching statistics for user: " + targetUsername);

            try {
                UserStatisticsDTO userStats = new UserStatisticsDTO(targetUsername);

                List<StatRow> rows = new ArrayList<>();
                for (SingleRunStatisticDTO stat : userStats.getStatistics()) {
                    int degree = stat.getRunDegree();  // ← ADD DEBUG HERE
                    System.out.println("Creating StatRow: runNumber=" + stat.getRunNumber() +
                            ", runDegree=" + degree);  // ← ADD THIS DEBUG LINE

                    rows.add(new StatRow(
                            stat.getRunNumber(),
                            stat.isMainProgram(),
                            stat.getProgramOrFunctionName(),
                            stat.getArchitectureType(),
                            degree,  // ← MAKE SURE THIS PASSES THE DEGREE
                            stat.getResult(),
                            stat.getCycles()
                    ));
                }

                UserStatisticsResponse response = new UserStatisticsResponse(targetUsername, rows);

                // ← ADD THIS: Print the JSON to see what's being sent
                String json = gson.toJson(response);
                System.out.println("Response JSON: " + json);

                resp.getWriter().write(json);

            } catch (ProgramNotExecutedYetException e) {
                // User has no execution history - return empty statistics
                System.out.println("No statistics found for user: " + targetUsername);
                UserStatisticsResponse response = new UserStatisticsResponse(targetUsername, new ArrayList<>());
                resp.getWriter().write(gson.toJson(response));
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch user statistics: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "server_error: " + e.getMessage())));
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