package ch.framedev.metarapp.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLite {

    private static String databaseFilePath;
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public SQLite(String path, String fileName) {
        if (!new File(path).exists())
            if(!new File(path).mkdirs()) {
                throw new RuntimeException("Could not create directory: " + path);
            }

        databaseFilePath = new File(path, fileName).getAbsolutePath();
    }

    public interface Callback<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    // Always return a new connection
    public static Connection connect() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 5000;");
                stmt.execute("PRAGMA journal_mode=WAL;");
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void connectAsync(Callback<Connection> callback) {
        singleThreadExecutor.submit(() -> {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA busy_timeout = 5000;");
                    stmt.execute("PRAGMA journal_mode=WAL;");
                }
                callback.onResult(connection);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}