package sserver.registry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UsersRegistry {
  private final ConcurrentHashMap<String, Boolean> users = new ConcurrentHashMap<>();
  public boolean tryAdd(String username){ return users.putIfAbsent(username, Boolean.TRUE) == null; }
  public Set<String> list(){ return users.keySet(); }
}
