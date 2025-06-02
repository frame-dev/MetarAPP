package ch.framedev.metarapp.cli.utils;

import ch.framedev.javamongodbutils.BackendMongoDBManager;
import ch.framedev.javamongodbutils.MongoDBManager;
import ch.framedev.javamysqlutils.JsonConnection;
import ch.framedev.javamysqlutils.MySQL;
import ch.framedev.javasqliteutils.SQLite;
import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import ch.framedev.simplejavautils.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;

import org.apache.log4j.Level;
import org.bson.Document;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.framedev.metarapp.cli.Main.getFilePath;
import static ch.framedev.metarapp.cli.Main.settings;
import ch.framedev.metarapp.util.Variables;
import static ch.framedev.metarapp.main.Main.connectionTokenHandler;

public class Database {

    private final String TABLE = "accounts", UTILITIES_TABLE = "utilities";
    public MongoDBManager mongoDBManager;
    public BackendMongoDBManager backendMongoDBManager;

    public Database() {
        if (getDatabaseClass() == MySQL.class) {
                new MySQL(new JsonConnection(Variables.MYSQL_HOST,
                        Variables.MYSQL_USER,
                        Variables.MYSQL_PASSWORD,
                        Variables.MYSQL_DATABASE,
                        Variables.MYSQL_PORT));
                MySQL.setAllowPublicKey(true);
                Main.getLogger().log(Level.INFO, "Connecting to database MySQL host(framedev.ch)");
        } else if (getDatabaseClass() == SQLite.class) {
            new SQLite(new ch.framedev.javasqliteutils.JsonConnection(getFilePath() + Variables.FILES_DIRECTORY, "database.db"));
            Main.getLogger().log(Level.INFO, "Connecting to database SQLite");
            SQLite.connect();
        } else if (getDatabaseClass() == MongoDBManager.class) {
            String host = settings.getString("mongodb.host");
            int port = settings.getInt("mongodb.port");
            String user = settings.getString("mongodb.user");
            String password = settings.getString("mongodb.password");
            String database = settings.getString("mongodb.database");
            mongoDBManager = new MongoDBManager(host, user, password, port, database);
            mongoDBManager.connect();
            backendMongoDBManager = new BackendMongoDBManager(mongoDBManager);
        }
    }

    public boolean isSQLDatabase() {
        return getDatabaseClass() == MySQL.class || getDatabaseClass() == SQLite.class;
    }

