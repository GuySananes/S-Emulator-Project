package sserver.api;

import com.google.gson.Gson;
import sserver.manager.ChatManager;
import sserver.ctx.AppContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "SendChatMessageServlet", urlPatterns = "/api/chat/send")
public class SendChatMessageServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("SendChatMessageServlet: Received POST request");

            // Check authentication using cookie-based session
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null) {
                System.out.println("SendChatMessageServlet: No session cookie found");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("success", false);
                response.put("message", "No session cookie");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);
            if (username == null) {
                System.out.println("SendChatMessageServlet: Invalid session - " + sessionId);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("success", false);
                response.put("message", "User not logged in");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            System.out.println("SendChatMessageServlet: User authenticated - " + username);

            String message = req.getParameter("message");

            if (message == null || message.trim().isEmpty()) {
                System.out.println("SendChatMessageServlet: Empty message");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "Message cannot be empty");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            // Get chat manager and add message
            ChatManager chatManager = (ChatManager)
                    getServletContext().getAttribute("chatManager");

            if (chatManager == null) {
                System.out.println("SendChatMessageServlet: ChatManager not initialized!");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.put("success", false);
                response.put("message", "Chat manager not initialized");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            chatManager.addMessage(username, message.trim());
            System.out.println("SendChatMessageServlet: Message added - " + message);

            response.put("success", true);
            response.put("message", "Message sent successfully");
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("SendChatMessageServlet: Error - " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            resp.getWriter().write(gson.toJson(response));
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