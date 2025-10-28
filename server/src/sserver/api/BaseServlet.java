package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sserver.api.dto.ErrorResponse;
import sserver.ctx.AppContext;
import sserver.registry.EngineRegistry;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base servlet class providing common session engine logic and standardized error handling.
 * All API servlets should extend this class to ensure consistent session management
 * and error response formatting.
 */
public abstract class BaseServlet extends HttpServlet {
    protected final Gson gson = new Gson();
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Extract session ID from JSESSIONID cookie.
     * Copied from existing servlet implementations for consistency.
     * 
     * @param req HTTP request containing cookies
     * @return session ID string or null if not found
     */
    protected String getSessionIdFromCookie(HttpServletRequest req) {
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

    /**
     * Get or create session-specific engine context using AppContext.engines().
     * This ensures each user session has its own isolated Engine instance.
     * 
     * @param req HTTP request to extract session ID from
     * @return EngineContext for the session, or null if no valid session
     */
    protected EngineRegistry.EngineContext getSessionEngine(HttpServletRequest req) {
        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            return null;
        }
        
        // Verify session is valid before creating/getting engine
        if (!AppContext.sessions().isValidSession(sessionId)) {
            return null;
        }
        
        return AppContext.engines().getOrCreateEngine(sessionId);
    }

    /**
     * Check if the request has valid authentication.
     * Verifies both session ID cookie and session validity.
     * 
     * @param req HTTP request to check
     * @return true if authenticated, false otherwise
     */
    protected boolean checkAuthentication(HttpServletRequest req) {
        String sessionId = getSessionIdFromCookie(req);
        if (sessionId == null) {
            return false;
        }
        
        String username = AppContext.sessions().getUserBySession(sessionId);
        return username != null;
    }

    /**
     * Send standardized error response with HTTP status code.
     * Uses the existing ErrorResponse class for consistent formatting.
     * Includes comprehensive logging with context information.
     * 
     * @param resp HTTP response to write to
     * @param status HTTP status code (400, 401, 500, etc.)
     * @param type Error type ("validation", "execution", "authentication")
     * @param message Error message
     * @throws IOException if writing response fails
     */
    protected void sendError(HttpServletResponse resp, int status, String type, String message) throws IOException {
        sendError(resp, status, type, message, null);
    }

    /**
     * Send standardized error response with additional details.
     * Includes comprehensive logging with session ID and operation context.
     * 
     * @param resp HTTP response to write to
     * @param status HTTP status code
     * @param type Error type
     * @param message Error message
     * @param details Additional error details
     * @throws IOException if writing response fails
     */
    protected void sendError(HttpServletResponse resp, int status, String type, String message, String details) throws IOException {
        // Log error with appropriate level based on status code
        Level logLevel = (status >= 500) ? Level.SEVERE : Level.WARNING;
        String logMessage = String.format("[%s] Error: %s - %s%s", 
            type, 
            message, 
            details != null ? details : "no details",
            getContextInfo(resp));
        logger.log(logLevel, logMessage);
        
        resp.setStatus(status);
        resp.setContentType("application/json");
        ErrorResponse error = new ErrorResponse(message, type, details);
        resp.getWriter().write(gson.toJson(error));
    }
    
    /**
     * Get context information for logging (session ID, servlet name, etc.)
     * 
     * @param resp HTTP response to extract request context from
     * @return formatted context string
     */
    private String getContextInfo(HttpServletResponse resp) {
        StringBuilder context = new StringBuilder();
        context.append(" | Servlet: ").append(getClass().getSimpleName());
        return context.toString();
    }

    /**
     * Send standardized error response with session context.
     * Includes session ID in logging for better traceability.
     * 
     * @param req HTTP request to extract session from
     * @param resp HTTP response to write to
     * @param status HTTP status code
     * @param type Error type
     * @param message Error message
     * @param details Additional error details
     * @throws IOException if writing response fails
     */
    protected void sendError(HttpServletRequest req, HttpServletResponse resp, int status, String type, String message, String details) throws IOException {
        // Log error with session context
        Level logLevel = (status >= 500) ? Level.SEVERE : Level.WARNING;
        String sessionId = getSessionIdFromCookie(req);
        String username = sessionId != null ? AppContext.sessions().getUserBySession(sessionId) : null;
        
        String logMessage = String.format("[%s] Error: %s - %s%s | SessionID: %s | User: %s | Servlet: %s", 
            type, 
            message, 
            details != null ? details : "no details",
            "",
            sessionId != null ? sessionId : "none",
            username != null ? username : "none",
            getClass().getSimpleName());
        logger.log(logLevel, logMessage);
        
        resp.setStatus(status);
        resp.setContentType("application/json");
        ErrorResponse error = new ErrorResponse(message, type, details);
        resp.getWriter().write(gson.toJson(error));
    }

    /**
     * Send authentication error (HTTP 401).
     * Convenience method for common authentication failures.
     * 
     * @param resp HTTP response to write to
     * @throws IOException if writing response fails
     */
    protected void sendAuthenticationError(HttpServletResponse resp) throws IOException {
        logger.log(Level.WARNING, "[authentication] Unauthorized access attempt | Servlet: " + getClass().getSimpleName());
        sendError(resp, 401, "authentication", "unauthorized");
    }

    /**
     * Send validation error (HTTP 400).
     * Convenience method for request validation failures.
     * 
     * @param resp HTTP response to write to
     * @param message Validation error message
     * @throws IOException if writing response fails
     */
    protected void sendValidationError(HttpServletResponse resp, String message) throws IOException {
        logger.log(Level.WARNING, "[validation] " + message + " | Servlet: " + getClass().getSimpleName());
        sendError(resp, 400, "validation", message);
    }

    /**
     * Send validation error with session context (HTTP 400).
     * Includes session ID in logging for better traceability.
     * 
     * @param req HTTP request to extract session from
     * @param resp HTTP response to write to
     * @param message Validation error message
     * @throws IOException if writing response fails
     */
    protected void sendValidationError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        sendError(req, resp, 400, "validation", message, null);
    }

    /**
     * Send execution error (HTTP 500).
     * Convenience method for engine/execution failures.
     * 
     * @param resp HTTP response to write to
     * @param message Execution error message
     * @throws IOException if writing response fails
     */
    protected void sendExecutionError(HttpServletResponse resp, String message) throws IOException {
        logger.log(Level.SEVERE, "[execution] " + message + " | Servlet: " + getClass().getSimpleName());
        sendError(resp, 500, "execution", message);
    }

    /**
     * Send execution error with details (HTTP 500).
     * 
     * @param resp HTTP response to write to
     * @param message Execution error message
     * @param details Additional error details (e.g., exception message)
     * @throws IOException if writing response fails
     */
    protected void sendExecutionError(HttpServletResponse resp, String message, String details) throws IOException {
        logger.log(Level.SEVERE, "[execution] " + message + " - " + details + " | Servlet: " + getClass().getSimpleName());
        sendError(resp, 500, "execution", message, details);
    }

    /**
     * Send execution error with session context (HTTP 500).
     * Includes session ID in logging for better traceability.
     * 
     * @param req HTTP request to extract session from
     * @param resp HTTP response to write to
     * @param message Execution error message
     * @param details Additional error details
     * @throws IOException if writing response fails
     */
    protected void sendExecutionError(HttpServletRequest req, HttpServletResponse resp, String message, String details) throws IOException {
        sendError(req, resp, 500, "execution", message, details);
    }
}