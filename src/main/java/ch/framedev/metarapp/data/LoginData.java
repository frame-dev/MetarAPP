package ch.framedev.metarapp.data;

public class LoginData {

    private final String userName;
    private final String password;

    public LoginData(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
