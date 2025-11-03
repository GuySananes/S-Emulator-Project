package sserver.api;

import com.google.gson.Gson;
import sserver.manager.ChatManager;
import sserver.model.ChatMessage;
import sserver.ctx.AppContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetChatMessagesServlet", urlPatterns = "/api/chat/messages")
public class GetChatMessagesServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        Map<String, Object> response = new HashMap<>();

        try {
            // Check authentication using cookie-based session
            String sessionId = getSessionIdFromCookie(req);
            if (sessionId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("success", false);
                response.put("message", "No session cookie");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            String username = AppContext.sessions().getUserBySession(sessionId);
            if (username == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("success", false);
                response.put("message", "User not logged in");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            // Get chat manager
            ChatManager chatManager = (ChatManager)
                    getServletContext().getAttribute("chatManager");

            if (chatManager == null) {
                System.out.println("GetChatMessagesServlet: ChatManager not initialized!");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.put("success", false);
                response.put("message", "Chat manager not initialized");
                resp.getWriter().write(gson.toJson(response));
                return;
            }

            // Get messages from index (for polling)
            String fromIndexStr = req.getParameter("fromIndex");
            List<ChatMessage> messages;

            if (fromIndexStr != null) {
                int fromIndex = Integer.parseInt(fromIndexStr);
                messages = chatManager.getMessages(fromIndex);
            } else {
                messages = chatManager.getAllMessages();
            }

            response.put("success", true);
            response.put("messages", messages);
            response.put("totalCount", chatManager.getMessageCount());
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("GetChatMessagesServlet: Error - " + e.getMessage());
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