package ch.framedev.metarapp.cli.utils;



/*
 * ch.framedev.metarappcli.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 24.09.2024 19:20
 */

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseHelper {

    boolean createTable(String tableName, String... columnNames) throws SQLException;

    boolean isTableExists(String tableName) throws SQLException;

    boolean exists(String tableName, String columnName, String value) throws SQLException;

    Object get(String tableName, String columnName, String whereColumn, String whereValue) throws SQLException;

    boolean insertData(String tableName, String[] data, String... columnNames) throws SQLException;

    Connection getConnection();
}