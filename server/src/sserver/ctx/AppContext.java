package sserver.ctx;

import sserver.registry.UsersRegistry;
import sserver.registry.ProgramsRegistry;
import core.logic.engine.Engine; // from engine module

public final class AppContext {
  private static final UsersRegistry USERS = new UsersRegistry();
  private static final ProgramsRegistry PROGRAMS = new ProgramsRegistry(new Engine());
  static {
    // Seed dummy data so the Programs list isn't empty on first run
    PROGRAMS.seedDummy();
  }
  public static UsersRegistry users(){ return USERS; }
  public static ProgramsRegistry programs(){ return PROGRAMS; }
  private AppContext(){}
}
