package ch.framedev.metarapp.events;

/**
 * This event is dispatched when a login attempt is made.
 * It contains the username, whether the "remember me" option was selected,
 * and whether the login was successful.
 */
public class LoginEvent {

    private String username;
    private boolean rememberMe;
    private boolean success;

    public LoginEvent(String username, boolean rememberMe, boolean success) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username;
        this.rememberMe = rememberMe;
        this.success = success;
    }

    public String getUsername() {
        return username;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public boolean isSuccess() {
        return success;
    }
}
