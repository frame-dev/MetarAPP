package ch.framedev.metarapp.events;

public class LogoutEvent {
    
    private final String username;
    private String message;

    public LogoutEvent(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
