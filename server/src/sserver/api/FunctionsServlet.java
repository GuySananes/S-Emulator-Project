package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name = "FunctionsServlet", urlPatterns = "/api/functions")
public class FunctionsServlet extends HttpServlet {
  private final Gson gson = new Gson();
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    // Placeholder: return empty list until functions are wired from engine/DTO
    resp.getWriter().write(gson.toJson(new String[]{}));
  }
}
