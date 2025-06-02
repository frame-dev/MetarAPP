package ch.framedev.metarapp.database;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 22.08.2024 22:49
 */

import ch.framedev.javamysqlutils.MySQL;
import ch.framedev.metarapp.events.DatabaseErrorEvent;
import ch.framedev.metarapp.events.EventBus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.framedev.metarapp.main.Main.getLogger;
import static ch.framedev.metarapp.main.Main.loggerUtils;

public class MySQLDatabaseHelper extends DatabaseHelper {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public Connection getConnection() throws SQLException {
        return MySQL.getConnection();
    }

    @Override
    public void createTable(String tableName, String[] columns, Callback<Boolean> callback) throws SQLException {
        super.createTable(tableName, columns, callback);
        isTableExists(tableName, new Callback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    throwErrorOnLength(columns == null || columns.length == 0, "At least one column must be specified.");

                    String columnDefinitions = getColumnDefinitions(columns);

                    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                                 + "ID INT PRIMARY KEY AUTO_INCREMENT, "
                                 + columnDefinitions
                                 + ", created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(sql)) {
                                stmt.execute();  // execute() is used here instead of executeUpdate()
                                callback.onResult(true);
                            } catch (SQLException e) {
                                callback.onError(e);
                                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, e.getMessage(), e));
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                loggerUtils.addLog("Error : " + throwable.getMessage());
                getLogger().error("Error : " + throwable.getMessage(), throwable);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, throwable.getMessage(), throwable));
            }
        });
    }

    private static void throwErrorOnLength(boolean length, String s) {
        if (length) {
            throw new IllegalArgumentException(s);
        }
    }

    private static String getColumnDefinitions(String[] columns) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            stringBuilder.append(columns[i]);
            if (i < columns.length - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void isTableExists(String tableName, Callback<Boolean> callback) throws SQLException {
        super.isTableExists(tableName, callback);
        executor.execute(() -> {
            try (Connection connection = MySQL.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SHOW TABLES LIKE ?");
                statement.setString(1, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    callback.onResult(resultSet.next());
                }
            } catch (Exception ex) {
                callback.onError(ex);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
            }
        });
    }

    @Override
    public void exists(String tableName, String columnName, String value, Callback<Boolean> callback) throws SQLException {
        super.exists(tableName, columnName, value, callback);
        executor.execute(() -> {
            try (Connection connection = MySQL.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnName + " = ?");
                statement.setString(1, value);
                try (ResultSet resultSet = statement.executeQuery()) {
                    callback.onResult(resultSet.next());
                }
            } catch (Exception ex) {
                callback.onError(ex);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
            }
        });
    }

    @Override
    public void get(String tableName, String columnName, String whereColumn, String whereValue, Callback<Object> callback) throws SQLException {
        super.get(tableName, columnName, whereColumn, whereValue, callback);
        executor.execute(() -> {
            try (Connection connection = MySQL.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = ?");
                statement.setString(1, whereValue);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        callback.onResult(resultSet.getObject(columnName));
                    }
                }
            } catch (Exception ex) {
                callback.onError(ex);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
            }
        });
    }

    @Override
    public void update(String tableName, String selectedName, Object selectedValue, String whereColumn, String whereValue, Callback<Boolean> callback) throws SQLException {
        super.update(tableName, selectedName, selectedValue, whereColumn, whereValue, callback);
        executor.execute(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + tableName + " SET " + selectedName + " = ? WHERE " + whereColumn + " = ?")) {

                preparedStatement.setObject(1, selectedValue);
                preparedStatement.setString(2, whereValue);
                callback.onResult(preparedStatement.executeUpdate() > 0);
            } catch (Exception ex) {
                callback.onError(ex);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
            }
        });
    }

    @Override
    public <T> void get(String tableName, String columnName, String whereColumn, String whereValue, Class<T> clazz, Callback<T> callback) throws SQLException {
        super.get(tableName, columnName, whereColumn, whereValue, clazz, callback);
        executor.execute(() -> {
            try (Connection connection = MySQL.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = ?");
                statement.setString(1, whereValue);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        callback.onResult(clazz.cast(resultSet.getObject(columnName)));
                    }
                }
            } catch (Exception ex) {
                callback.onError(ex);
                EventBus.dispatchDatabaseErrorEvent(new DatabaseErrorEvent(tableName, ex.getMessage(), ex));
            }
        });
    }
}
