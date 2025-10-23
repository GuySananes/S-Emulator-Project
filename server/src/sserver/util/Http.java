package sserver.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Http {
    public static void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonUtil.GSON.toJson(body));
    }

    public static <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        req.setCharacterEncoding("UTF-8");
        return JsonUtil.GSON.fromJson(req.getReader(), clazz);
    }
}