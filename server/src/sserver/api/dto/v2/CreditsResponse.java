package sserver.api.dto.v2;

public class CreditsResponse {
    public int credits;
    public int used;

    public CreditsResponse(int credits, int used) {
        this.credits = credits;
        this.used = used;
    }
}