package sserver.ctx;

import sserver.registry.v2.UsersRegistryV2;
import sserver.util.TsClock;

public class AppContextV2 {
    private final UsersRegistryV2 usersRegistry;
    private final TsClock tsClock;

    public AppContextV2() {
        this.usersRegistry = new UsersRegistryV2();
        this.tsClock = new TsClock();
    }

    public UsersRegistryV2 users() {
        return usersRegistry;
    }

    public TsClock clock() {
        return tsClock;
    }
}