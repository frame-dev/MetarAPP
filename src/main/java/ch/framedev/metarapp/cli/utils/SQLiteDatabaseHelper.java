package ch.framedev.metarapp.cli.utils;



/*
 * ch.framedev.metarappcli.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 22.08.2024 22:49
 */

import ch.framedev.javasqliteutils.SQLite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

public class SQLiteDatabaseHelper implements DatabaseHelper {

    @Override
    public Connection getConnection() {
        return SQLite.connect();
    }

    @Override
    public boolean createTable(String tableName, String... columnNames) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            stringBuilder.append(columnNames[i]);
            if (i < columnNames.length - 1) {
                stringBuilder.append(",");
            }
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                    " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    stringBuilder.toString() +
                    ", created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Connection conn = SQLite.connect();
             PreparedStatement stmt = Objects.requireNonNull(conn).prepareStatement(sql)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean isTableExists(String tableName) throws SQLException {
        try (Connection connection = SQLite.connect()) {
            PreparedStatement statement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean exists(String tableName, String columnName, String value) throws SQLException {
        try (Connection connection = SQLite.connect()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnName + " = ?");
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public Object get(String tableName, String columnName, String whereColumn, String whereValue) throws SQLException {
        try (Connection connection = SQLite.connect()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = ?");
            statement.setString(1, whereValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject(columnName);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insertData(String tableName, String[] data, String... columnNames) throws SQLException {
        if (columnNames.length == 0 || data.length == 0 || columnNames.length != data.length) {
            throw new IllegalArgumentException("Columns and data arrays must not be empty and must have the same length.");
        }

        // Join the column names with commas
        String columns = String.join(",", columnNames);

        // Create a string of placeholders (e.g., "?, ?, ?")
        String placeholders = String.join(",", Collections.nCopies(columnNames.length, "?"));

        // Construct the SQL insert statement
        String sql = "INSERT INTO " + tableName + " (" + columns+ ") VALUES (" + placeholders + ")";
        boolean result;

        // Try-with-resources ensures that resources are closed automatically
        try (Connection conn = SQLite.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the values for each placeholder
            for (int i = 0; i < data.length; i++) {
                stmt.setString(i + 1, data[i]);
            }

            // Execute the statement
            result = stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return result;
    }
}