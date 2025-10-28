package sserver.api.dto;

/**
 * Standardized error response class with type categorization and optional details.
 * Maintains backward compatibility with existing 'reason' field while adding
 * enhanced error handling capabilities.
 */
public class ErrorResponse {
    public String reason;  // Kept for backward compatibility
    public String error;   // New standardized error message field
    public String type;    // Error type: "validation", "execution", "authentication"
    public String details; // Optional additional error details

    // Legacy constructor for backward compatibility
    public ErrorResponse(String reason) {
        this.reason = reason;
        this.error = reason;
        this.type = "general";
        this.details = null;
    }

    // Enhanced constructor with type and details
    public ErrorResponse(String error, String type, String details) {
        this.reason = error;  // For backward compatibility
        this.error = error;
        this.type = type;
        this.details = details;
    }

    // Static factory methods for different error types
    public static ErrorResponse validation(String message) {
        return new ErrorResponse(message, "validation", null);
    }

    public static ErrorResponse validation(String message, String details) {
        return new ErrorResponse(message, "validation", details);
    }

    public static ErrorResponse execution(String message) {
        return new ErrorResponse(message, "execution", null);
    }

    public static ErrorResponse execution(String message, String details) {
        return new ErrorResponse(message, "execution", details);
    }

    public static ErrorResponse authentication(String message) {
        return new ErrorResponse(message, "authentication", null);
    }

    public static ErrorResponse general(String message) {
        return new ErrorResponse(message, "general", null);
    }
}