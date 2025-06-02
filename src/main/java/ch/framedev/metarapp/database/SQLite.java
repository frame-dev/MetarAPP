package ch.framedev.metarapp.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLite {

    private static String databaseFilePath;
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private static Connection sharedConnection;

    public SQLite(String path, String fileName) {
        if (!new File(path).exists())
            new File(path).mkdirs();

        databaseFilePath = new File(path, fileName).getAbsolutePath();

        // Open and hold the single shared connection
        try {
            sharedConnection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
            Statement stmt = sharedConnection.createStatement();
            stmt.execute("PRAGMA busy_timeout = 5000;");
            stmt.execute("PRAGMA journal_mode=WAL;"); // even better concurrency
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite database connection", e);
        }
    }

    public interface Callback<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    // Return the same connection every time
    public static java.sql.Connection connect() {
        try {
            if(sharedConnection.isClosed())
                sharedConnection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sharedConnection;
    }

    public static void connectAsync(Callback<java.sql.Connection> callback) {
        singleThreadExecutor.submit(() -> {
            try {
                if(sharedConnection.isClosed())
                    sharedConnection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
                callback.onResult(sharedConnection);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}