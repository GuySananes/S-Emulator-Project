package sserver.api;

import com.google.gson.Gson;
import sserver.ctx.AppContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="LoginServlet", urlPatterns="/api/login")
public class LoginServlet extends HttpServlet {
  private final Gson gson = new Gson();
  static class LoginReq { String username; }
  static class LoginResp { boolean ok; String error; LoginResp(boolean ok,String e){this.ok=ok;this.error=e;} }
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    var in = gson.fromJson(req.getReader(), LoginReq.class);
    boolean added = AppContext.users().tryAdd(in.username);
    resp.setContentType("application/json");
    if (!added) { resp.setStatus(409); resp.getWriter().write(gson.toJson(new LoginResp(false,"username_taken"))); return; }
    resp.getWriter().write(gson.toJson(new LoginResp(true,null)));
  }
}
