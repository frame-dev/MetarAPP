package ch.framedev.metarapp.database;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 22.08.2024 22:48
 */

import java.sql.Connection;
import java.sql.SQLException;

import ch.framedev.metarapp.events.DatabaseSendEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;

public abstract class DatabaseHelper {

    public void createTable(String tableName, String[] columns, Callback<Boolean> callback) throws SQLException {
        EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "CREATE TABLE " + tableName + " (" + String.join(", ", columns) + ")"));
    }

    /**
     * Creates a new table in the database.
     *
     * @param tableName The name of the table to create.
     * @param callback  The callback to call when the operation is finished.
     * @throws SQLException If an error occurs while creating the table.
     */
    public void isTableExists(String tableName, Callback<Boolean> callback) throws SQLException {
        if(Main.database.isSQLite()) {
            // For SQLite, we check if the table exists by querying the sqlite_master table
            EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'"));
        } else {
            // For other databases, we can use a different query
            EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "SHOW TABLES LIKE '" + tableName + "'"));
        }
    }
    /**
     * Creates a new table in the database.
     *
     * @param tableName The name of the table to create.
     * @param callback  The callback to call when the operation is finished.
     * @throws SQLException If an error occurs while creating the table.
     */
    public void exists(String tableName, String columnName, String value, Callback<Boolean> callback) throws SQLException {
        EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "SELECT * FROM " + tableName + " WHERE " + columnName + " = '" + value + "'"));
    }
    /**
     * Creates a new table in the database.
     *
     * @param tableName The name of the table to create.
     * @param callback  The callback to call when the operation is finished.
     * @throws SQLException If an error occurs while creating the table.
     */
    public void get(String tableName, String columnName, String whereColumn, String whereValue, Callback<Object> callback) throws SQLException {
        EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = '" + whereValue + "'"));
    }
    /**
     * Creates a new table in the database.
     *
     * @param tableName The name of the table to create.
     * @param callback  The callback to call when the operation is finished.
     * @throws SQLException If an error occurs while creating the table.
     */
    public void update(String tableName, String selectedName, Object selectedValue, String whereColumn, String whereValue, Callback<Boolean> callback) throws SQLException {
        EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "UPDATE " + tableName + " SET " + selectedName + " = '" + selectedValue + "' WHERE " + whereColumn + " = '" + whereValue + "'"));
    }
    /**
     * Creates a new table in the database.
     *
     * @param tableName The name of the table to create.
     * @param callback  The callback to call when the operation is finished.
     * @throws SQLException If an error occurs while creating the table.
     */
    public <T> void get(String tableName, String columnName, String whereColumn, String whereValue, Class<T> clazz, Callback<T> callback) throws SQLException {
        EventBus.dispatchDatabaseSendEvent(new DatabaseSendEvent(tableName, "SELECT " + columnName + " FROM " + tableName + " WHERE " + whereColumn + " = '" + whereValue + "'"));
    }

    /**
     * Returns a connection to the database.
     * @return The connection to the database.
     * @throws SQLException If an error occurs while creating the table.
     */
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("This method should be implemented by subclasses");
    }
}