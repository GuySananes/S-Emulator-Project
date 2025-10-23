package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.ctx.AppContext;
import java.io.IOException;

@WebServlet(name = "UsersServlet", urlPatterns = "/api/users")
public class UsersServlet extends HttpServlet {
  private final Gson gson = new Gson();
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.getWriter().write(gson.toJson(AppContext.users().list()));
  }
}
