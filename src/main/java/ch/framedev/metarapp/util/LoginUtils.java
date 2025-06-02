package ch.framedev.metarapp.util;

import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.simplejavautils.PasswordHasher;
import org.apache.log4j.Level;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class LoginUtils {

    public String userName, password;
    final PasswordHasher passwordHasher;
    byte[] hashedPassword;

    public static String userNameStatic;
    public static boolean active;

    public LoginUtils() {
        this.passwordHasher = new PasswordHasher();
    }

    public LoginUtils(String userName, String password) {
        this.userName = userName;
        this.password = password;
        try {
            this.passwordHasher = new PasswordHasher();
            this.hashedPassword = passwordHasher.hashPassword(password);
        } catch (Exception e) {
            Main.loggerUtils.addLog(ErrorCode.ERROR_HASHING_PASSWORD.getError() + " : " + e.getMessage());
            Main.getLogger().error(ErrorCode.ERROR_HASHING_PASSWORD.getError() + " : " + e.getMessage(), e);
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_HASHING_PASSWORD, e.getMessage()));
            throw new RuntimeException(e);
        }
        userNameStatic = userName;
    }

    public boolean create() {
        try {
            return Main.database.createAccount(userName, hashedPassword);
        } catch (SQLException e) {
            Main.loggerUtils.addLog(ErrorCode.ERROR_CREATE_ACCOUNT.getError() + " : " + e.getMessage());
            Main.getLogger().log(Level.ERROR, "Error creating account", e);
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_CREATE_ACCOUNT, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> isRight() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Main.database.isUserRight(userName, password).thenAccept(future::complete);
        return future;
    }

    public boolean isRight(String userName, String password) {
        final boolean[] result = new boolean[1];
        Main.database.isUserRight(userName, password).thenAccept(aBoolean -> result[0] = aBoolean);
        return result[0];
    }

    public boolean changePassword(String userName, String oldPassword, String newPassword) {
        return Main.database.changePassword(userName, oldPassword, newPassword);
    }
}
