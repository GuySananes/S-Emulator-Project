package sserver.api;

import core.logic.engine.Engine;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import present.program.PresentProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.IOException;

/**
 * Servlet for expanding S-programs to a higher degree of detail.
 * Handles POST requests to /api/execution/expand endpoint.
 * Uses session-based engines to maintain per-user program state.
 */
@WebServlet(name="ExpandServlet", urlPatterns="/api/execution/expand")
public class ExpandServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
            // Check if program is loaded
            if (!engineCtx.isProgramLoaded()) {
                sendValidationError(resp, "No program loaded. Please select a program first.");
                return;
            }

            // Get current degree from EngineContext and increment by 1
            int currentDegree = engineCtx.getCurrentDegree();
            int newDegree = currentDegree + 1;

            logger.info(String.format("Expanding program from degree %d to %d | SessionID: %s | User: %s",
                currentDegree, newDegree, sessionId, username));

            // Call expandOrShrinkProgram with the new degree
            PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(newDegree);

            // Update the degree in EngineContext
            engineCtx.setCurrentDegree(newDegree);

            logger.info(String.format("Program expanded successfully to degree %d | SessionID: %s | User: %s",
                newDegree, sessionId, username));

            // Return the updated program data
            resp.setContentType("application/json");
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(expandedProgram));

        } catch (NoProgramException e) {
            sendValidationError(req, resp, "No program loaded. Please select a program first.");
        } catch (DegreeOutOfRangeException e) {
            sendValidationError(req, resp, "Cannot expand program further. Maximum degree reached.");
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to expand program", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendError(resp, 405, "validation", "Method not allowed. Use POST.");
    }
}