package sserver.api.dto.v2;

public class LoginResponse {
    public boolean ok;
    public String user;
    public int credits;

    public LoginResponse(boolean ok, String user, int credits) {
        this.ok = ok;
        this.user = user;
        this.credits = credits;
    }
}