package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="ProgramsServlet", urlPatterns="/api/programs")
public class ProgramsServlet extends HttpServlet {
  private final Gson gson = new Gson();
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.getWriter().write(gson.toJson(AppContext.programs().list()));
  }
}
