package ch.framedev.metarapp.events;

import ch.framedev.metarapp.util.ErrorCode;

public class ErrorEvent {

    private final ErrorCode errorCode;
    private final String message;

    public ErrorEvent(ErrorCode errorCode, String message) {
        if (errorCode == null) {
            throw new IllegalArgumentException("ErrorCode cannot be null");
        }
        this.errorCode = errorCode;
        this.message = message != null ? message : "";
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
    
}
