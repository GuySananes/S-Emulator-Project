package sserver.registry;

import core.logic.engine.Engine;
import present.program.ProgramSummary;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgramsRegistry {
  private final Engine engine;
  private final CopyOnWriteArrayList<ProgramSummary> programs = new CopyOnWriteArrayList<>();
  public ProgramsRegistry(Engine engine){ this.engine = engine; }
  public List<ProgramSummary> list(){ return programs; } // start with dummy; wire engine later
  public void seedDummy(){
    programs.add(new ProgramSummary("Demo", "system", 12, "I", 0, 0.0));
  }
}
