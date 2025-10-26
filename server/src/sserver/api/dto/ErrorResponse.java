package sserver.api.dto;

public class ErrorResponse {
    public String reason;

    public ErrorResponse(String reason) {
        this.reason = reason;
    }
}