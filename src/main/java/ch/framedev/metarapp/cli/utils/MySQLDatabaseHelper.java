package ch.framedev.metarapp.cli.utils;



/*
 * ch.framedev.metarappcli.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 24.09.2024 19:20
 */

import ch.framedev.javamysqlutils.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLDatabaseHelper implements DatabaseHelper {
    @Override
    public Connection getConnection() {
        try {
            return MySQL.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean createTable(String tableName, String... columnNames) throws SQLException {
        if (columnNames == null || columnNames.length == 0) {
            throw new IllegalArgumentException("At least one column must be specified.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            stringBuilder.append(columnNames[i]);
            if (i < columnNames.length - 1) {
                stringBuilder.append(", ");
            }
        }
        String columnDefinitions = stringBuilder.toString();

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "ID INT PRIMARY KEY AUTO_INCREMENT, "
                + columnDefinitions
                + ", created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(sql)) {
            stmt.execute();  // execute() is used here instead of executeUpdate()
            return true;
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean isTableExists(String tableName) throws SQLException {
        try (Connection connection = MySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SHOW TABLES LIKE ?");
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean exists(String tableName, String columnName, String value) throws SQLException {
        try (Connection connection = MySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnName + " = ?");
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public Object get(String tableName, String columnName, String whereColumn, String whereValue) throws SQLException {
        try (Connection connection = MySQL.getConnection()) {
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
        if (columnNames != null && columnNames.length != 0 && data != null && data.length != 0) {
            if (columnNames.length != data.length) {
                throw new IllegalArgumentException("Number of columns and data values must match.");
            } else {
                StringBuilder columnsBuilder = new StringBuilder();
                StringBuilder placeholdersBuilder = new StringBuilder();

                for (int i = 0; i < columnNames.length; ++i) {
                    columnsBuilder.append(columnNames[i]);
                    placeholdersBuilder.append("?");
                    if (i < columnNames.length - 1) {
                        columnsBuilder.append(", ");
                        placeholdersBuilder.append(", ");
                    }
                }

                String sql = "INSERT INTO " + tableName + " (" + columnsBuilder + ") VALUES (" + placeholdersBuilder + ")";

                try {
                    Connection conn = getConnection();

                    boolean result;
                    try {
                        PreparedStatement stmt = conn.prepareStatement(sql);

                        try {
                            for (int i = 0; i < data.length; ++i) {
                                stmt.setString(i + 1, data[i]);
                            }

                            result = stmt.executeUpdate() > 0;
                        } catch (Throwable var12) {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (Throwable var11) {
                                    var12.addSuppressed(var11);
                                }
                            }

                            throw var12;
                        }

                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (Throwable e) {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (Throwable var10) {
                                e.addSuppressed(var10);
                            }
                        }

                        throw e;
                    }

                    if (conn != null) {
                        conn.close();
                    }

                    return result;
                } catch (SQLException e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("Columns and data must be provided.");
        }
    }
}
