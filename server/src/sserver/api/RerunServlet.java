package sserver.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import core.logic.engine.Engine;
import exception.NoProgramException;
import exception.NoSuchRunException;
import exception.ProgramNotExecutedYetException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import run.ReExecuteProgramDTO;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Servlet for re-executing S-programs with previous run parameters.
 * Handles POST requests to /api/execution/rerun endpoint.
 * Uses session-based engines to maintain per-user program state.
 */
@WebServlet(name="RerunServlet", urlPatterns="/api/execution/rerun")
public class RerunServlet extends BaseServlet {

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
            // Parse request body to get run number
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            if (sb.length() == 0) {
                sendValidationError(resp, "Request body is required. Please provide runNumber.");
                return;
            }

            JsonObject requestJson = JsonParser.parseString(sb.toString()).getAsJsonObject();

            if (!requestJson.has("runNumber")) {
                sendValidationError(resp, "runNumber is required in request body.");
                return;
            }

            int runNumber;
            try {
                runNumber = requestJson.get("runNumber").getAsInt();
            } catch (Exception e) {
                sendValidationError(resp, "runNumber must be a valid integer.");
                return;
            }

            if (runNumber < 1) {
                sendValidationError(resp, "runNumber must be greater than 0.");
                return;
            }

            logger.info(String.format("Rerunning program execution #%d | SessionID: %s | User: %s",
                    runNumber, sessionId, username));

            // Call reExecuteProgram with the specified run number
            ReExecuteProgramDTO rerunResult = engine.reExecuteProgram(runNumber);

            logger.info(String.format("Program rerun completed successfully for execution #%d | SessionID: %s | User: %s",
                    runNumber, sessionId, username));

            // Return the rerun result
            resp.setContentType("application/json");
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(rerunResult));

        } catch (NoProgramException e) {
            sendValidationError(req, resp, "No program loaded. Please select a program first.");
        } catch (ProgramNotExecutedYetException e) {
            sendValidationError(req, resp, "Program has not been executed yet. Please execute the program before attempting to rerun.");
        } catch (NoSuchRunException e) {
            sendValidationError(req, resp, e.getMessage());
        } catch (Exception e) {
            sendExecutionError(req, resp, "Failed to rerun program", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendError(resp, 405, "validation", "Method not allowed. Use POST.");
    }
}