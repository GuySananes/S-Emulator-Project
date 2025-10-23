package javafxUI.service;
import java.net.*; import java.net.http.*;

public class AuthApi extends HttpBase {
  public boolean login(String username) throws Exception {
    var req = json(URI.create(BASE+"/api/login")).POST(HttpRequest.BodyPublishers.ofString("{\"username\":\""+username+"\"}")).build();
    var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    return res.statusCode()==200;
  }
}
