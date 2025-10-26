package sserver.api.dto;

import java.util.List;

public class UsersResponse {
    public long ts;
    public List<UserRow> users;

    public UsersResponse(long ts, List<UserRow> users) {
        this.ts = ts;
        this.users = users;
    }
}