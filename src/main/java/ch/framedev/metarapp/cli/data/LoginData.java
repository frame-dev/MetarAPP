package ch.framedev.metarapp.cli.data;

public class LoginData {

    private final String userName, password;
    private boolean dontAskBoolean;

    public LoginData(String userName, String password, boolean dontAskBoolean) {
        this.userName = userName;
        this.password = password;
        this.dontAskBoolean = dontAskBoolean;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isDontAskBoolean() {
        return dontAskBoolean;
    }

    public void setDontAskBoolean(boolean dontAskBoolean) {
        this.dontAskBoolean = dontAskBoolean;
    }
}
