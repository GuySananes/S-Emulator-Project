
package sserver.api.v2;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import sserver.api.dto.v2.*;
import sserver.ctx.AppBootstrapListener;
import sserver.ctx.AppContextV2;
import sserver.util.Http;
import java.io.IOException;
import java.util.List;

@WebServlet(name="UsersServletV2", urlPatterns="/api/v2/users")
public class UsersServletV2 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppContextV2 ctx = AppBootstrapListener.get(getServletContext());

        try {
            long ts = ctx.clock().current();
            List<UserRow> users = ctx.users().snapshot();

            Http.writeJson(resp, 200, new UsersResponse(ts, users));

        } catch (Exception e) {
            Http.writeJson(resp, 500, new ErrorResponse("SERVER_ERROR"));
        }
    }
}