    public Class<?> getDatabaseClass() {
        String databaseType = (String) settings.get("database");
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return MySQL.class;
            case "sqlite":
                return SQLite.class;
            case "mongodb":
                return MongoDBManager.class;
            default:
                throw new IllegalArgumentException("Unsupported Database Type: " + databaseType);
        }
    }

    public DatabaseHelper getDatabaseHelper() {
        if (getDatabaseClass() == MySQL.class)
            return new MySQLDatabaseHelper();
        else if (getDatabaseClass() == SQLite.class)
            return new SQLiteDatabaseHelper();
        else
            throw new IllegalArgumentException("Unsupported Database Type: " + getDatabaseClass());
    }

    public void createTableIfNotExistsUtilities() {
        Class<?> dbClass = getDatabaseClass();
        String[] columns = { "UserName TEXT(255)",
                "Online TEXT",
                "Version TEXT(255)",
                "HasUpdate TEXT",
                "LastUpdated VARCHAR(2666)" };

        if (dbClass == MySQL.class) {
            try {
                if (!getDatabaseHelper().isTableExists(UTILITIES_TABLE))
                    getDatabaseHelper().createTable(UTILITIES_TABLE, columns);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (dbClass == SQLite.class) {
            try {
                if (!getDatabaseHelper().isTableExists(UTILITIES_TABLE)) {
                    getDatabaseHelper().createTable(UTILITIES_TABLE, columns);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
        }
    }

    public void createAdminAccount() {
        if (isSQLDatabase())
            createTableIfNotExists();
        try {
            createAccount("admin",
                    new PasswordHasher().hashPassword(EncryptionUtil.decrypt(connectionTokenHandler.getProperty("admin-password"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTableIfNotExists() {
        Class<?> dbClass = getDatabaseClass();
        String[] columns = {
                "UserName TEXT",
                "Password blob(255)",
                "Used INT",
                "MapOpened INT",
                "FilesDownloaded INT",
                "ICAOS VARCHAR(2556)",
                "LastUsed VARCHAR(255)"
        };

        if (dbClass == MySQL.class) {
            try {
                if (!getDatabaseHelper().isTableExists(TABLE)) {
                    getDatabaseHelper().createTable(TABLE, columns);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (dbClass == SQLite.class) {
            try {
                if (!getDatabaseHelper().isTableExists(TABLE)) {
                    getDatabaseHelper().createTable(TABLE, columns);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
        }
    }

    public boolean createAccount(String userName, byte[] hashedPassword) throws SQLException {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            Connection connection = null;

            try {
                if (dbClass == MySQL.class) {
                    connection = MySQL.getConnection();
                } else if (dbClass == SQLite.class) {
                    connection = SQLite.connect();
                } else {
                    throw new UnsupportedOperationException("Database class not supported: " + dbClass.getName());
                }

                if (connection != null) {
                    // Check if user already exists
                    try (PreparedStatement statement = connection
                            .prepareStatement("SELECT * FROM " + TABLE + " WHERE UserName = ?")) {
                        statement.setString(1, userName);
                        try (ResultSet existsResult = statement.executeQuery()) {
                            if (!existsResult.next()) {
                                // Insert the new user
                                try (PreparedStatement createUserPs = connection
                                        .prepareStatement(
                                                "INSERT INTO " + TABLE + " (UserName,Password) VALUES (?,?)")) {
                                    createUserPs.setString(1, userName);
                                    createUserPs.setBytes(2, hashedPassword);
                                    createUserPs.execute();
                                    return true;
                                }
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                String errorMessage = dbClass == MySQL.class ? ErrorCode.ERROR_MYSQL_DATABASE.getError()
                        : ErrorCode.ERROR_SQLITE_DATABASE.getError();
                Main.getLogger().log(Level.ERROR, errorMessage, ex);
                throw ex; // Consider rethrowing the exception after logging
            } finally {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
        } else {
            if (!backendMongoDBManager.exists("userName", userName, TABLE)) {
                Map<String, Object> map = new HashMap<>();
                map.put("userName", userName);
                map.put("password", hashedPassword);
                map.put("used", 0);
                map.put("mapOpened", 0);
                map.put("filesDownloaded", 0);
                map.put("ICAOS", new Gson().toJson(new ArrayList<String>()));
                map.put("lastUsed", "");
                backendMongoDBManager.createData("userName", userName, map, TABLE);
                return true;
            }
        }

        return false;
    }

    public boolean existsUser(String userName) throws SQLException {
        if (isSQLDatabase()) {

            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            Connection connection = null;

            try {
                if (dbClass == MySQL.class) {
                    connection = MySQL.getConnection();
                } else if (dbClass == SQLite.class) {
                    connection = SQLite.connect();
                } else {
                    throw new UnsupportedOperationException("Database class not supported: " + dbClass.getName());
                }

                if (connection != null) {
                    // Check if user already exists
                    try (PreparedStatement statement = connection
                            .prepareStatement("SELECT * FROM " + TABLE + " WHERE UserName = ?")) {
                        statement.setString(1, userName);
                        try (ResultSet existsResult = statement.executeQuery()) {
                            return existsResult.next();
                        }
                    }
                }
            } catch (SQLException ex) {
                String errorMessage = dbClass == MySQL.class ? ErrorCode.ERROR_MYSQL_DATABASE.getError()
                        : ErrorCode.ERROR_SQLITE_DATABASE.getError();
                Main.getLogger().log(Level.ERROR, errorMessage, ex);
                throw ex; // Consider rethrowing the exception after logging
            } finally {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
        } else {
            if (backendMongoDBManager.existsCollection(TABLE))
                return backendMongoDBManager.exists("userName", userName, TABLE);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public byte[] retrieveBinaryData(String collectionName, String queryKey, Object queryValue, String selectedKey) {
        MongoCollection<Document> collection = mongoDBManager.getDatabase().getCollection(collectionName);
        Document query = new Document(queryKey, queryValue);
        Document document = collection.find(query).first();
        if (document != null) {
            ArrayList<Integer> asArrayList = (ArrayList<Integer>) document.get(selectedKey);
            byte[] password = new byte[asArrayList.size()];
            for (int i = 0; i < asArrayList.size(); i++) {
                password[i] = Byte.parseByte(String.valueOf(asArrayList.get(i)));
            }
            return password;
        }

        return null; // Handle case where no document or binary field is found
    }

    public boolean isUserRight(String userName, String password) {
        byte[] fromDataBase = null;
        if (isSQLDatabase()) {
            Class<?> dbClass = getDatabaseClass();

            DatabaseHelper dbHelper;
            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (!dbHelper.isTableExists(TABLE)) {
                    return false;
                }

                if (!dbHelper.exists(TABLE, "UserName", userName)) {
                    return false;
                }

                fromDataBase = (byte[]) dbHelper.get(TABLE, "Password", "UserName", userName);
                if (fromDataBase == null) {
                    return false;
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Database error during user authentication", e);
                return false;
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                fromDataBase = retrieveBinaryData(TABLE, "userName", userName, "password");
                if (fromDataBase == null)
                    return false;
            }
        }

        return new PasswordHasher().verifyPassword(password, fromDataBase);
    }

    public byte[] getPassword(String userName) {
        // Remove single quotes for basic sanitization (though prepared statements are
        // the real defense)
        userName = userName.replace("'", "");

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    if (dbHelper.get(TABLE, "Used", "UserName", userName) != null) {
                        return (byte[]) dbHelper.get(TABLE, "Password", "UserName", userName);
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Error retrieving password for user: " + userName, e);
            }
        } else {
            return retrieveBinaryData(TABLE, "userName", userName, "password");
        }

        return null;
    }

    public void resetPassword(String userName, String password) {
        // Remove single quotes for basic sanitization (though prepared statements are
        // the real defense)
        userName = userName.replace("'", "");

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement("UPDATE " + TABLE + " SET password=? WHERE UserName=?;")) {

                        preparedStatement.setBytes(1, new PasswordHasher().hashPassword(password));
                        preparedStatement.setString(2, userName);
                        preparedStatement.execute();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to reset password for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                byte[] passwordBytes = new PasswordHasher().hashPassword(password);
                backendMongoDBManager.updateData("userName", userName, "password", passwordBytes, TABLE);
            }
        }
    }

    public void setUsed(String userName, int used) {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement("UPDATE " + TABLE + " SET Used = ? WHERE UserName = ?")) {

                        preparedStatement.setInt(1, used);
                        preparedStatement.setString(2, userName);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to update 'Used' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "used", used, TABLE);
            }
        }
    }

    public int getUsed(String userName) {
        // Basic sanitization, though prepared statements should also handle this
        userName = userName.replace("'", "");

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    Object usedValue = dbHelper.get(TABLE, "Used", "UserName", userName);
                    if (usedValue != null) {
                        return (int) usedValue;
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to retrieve 'Used' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object usedValue = backendMongoDBManager.getObject("userName", userName, "used", TABLE);
                if (usedValue != null) {
                    return (int) usedValue;
                }
            }
        }

        return 0;
    }

    public void addUsed(String userName, int amount) {
        int used = getUsed(userName);
        used += amount;
        setUsed(userName, used);
    }

    public void setMapOpened(String userName, int mapOpened) {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement("UPDATE " + TABLE + " SET MapOpened = ? WHERE UserName = ?")) {

                        preparedStatement.setInt(1, mapOpened);
                        preparedStatement.setString(2, userName);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to update 'MapOpened' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "mapOpened", mapOpened, TABLE);
            }
        }
    }

    public int getMapOpened(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization (though prepared statements handle this better)

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    Object mapOpenedValue = dbHelper.get(TABLE, "MapOpened", "UserName", userName);
                    if (mapOpenedValue != null) {
                        return (int) mapOpenedValue;
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to retrieve 'MapOpened' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object mapOpenedValue = backendMongoDBManager.getObject("userName", userName, "mapOpened", TABLE);
                if (mapOpenedValue != null) {
                    return (int) mapOpenedValue;
                }
            }
        }

        return 0;
    }

    public void addMapOpened(String userName, int amount) {
        int opened = getMapOpened(userName);
        opened += amount;
        setMapOpened(userName, opened);
    }

    public void setFilesDownloaded(String userName, int filesDownloaded) {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement(
                                            "UPDATE " + TABLE + " SET FilesDownloaded = ? WHERE UserName = ?")) {

                        preparedStatement.setInt(1, filesDownloaded);
                        preparedStatement.setString(2, userName);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to update 'FilesDownloaded' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "filesDownloaded", filesDownloaded, TABLE);
            }
        }
    }

    public int getFilesDownloaded(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization, though prepared statements handle this better

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    Object filesDownloadedValue = dbHelper.get(TABLE, "FilesDownloaded", "UserName", userName);
                    if (filesDownloadedValue != null) {
                        return (int) filesDownloadedValue;
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to retrieve 'FilesDownloaded' value for user: " + userName,
                        e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object filesDownloadedValue = backendMongoDBManager.getObject("userName", userName, "filesDownloaded",
                        TABLE);
                if (filesDownloadedValue != null) {
                    return (int) filesDownloadedValue;
                }
            }
        }

        return 0;
    }

    public void addFilesDownloaded(String userName, int amount) {
        int downloaded = getFilesDownloaded(userName);
        downloaded += amount;
        setFilesDownloaded(userName, downloaded);
    }

    public void setIcaos(String userName, List<String> icaos) {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            String icaosJson = new Gson().toJson(icaos);

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement("UPDATE " + TABLE + " SET ICAOS = ? WHERE UserName = ?")) {

                        preparedStatement.setString(1, icaosJson);
                        preparedStatement.setString(2, userName);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to update ICAOS for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "ICAOS", icaos, TABLE);
            }
        }
    }

    public List<String> getIcaos(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization, though prepared statements handle this better

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    String icaosJson = (String) dbHelper.get(TABLE, "ICAOS", "UserName", userName);
                    if (icaosJson != null) {
                        Type type = new TypeToken<List<String>>() {
                        }.getType();
                        return new Gson().fromJson(icaosJson, type);
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to retrieve ICAOS for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object icaosValue = backendMongoDBManager.getObject("userName", userName, "ICAOS", TABLE);
                if (icaosValue != null) {
                    Type type = new TypeToken<List<String>>() {
                    }.getType();
                    return new Gson().fromJson(icaosValue.toString(), type);
                }
            }
        }

        return new ArrayList<>();

    }

    public void addToIcaos(String userName, String icao) {
        try {
            List<String> icaos = getIcaos(userName);
            if (!icaos.contains(icao))
                icaos.add(icao);
            setIcaos(userName, icaos);
        } catch (Exception ex) {
            List<String> icaos = new ArrayList<>();
            if (!icaos.contains(icao))
                icaos.add(icao);
            setIcaos(userName, icaos);
        }
    }

    public void removeFromIcaos(String userName, String icao) {
        List<String> icaos = getIcaos(userName);
        icaos.remove(icao);
        setIcaos(userName, icaos);
    }

    public void setLastUsed(String userName, String date) {
        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    try (Connection connection = dbHelper.getConnection();
                            PreparedStatement preparedStatement = connection
                                    .prepareStatement("UPDATE " + TABLE + " SET LastUsed = ? WHERE UserName = ?")) {

                        preparedStatement.setString(1, date);
                        preparedStatement.setString(2, userName);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to update LastUsed for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "lastUsed", date, TABLE);
            }
        }
    }

    public String getLastUsed(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization, though prepared statements handle this better

        if (isSQLDatabase()) {
            createTableIfNotExists();

            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try {
                if (dbHelper.exists(TABLE, "UserName", userName)) {
                    return (String) dbHelper.get(TABLE, "LastUsed", "UserName", userName);
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Failed to retrieve LastUsed for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                return (String) backendMongoDBManager.getObject("userName", userName, "lastUsed", TABLE);
            }
        }

        return null;
    }

    public List<String> getAllUserNames() {
        List<String> users = new ArrayList<>();
        if (isSQLDatabase()) {
            Class<?> dbClass = getDatabaseClass();
            DatabaseHelper dbHelper;

            if (dbClass == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (dbClass == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }

            try (Connection connection = dbHelper.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet res = statement.executeQuery("SELECT UserName FROM " + TABLE)) {

                while (res.next()) {
                    users.add(res.getString("UserName").replace("'", ""));
                }
            } catch (SQLException e) {
                Main.getLogger().log(Level.ERROR, "Error retrieving user names", e);
                throw new RuntimeException(e);
            }
        } else {
            List<Document> documents = backendMongoDBManager.getAllDocuments(TABLE);
            for (Document doc : documents) {
                users.add(doc.getString("userName"));
            }
        }

        return users;
    }

    public Object getID(String userName) {
        if (isSQLDatabase()) {
            createTableIfNotExists();
            if (getDatabaseClass() == MySQL.class) {
                if (MySQL.exists(TABLE, "UserName", userName))
                    return (int) MySQL.get(TABLE, "ID", "UserName", userName);
            } else if (getDatabaseClass() == SQLite.class) {
                if (SQLite.exists(TABLE, "UserName", userName))
                    return (int) SQLite.get(TABLE, "ID", "UserName", userName);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                return backendMongoDBManager.getObject("userName", userName, "_id", TABLE);
            }
        }
        return 0;
    }

    public void changeValue(String userName, String where, String data) {
        createTableIfNotExists();
        if (getAllUserNames().contains(userName))
            MySQL.updateData(TABLE, where, "'" + data + "'", "UserName='" + userName + "'");
    }

    public void deleteUser(String user) {
        if (isSQLDatabase()) {
            createTableIfNotExists();
            if (getDatabaseClass() == MySQL.class) {
                try {
                    if (getDatabaseHelper().exists(TABLE, "UserName", user)) {
                        MySQL.deleteDataInTable(TABLE, "UserName='" + user + "'");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (getDatabaseClass() == SQLite.class) {
                try {
                    if (getDatabaseHelper().exists(TABLE, "UserName", user)) {
                        SQLite.deleteDataInTable(TABLE, "UserName='" + user + "'");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (backendMongoDBManager.exists("userName", user, TABLE)) {
                backendMongoDBManager.removeDocument("userName", user, TABLE);
            }
        }
    }

    public boolean createData(String userName) {
        if (isSQLDatabase()) {
            createTableIfNotExistsUtilities();
            if (getDatabaseClass() == MySQL.class) {
                try (Connection connection = MySQL.getConnection();
                        PreparedStatement selectStatement = connection
                                .prepareStatement("SELECT * FROM " + UTILITIES_TABLE + " WHERE UserName = ?")) {

                    selectStatement.setString(1, userName);
                    try (ResultSet existsResult = selectStatement.executeQuery()) {
                        if (!existsResult.next()) { // Insert only if the user does not exist
                            try (PreparedStatement preparedStatement = connection.prepareStatement(
                                    "INSERT INTO " + UTILITIES_TABLE
                                            + " (UserName, Online, Version, HasUpdate, LastUpdated) VALUES (?, ?, ?, ?, ?)")) {

                                preparedStatement.setString(1, userName);
                                preparedStatement.setString(2, "false");
                                preparedStatement.setString(3, "1.0.0");
                                preparedStatement.setString(4, "false");
                                preparedStatement.setString(5, "Not Set");

                                int rowsAffected = preparedStatement.executeUpdate();
                                return rowsAffected > 0; // Return true if one or more rows were affected
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Better error handling/logging
                }
            } else if (getDatabaseClass() == SQLite.class) {
                try (Connection connection = SQLite.connect();
                        PreparedStatement selectStatement = connection
                                .prepareStatement("SELECT * FROM " + UTILITIES_TABLE + " WHERE UserName = ?")) {

                    selectStatement.setString(1, userName);
                    try (ResultSet existsResult = selectStatement.executeQuery()) {
                        if (!existsResult.next()) { // Insert only if the user does not exist
                            try (PreparedStatement preparedStatement = connection.prepareStatement(
                                    "INSERT INTO " + UTILITIES_TABLE
                                            + " (UserName, Online, Version, HasUpdate, LastUpdated) VALUES (?, ?, ?, ?, ?)")) {

                                preparedStatement.setString(1, userName);
                                preparedStatement.setString(2, "false");
                                preparedStatement.setString(3, "1.0.0");
                                preparedStatement.setString(4, "false");
                                preparedStatement.setString(5, "Not Set");

                                int rowsAffected = preparedStatement.executeUpdate();
                                return rowsAffected > 0; // Return true if one or more rows were affected
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Better error handling/logging
                }
            }
        } else {
            if (!backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE)) {
                Map<String, Object> map = new HashMap<>();
                map.put("userName", userName);
                map.put("Online", false);
                map.put("Version", "1.0.0");
                map.put("HasUpdate", false);
                map.put("LastUpdated", "Not Set");
                backendMongoDBManager.createData("userName", userName, map, UTILITIES_TABLE);
            }
        }

        return false; // Return false if insertion didn't happen or an exception occurred
    }

    public void setVersion(String userName, String version) {
        createData(userName);
        if (isSQLDatabase()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateData(UTILITIES_TABLE, "Version", version, "UserName='" + userName + "'");
            } else if (getDatabaseClass() == SQLite.class) {
                SQLite.updateData(UTILITIES_TABLE, "Version", version, "UserName='" + userName + "'");
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "Version", version, UTILITIES_TABLE);
        }
    }

    public String getVersion(String userName) {
        if (isSQLDatabase()) {
            DatabaseHelper dbHelper;

            if (getDatabaseClass() == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (getDatabaseClass() == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + getDatabaseClass().getName());
            }
            try {
                return (String) dbHelper.get(UTILITIES_TABLE, "Version", "UserName", userName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return backendMongoDBManager.getDocument("userName", userName, UTILITIES_TABLE).get("Version").toString();
        }
    }

    public void setOnline(String userName, boolean online) {
        createData(userName);
        if (isSQLDatabase()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateData(UTILITIES_TABLE, "Online", "" + online, "UserName='" + userName + "'");
            } else if (getDatabaseClass() == SQLite.class) {
                SQLite.updateData(UTILITIES_TABLE, "Online", "" + online, "UserName='" + userName + "'");
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "Online", online, UTILITIES_TABLE);
        }
    }

    public boolean isOnline(String userName) {
        if (isSQLDatabase()) {
            DatabaseHelper dbHelper;

            if (getDatabaseClass() == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (getDatabaseClass() == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + getDatabaseClass().getName());
            }
            try {
                return Boolean.parseBoolean((String) dbHelper.get(UTILITIES_TABLE, "Online", "UserName", userName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                return (boolean) backendMongoDBManager.getObject("userName", userName, "Online", UTILITIES_TABLE);
        }
        return false;
    }

    public void setHasUpdate(String userName, boolean hasUpdate) {
        createData(userName);
        if (isSQLDatabase()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateData(UTILITIES_TABLE, "HasUpdate", "" + hasUpdate, "UserName='" + userName + "'");
            } else if (getDatabaseClass() == SQLite.class) {
                SQLite.updateData(UTILITIES_TABLE, "HasUpdate", String.valueOf(hasUpdate),
                        "UserName='" + userName + "'");
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "HasUpdate", hasUpdate, UTILITIES_TABLE);
        }
    }

    public boolean hasUpdate(String userName) {
        if (isSQLDatabase()) {
            DatabaseHelper dbHelper;

            if (getDatabaseClass() == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (getDatabaseClass() == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + getDatabaseClass().getName());
            }
            try {
                return Boolean.parseBoolean((String) dbHelper.get(UTILITIES_TABLE, "HasUpdate", "UserName", userName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                return (boolean) backendMongoDBManager.getObject("userName", userName, "HasUpdate", UTILITIES_TABLE);
        }
        return false;
    }

    public void setLastUpdated(String userName, String date) {
        date = date.replace("'", "");
        createData(userName);
        if (isSQLDatabase()) {
            if (getDatabaseClass() == MySQL.class)
                MySQL.updateData(UTILITIES_TABLE, "LastUpdated", date, "UserName='" + userName + "'");
            else if (getDatabaseClass() == SQLite.class)
                SQLite.updateData(UTILITIES_TABLE, "LastUpdated", date, "UserName='" + userName + "'");
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "LastUpdated", date, UTILITIES_TABLE);
        }
    }

    public String getLastUpdated(String userName) {
        if (isSQLDatabase()) {
            DatabaseHelper dbHelper;

            if (getDatabaseClass() == MySQL.class) {
                dbHelper = new MySQLDatabaseHelper();
            } else if (getDatabaseClass() == SQLite.class) {
                dbHelper = new SQLiteDatabaseHelper();
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + getDatabaseClass().getName());
            }
            try {
                return (String) dbHelper.get(UTILITIES_TABLE, "LastUpdated", "UserName", userName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                return backendMongoDBManager.getDocument("userName", userName, UTILITIES_TABLE).get("LastUpdated")
                        .toString();
        }
        return null;
    }

    public boolean changePassword(String userName, String oldPassword, String newPassword) {
        byte[] oldPw = null;
        if (isSQLDatabase()) {
            if (getDatabaseClass() == MySQL.class) {
                try {
                    ResultSet resultSet;
                    try (PreparedStatement statement = MySQL.getConnection()
                            .prepareStatement("SELECT * FROM accounts WHERE password=?")) {
                        statement.setBytes(1, new PasswordHasher().hashPassword(oldPassword));
                        resultSet = statement.executeQuery();
                    }
                    if (resultSet.next()) {
                        oldPw = resultSet.getBytes("password");
                    }
                    try (PreparedStatement preparedStatement = MySQL.getConnection()
                            .prepareStatement("UPDATE accounts SET password=? WHERE username=?")) {
                        if (oldPw != null) {
                            preparedStatement.setString(2, userName);
                            preparedStatement.setBytes(1, new PasswordHasher().hashPassword(newPassword));
                            preparedStatement.execute();
                            return true;
                        } else {
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    Main.getLogger().log(Level.ERROR, "Failed to retrieve data", ex);
                    return false;
                }
            } else if (getDatabaseClass() == SQLite.class) {
                try {
                    ResultSet resultSet;
                    try (PreparedStatement statement = SQLite.connect()
                            .prepareStatement("SELECT * FROM accounts WHERE password=?")) {
                        statement.setBytes(1, new PasswordHasher().hashPassword(oldPassword));
                        resultSet = statement.executeQuery();
                    }
                    if (resultSet.next()) {
                        oldPw = resultSet.getBytes("password");
                    }
                    try (PreparedStatement preparedStatement = SQLite.connect()
                            .prepareStatement("UPDATE accounts SET password=? WHERE username=?")) {
                        if (oldPw != null) {
                            preparedStatement.setString(2, userName);
                            preparedStatement.setBytes(1, new PasswordHasher().hashPassword(newPassword));
                            preparedStatement.execute();
                            return true;
                        } else {
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    Main.getLogger().log(Level.ERROR, "Failed to retrieve data", ex);
                    return false;
                }
            }
        } else {
            if (backendMongoDBManager.exists("username", userName, TABLE)) {
                oldPw = retrieveBinaryData(TABLE, "userName", userName, "password");
                if (oldPw != null) {
                    backendMongoDBManager.updateData("userName", userName, "password",
                            new PasswordHasher().hashPassword(newPassword), TABLE);
                    return true;
                }
            }
        }
        return false;
    }
}