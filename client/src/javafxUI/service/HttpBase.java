package javafxUI.service;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public abstract class HttpBase {
  // Update this if you deploy the WAR under a different context path
  protected static final String BASE = "http://localhost:8080/web-SEmulator"; // Tomcat context path of WAR
  protected static final HttpClient HTTP = HttpClient.newHttpClient();
  protected static final Gson GSON = new Gson();
  protected static HttpRequest.Builder json(URI uri){ return HttpRequest.newBuilder(uri).header("Content-Type","application/json"); }
}
