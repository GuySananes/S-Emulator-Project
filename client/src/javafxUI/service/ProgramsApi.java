package javafxUI.service;
import java.net.*; import java.net.http.*; import present.program.ProgramSummary;

public class ProgramsApi extends HttpBase {
  public ProgramSummary[] list() throws Exception {
    var req = HttpRequest.newBuilder(URI.create(BASE+"/api/programs")).GET().build();
    var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode()!=200) throw new RuntimeException("programs http "+res.statusCode());
    return GSON.fromJson(res.body(), ProgramSummary[].class);
  }
}
