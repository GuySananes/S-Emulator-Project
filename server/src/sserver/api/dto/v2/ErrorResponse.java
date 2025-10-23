package sserver.api.dto.v2;

public class ErrorResponse {
    public String reason;

    public ErrorResponse(String reason) {
        this.reason = reason;
    }
}