package sserver.ctx;

import sserver.registry.UsersRegistry;
import sserver.registry.ProgramsRegistry;
import sserver.registry.SessionRegistry;
import sserver.registry.ExecutionRegistry;
import sserver.registry.EngineRegistry;
import core.logic.engine.Engine;

public final class AppContext {
    private static final UsersRegistry USERS = new UsersRegistry();
    private static final ProgramsRegistry PROGRAMS = new ProgramsRegistry(new Engine());
    private static final SessionRegistry SESSIONS = new SessionRegistry();
    private static final ExecutionRegistry EXECUTIONS = new ExecutionRegistry();
    private static final EngineRegistry ENGINES = new EngineRegistry();
    static {
        PROGRAMS.seedDummy();
    }
    public static UsersRegistry users(){ return USERS; }
    public static ProgramsRegistry programs(){ return PROGRAMS; }
    public static SessionRegistry sessions(){ return SESSIONS; }
    public static ExecutionRegistry executions(){ return EXECUTIONS; }
    public static EngineRegistry engines(){ return ENGINES; }
    private AppContext(){}
}