package ch.framedev.metarapp.database;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 22.08.2024 22:49
 */

import java.sql.*;

import ch.framedev.metarapp.events.DatabaseErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import org.jetbrains.annotations.NotNull;

public class SQLiteDatabaseHelper extends DatabaseHelper {

    @Override
    public Connection getConnection() {
        return SQLite.connect();
    }

    @Override
    public void createTable(String tableName, String[] columns, Callback<Boolean> callback) throws SQLException {
        super.createTable(tableName, columns, callback);
        String sql = getString(tableName, columns);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try (Connection conn = result;
                     Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    callback.onResult(true);
                } catch (SQLException e) {
                    callback.onError(e);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                }
            }

            @Override
            public void onError(Exception e) {
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                callback.onError(e);
            }
        });
    }

    private static @NotNull String getString(String tableName, String[] columns) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            stringBuilder.append(columns[i]);
            if (i < columns.length - 1) {
                stringBuilder.append(",");
            }
        }
        String sql;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                stringBuilder +
                ", created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
        return sql;
    }

    @Override
    public void isTableExists(String tableName, Callback<Boolean> callback) throws SQLException {
        super.isTableExists(tableName, callback);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try (
                        PreparedStatement statement = result.prepareStatement(
                                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ? AND name NOT LIKE 'sqlite_%'"
                        )
                ) {
                    statement.setString(1, tableName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        callback.onResult(resultSet.next());
                    }
                } catch (Exception ex) {
                    callback.onError(ex);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);  // you forgot this too!
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
            }
        });
    }

    @Override
    public void exists(String tableName, String columnName, String value, Callback<Boolean> callback) throws
            SQLException {
        super.exists(tableName, columnName, value, callback);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try {
                    PreparedStatement statement = result.prepareStatement(
                            "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?");

                    statement.setString(1, value);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        boolean exists = resultSet.next();
                        callback.onResult(exists);
                    }
                } catch (Exception ex) {
                    callback.onError(ex);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
                }
            }

            @Override
            public void onError(Exception e) {
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                callback.onError(e);
            }
        });
    }

    @Override
    public void get(String tableName, String columnName, String whereColumn, String
            whereValue, Callback<Object> callback) throws SQLException {
        super.get(tableName, columnName, whereColumn, whereValue, callback);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try (PreparedStatement statement = result.prepareStatement(
                        "SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = ?")) {

                    statement.setString(1, whereValue);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object value = resultSet.getObject(columnName); // Extract value before exiting try block
                            callback.onResult(value);
                        } else {
                            callback.onResult(null); // Important to notify when no row found
                            EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, "No row found for " + whereColumn + " = " + whereValue, null));
                        }
                    }

                } catch (Exception ex) {
                    callback.onError(ex);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
                }
            }

            @Override
            public void onError(Exception e) {
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                callback.onError(e);
            }
        });
    }

    @Override
    public void update(String tableName, String selectedName, Object selectedValue, String whereColumn, String
            whereValue, Callback<Boolean> callback) throws SQLException {
        super.update(tableName, selectedName, selectedValue, whereColumn, whereValue, callback);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try (PreparedStatement preparedStatement = result.prepareStatement(
                        "UPDATE " + tableName + " SET " + selectedName + " = ? WHERE " + whereColumn + " = ?")) {

                    preparedStatement.setObject(1, selectedValue);
                    preparedStatement.setString(2, whereValue);
                    callback.onResult(preparedStatement.executeUpdate() > 0);
                } catch (Exception ex) {
                    callback.onError(ex);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
                }
            }

            @Override
            public void onError(Exception e) {
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                callback.onError(e);
            }
        });
    }

    @Override
    public <T> void get(String tableName, String columnName, String whereColumn, String
            whereValue, Class<T> clazz, Callback<T> callback) throws SQLException {
        super.get(tableName, columnName, whereColumn, whereValue, clazz, callback);
        SQLite.connectAsync(new SQLite.Callback<>() {
            @Override
            public void onResult(Connection result) {
                try (PreparedStatement statement = result.prepareStatement(
                        "SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = ?")) {

                    statement.setString(1, whereValue);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            // Always extract value *before* calling back
                            Object value = resultSet.getObject(columnName);
                            T castedValue = clazz.cast(value);
                            callback.onResult(castedValue);
                        } else {
                            callback.onResult(null); // Or handle not found
                            EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, "No row found for " + whereColumn + " = " + whereValue, null));
                        }
                    }

                } catch (Exception ex) {
                    callback.onError(ex);
                    EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
                }
            }

            @Override
            public void onError(Exception e) {
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                callback.onError(e);
            }
        });
    }
}