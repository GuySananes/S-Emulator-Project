package sserver.api;

import core.logic.engine.Engine;
import exception.NoProgramException;
import exception.ProgramNotExecutedYetException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;
import statistic.ProgramStatisticsDTO;

import java.io.IOException;

@WebServlet(name="StatisticsServlet", urlPatterns="/api/execution/statistics")
public class StatisticsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Check authentication
        if (!checkAuthentication(req)) {
            sendAuthenticationError(resp);
            return;
        }

        // Get session engine
        EngineRegistry.EngineContext engineCtx = getSessionEngine(req);
        if (engineCtx == null) {
            sendAuthenticationError(resp);
            return;
        }

        String sessionId = getSessionIdFromCookie(req);
        String username = AppContext.sessions().getUserBySession(sessionId);
        Engine engine = engineCtx.engine;

        try {
            logger.info(String.format("Retrieving program statistics | SessionID: %s | User: %s", sessionId, username));

            // Call presentProgramStats to get program statistics
            ProgramStatisticsDTO statistics = engine.presentProgramStats();

            logger.info(String.format("Program statistics retrieved successfully | SessionID: %s | User: %s", sessionId, username));

            // Return the statistics
            resp.setContentType("application/json");
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(statistics));

        } catch (NoProgramException e) {
            sendValidationError(req, resp, "No program loaded. Please select a program first.");
        } catch (ProgramNotExecutedYetException e) {
            sendValidationError(req, resp, "Program has not been executed yet. Please execute the program before viewing statistics.");
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to retrieve program statistics", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendError(resp, 405, "validation", "Method not allowed. Use GET.");
    }
}