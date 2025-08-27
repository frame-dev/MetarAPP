package ch.framedev.metarapp.database;

import ch.framedev.javamongodbutils.BackendMongoDBManager;
import ch.framedev.javamongodbutils.MongoDBManager;
import ch.framedev.javamysqlutils.JsonConnection;
import ch.framedev.javamysqlutils.MySQL;
import ch.framedev.metarapp.data.MySQLData;
import ch.framedev.metarapp.data.UserData;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.Variables;
import ch.framedev.simplejavautils.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import org.apache.log4j.Level;
import org.bson.Document;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ch.framedev.metarapp.main.Main.*;
import static ch.framedev.metarapp.util.Setting.*;

public class Database {

    private final String TABLE = "accounts";
    private final String UTILITIES_TABLE = "utilities";
    private MongoDBManager mongoDBManager;
    private BackendMongoDBManager backendMongoDBManager;

    public Database() {
        Class<?> dbClass = getDatabaseClass();

        if (dbClass == MySQL.class) {
            String host, user, password, database;
            int port;
            if (!(boolean) settings.get(OWN_MYSQL_DATABASE.getKey())) {
                host = Variables.MYSQL_HOST;
                user = Variables.MYSQL_USER;
                password = Variables.MYSQL_PASSWORD;
                database = Variables.MYSQL_DATABASE;
                port = Variables.MYSQL_PORT;
            } else {
                host = (String) settings.get(MYSQL_HOST.getKey());
                user = (String) settings.get(MYSQL_USERNAME.getKey());
                password = (String) settings.get(MYSQL_PASSWORD.getKey());
                database = (String) settings.get(MYSQL_DATABASE.getKey());
                port = (int) settings.get(MYSQL_PORT.getKey());
            }
            new MySQL(new JsonConnection(host, user, password, database, port));
            MySQL.setAllowPublicKey(true);
            String message = localeUtils.getString(
                    (boolean) settings.get(OWN_MYSQL_DATABASE.getKey()) ? "connectedToDatabasePort" : "connectedToDatabase",
                    "%HOST%", host
            );
            message = message.replace("%DATABASE%", "MySQL").replace("%PORT%", String.valueOf(port));
            getLogger().log(Level.INFO, message);
        } else if (dbClass == SQLite.class) {
            new SQLite(Variables.FILES_DIRECTORY + "/" + settings.getString(SQLITE_PATH.getKey()), settings.getString(SQLITE_DATABASE.getKey()));
            String message = localeUtils.getString("connectedToSQLite");
            getLogger().log(Level.INFO, message);
        } else if (dbClass == MongoDBManager.class) {
            String user = settings.getString(MONGODB_USER.getKey());
            String password = settings.getString(MONGODB_PASSWORD.getKey());
            String host = settings.getString(MONGODB_HOST.getKey());
            int port = settings.getInt(MONGODB_PORT.getKey());
            String database = settings.getString(MONGODB_DATABASE.getKey());
            String message = localeUtils.getString("connectedToDatabasePort", "%HOST%", host)
                    .replace("%PORT%", String.valueOf(port))
                    .replace("%DATABASE%", "MongoDB");
            mongoDBManager = new MongoDBManager(host, user, password, port, database);
            mongoDBManager.connect();
            backendMongoDBManager = new BackendMongoDBManager(mongoDBManager);
            getLogger().log(Level.INFO, message);
        } else if(settings.getString("database").equalsIgnoreCase("file")) {
            getLogger().info("Using File Database");
        }
    }

    public Class<?> getDatabaseClass() {
        String databaseType = (String) settings.get("database");
        switch (databaseType.toLowerCase()) {
            case "mysql":
            case "mysql-use-own":
                return MySQL.class;
            case "sqlite":
                return SQLite.class;
            case "mongodb":
                return MongoDBManager.class;
            default:
                throw new IllegalArgumentException("Unsupported Database Type: " + databaseType);
        }
    }

    public boolean isMySQL() {
        return getDatabaseClass() == MySQL.class;
    }

    public boolean isSQLite() {
        return getDatabaseClass() == SQLite.class;
    }

    public boolean isMongoDB() {
        return getDatabaseClass() == MongoDBManager.class;
    }

    public DatabaseHelper getDatabaseHelper() {
        if (getDatabaseClass() == MySQL.class)
            return new MySQLDatabaseHelper();
        else if (getDatabaseClass() == SQLite.class)
            return new SQLiteDatabaseHelper();
        else
            throw new UnsupportedOperationException("Unsupported Database Type: " + getDatabaseClass().getName());
    }

    public boolean isMySQLOrSQLite() {
        return getDatabaseClass() == MySQL.class || getDatabaseClass() == SQLite.class;
    }

    public void createTableIfNotExistsUtilities() {
        Class<?> dbClass = getDatabaseClass();
        String[] columns = {"UserName TEXT(255)",
                "Online TEXT",
                "Version TEXT(255)",
                "HasUpdate TEXT",
                "LastUpdated VARCHAR(2666)"};

        if (dbClass == MySQL.class || dbClass == SQLite.class) {
            DatabaseHelper databaseHelper = getDatabaseHelper();
            try {
                databaseHelper.isTableExists(UTILITIES_TABLE, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (!result) {
                            MySQL.createTableAsync(UTILITIES_TABLE, new MySQL.Callback<Boolean>() {
                                @Override
                                public void onResult(Boolean aBoolean) {
                                    getLogger().log(Level.INFO, "Utilities Table Created Successfully");
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    loggerUtils.addLog("Error : " + throwable.getMessage());
                                    getLogger().error("Error : " + throwable.getMessage(), throwable);
                                }
                            }, columns);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
        }
    }

    public void createAdminAccount() {
        createTableIfNotExists();
        try {
            createAccount("admin",
                    new PasswordHasher().hashPassword(EncryptionUtil.decrypt(connectionTokenHandler.getProperty("admin-password"))));
        } catch (Exception e) {
            getLogger().log(Level.ERROR, "Cannot create Admin Account", e);
            loggerUtils.addLog("Cannot create Admin Account : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createTableIfNotExists() {
        if (isMySQLOrSQLite()) {
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
                MySQL.isTableExistsAsync(TABLE, new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (!aBoolean) {
                            MySQL.createTableAsync(TABLE, new MySQL.Callback<Boolean>() {
                                @Override
                                public void onResult(Boolean aBoolean) {
                                    getLogger().log(Level.INFO, "Accounts Table Created Successfully");
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    loggerUtils.addLog("Error : " + throwable.getMessage());
                                    getLogger().error("Error : " + throwable.getMessage(), throwable);
                                }
                            }, columns);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } else if (dbClass == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.isTableExists(TABLE, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            if (!result) {
                                try {
                                    databaseHelper.createTable(TABLE, columns, new Callback<Boolean>() {
                                        @Override
                                        public void onResult(Boolean aBoolean) {
                                            getLogger().log(Level.INFO, "Accounts Table Created Successfully");
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            loggerUtils.addLog("Error : " + throwable.getMessage());
                                            getLogger().error("Error : " + throwable.getMessage(), throwable);
                                        }
                                    });
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported Database Type: " + dbClass.getName());
            }
        }
    }

    public boolean createAccount(String userName, byte[] hashedPassword) throws SQLException {
        userName = userName.toLowerCase();
        if (isMySQLOrSQLite()) {
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
                    try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE + " WHERE UserName = ?")) {
                        statement.setString(1, userName);
                        try (ResultSet existsResult = statement.executeQuery()) {
                            if (!existsResult.next()) {
                                // Insert the new user
                                try (PreparedStatement createUserPs = connection.prepareStatement("INSERT INTO " + TABLE + " (UserName,Password) VALUES (?,?)")) {
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
                String errorMessage = dbClass == MySQL.class ? ErrorCode.ERROR_MYSQL_DATABASE.getError() : ErrorCode.ERROR_SQLITE_DATABASE.getError();
                loggerUtils.addLog(errorMessage);
                getLogger().log(Level.ERROR, errorMessage, ex);
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
        userName = userName.toLowerCase();
        if (isMySQLOrSQLite()) {
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
                    try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE + " WHERE UserName = ?")) {
                        statement.setString(1, userName);
                        try (ResultSet existsResult = statement.executeQuery()) {
                            return existsResult.next();
                        }
                    }
                }
            } catch (SQLException ex) {
                String errorMessage = dbClass == MySQL.class ? ErrorCode.ERROR_MYSQL_DATABASE.getError() : ErrorCode.ERROR_SQLITE_DATABASE.getError();
                loggerUtils.addLog(errorMessage);
                getLogger().log(Level.ERROR, errorMessage, ex);
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

        return null;  // Handle case where no document or binary field is found
    }

    public CompletableFuture<Boolean> isUserRight(String userName, String password) {
        userName = userName.toLowerCase();

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                String finalUserName1 = userName;
                dbHelper.isTableExists(TABLE, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean tableExists) {
                        if (!tableExists) {
                            future.complete(false);
                            return;
                        }

                        try {
                            dbHelper.exists(TABLE, "UserName", finalUserName, new Callback<Boolean>() {
                                @Override
                                public void onResult(Boolean userExists) {
                                    if (!userExists) {
                                        future.complete(false);
                                        return;
                                    }

                                    try {
                                        dbHelper.get(TABLE, "Password", "UserName", finalUserName1, new Callback<Object>() {
                                            @Override
                                            public void onResult(Object result) {
                                                byte[] hashedPassword = (byte[]) result;
                                                boolean isValid = new PasswordHasher().verifyPassword(password, hashedPassword);
                                                future.complete(isValid);
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                logAndCompleteExceptionally(throwable, future);
                                            }
                                        });
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    logAndCompleteExceptionally(throwable, future);
                                }
                            });
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logAndCompleteExceptionally(throwable, future);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Database error during user authentication", e);
                future.completeExceptionally(e);
            }
        } else {
            // MongoDB logic
            try {
                String finalUserName2 = userName;
                backendMongoDBManager.existsAsync("userName", userName, TABLE, new ch.framedev.javamongodbutils.Callback<>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            byte[] storedPassword = retrieveBinaryData(TABLE, "userName", finalUserName2, "password");
                            boolean isValid = storedPassword != null && new PasswordHasher().verifyPassword(password, storedPassword);
                            future.complete(isValid);
                        } else {
                            future.complete(false);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                logAndCompleteExceptionally(ex, future);
            }
        }

        return future;
    }

    private void logAndCompleteExceptionally(Throwable throwable, CompletableFuture<?> future) {
        loggerUtils.addLog("Error: " + throwable.getMessage());
        getLogger().error("Error: " + throwable.getMessage(), throwable);
        future.completeExceptionally(throwable);
    }

    public CompletableFuture<byte[]> getPassword(String userName) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        // Validate input
        if (userName == null || userName.trim().isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Username cannot be null or empty"));
            return future;
        }

        if (isMySQLOrSQLite()) {
            try {
                createTableIfNotExists(); // Ensure table exists
                DatabaseHelper dbHelper = getDatabaseHelper();

                // Check if the user exists
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean userExists) {
                        if (Boolean.TRUE.equals(userExists)) {
                            // Retrieve the password
                            try {
                                dbHelper.get(TABLE, "Password", "UserName", userName, byte[].class, new Callback<byte[]>() {
                                    @Override
                                    public void onResult(byte[] password) {
                                        if (password != null) {
                                            future.complete(password);
                                        } else {
                                            future.completeExceptionally(new IllegalStateException(
                                                    "Password for user '" + userName + "' is null."));
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        logError("Error retrieving password for user: " + userName, throwable);
                                        future.completeExceptionally(throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            future.completeExceptionally(
                                    new IllegalStateException("User '" + userName + "' does not exist."));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logError("Error checking if user exists: " + userName, throwable);
                        future.completeExceptionally(throwable);
                    }
                });

            } catch (SQLException e) {
                logError("Database error when retrieving password for user: " + userName, e);
                future.completeExceptionally(e);
            }
        } else {
            try {
                byte[] data = retrieveBinaryData(TABLE, "userName", userName, "password");
                future.complete(data);
            } catch (Exception e) {
                logError("Error retrieving fallback password data for user: " + userName, e);
                future.completeExceptionally(e);
            }
        }

        return future;
    }

    private void logError(String message, Throwable throwable) {
        loggerUtils.addLog(message + " : " + throwable.getMessage());
        getLogger().error(message, throwable);
    }

    public CompletableFuture<Boolean> resetPassword(String userName, String password) {
        // Remove single quotes for basic sanitization (though prepared statements are the real defense)
        userName = userName.replace("'", "");
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.update(TABLE, "Password", new PasswordHasher().hashPassword(password), "UserName", finalUserName, new Callback<>() {
                                    @Override
                                    public void onResult(Boolean result) {
                                        future.complete(result);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                        future.completeExceptionally(throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                loggerUtils.addLog("Error : " + e.getMessage());
                                getLogger().error("Error : " + e.getMessage(), e);
                                future.completeExceptionally(e);
                            }
                        } else {
                            future.complete(false);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to reset password for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            String finalUserName1 = userName;
            backendMongoDBManager.existsAsync("userName", userName, TABLE, new ch.framedev.javamongodbutils.Callback<>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    if (aBoolean) {
                        byte[] passwordBytes = new PasswordHasher().hashPassword(password);
                        backendMongoDBManager.updateDataAsync("userName", finalUserName1, "password", passwordBytes, TABLE, new ch.framedev.javamongodbutils.Callback<Void>() {
                            @Override
                            public void onResult(Void unused) {
                                future.complete(true);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
        return future;
    }

    public void setUsed(String userName, int used) {

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.update(TABLE, "Used", used, "UserName", userName, new Callback<Boolean>() {
                                    @Override
                                    public void onResult(Boolean result) {
                                        // no-op
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                loggerUtils.addLog("Error : " + e.getMessage());
                                getLogger().error("Error : " + e.getMessage(), e);
                            }
                        } else {
                            try {
                                Connection connection = dbHelper.getConnection();
                                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (UserName, Used) VALUES (?,?);")) {
                                    statement.setObject(1, userName);
                                    statement.setInt(2, used);
                                    statement.executeUpdate();
                                }
                            } catch (Exception ex) {
                                getLogger().log(Level.ERROR, "Failed to update 'Used' value for user: " + userName, ex);
                                throw new RuntimeException(ex);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to update 'Used' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "used", used, TABLE);
            }
        }
    }

    public CompletableFuture<Integer> getUsed(String userName) {
        // Basic sanitization, though prepared statements should also handle this
        userName = userName.replace("'", "");
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.get(TABLE, "Used", "UserName", finalUserName, new Callback<>() {
                                    @Override
                                    public void onResult(Object result) {
                                        future.complete((int) Objects.requireNonNullElse(result, 0));
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to retrieve 'Used' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object usedValue = backendMongoDBManager.getObject("userName", userName, "used", TABLE);
                if (usedValue != null) {
                    future.complete((int) usedValue);
                }
            }
        }
        return future;
    }

    public void addUsed(String userName, int amount) {
        createTableIfNotExists();
        getUsed(userName).thenAccept(used -> {
            used += amount;
            setUsed(userName, used);
        }).exceptionally(throwable -> {
            loggerUtils.addLog("Error : " + throwable.getMessage());
            getLogger().error("Error : " + throwable.getMessage(), throwable);
            return null;
        });
    }

    public void setMapOpened(String userName, int mapOpened) {

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        try (Connection connection = dbHelper.getConnection();
                             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + TABLE + " SET MapOpened = ? WHERE UserName = ?")) {

                            preparedStatement.setInt(1, mapOpened);
                            preparedStatement.setString(2, userName);
                            preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            loggerUtils.addLog("Error : " + e.getMessage());
                            getLogger().error("Error : " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to update 'MapOpened' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "mapOpened", mapOpened, TABLE);
            }
        }
    }

    public CompletableFuture<Integer> getMapOpened(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization (though prepared statements handle this better)
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.get(TABLE, "MapOpened", "UserName", finalUserName, new Callback<Object>() {
                                    @Override
                                    public void onResult(Object result) {
                                        if (result == null)
                                            future.complete(0);
                                        else
                                            future.complete((int) result);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to retrieve 'MapOpened' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object mapOpenedValue = backendMongoDBManager.getObject("userName", userName, "mapOpened", TABLE);
                if (mapOpenedValue != null) {
                    future.complete((Integer) mapOpenedValue);
                }
            }
        }
        return future;
    }

    public void addMapOpened(String userName, int amount) {
        getMapOpened(userName).thenAccept(opened -> {
            opened += amount;
            setMapOpened(userName, opened);
        }).exceptionally(throwable -> {
            loggerUtils.addLog("Error : " + throwable.getMessage());
            getLogger().error("Error : " + throwable.getMessage(), throwable);
            return null;
        });
    }

    public void setFilesDownloaded(String userName, int filesDownloaded) {

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try (Connection connection = dbHelper.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + TABLE + " SET FilesDownloaded = ? WHERE UserName = ?")) {

                                preparedStatement.setInt(1, filesDownloaded);
                                preparedStatement.setString(2, userName);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to update 'FilesDownloaded' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "filesDownloaded", filesDownloaded, TABLE);
            }
        }
    }

    public CompletableFuture<Integer> getFilesDownloaded(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization, though prepared statements handle this better
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.get(TABLE, "FilesDownloaded", "UserName", finalUserName, new Callback<>() {
                                    @Override
                                    public void onResult(Object result) {
                                        future.complete((Integer) Objects.requireNonNullElse(result, 0));
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                loggerUtils.addLog("Error : " + e.getMessage());
                                getLogger().error("Error : " + e.getMessage(), e);
                                future.completeExceptionally(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to retrieve 'FilesDownloaded' value for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                Object filesDownloadedValue = backendMongoDBManager.getObject("userName", userName, "filesDownloaded", TABLE);
                if (filesDownloadedValue != null) {
                    future.complete((int) filesDownloadedValue);
                }
            }
        }
        return future;
    }

    public void addFilesDownloaded(String userName, int amount) {
        getFilesDownloaded(userName).thenAccept(downloaded -> {
            downloaded += amount;
            setFilesDownloaded(userName, downloaded);
        }).exceptionally(throwable -> {
            loggerUtils.addLog("Error : " + throwable.getMessage());
            getLogger().error("Error : " + throwable.getMessage(), throwable);
            return null;
        });
    }

    public void setIcaos(String userName, List<String> icaos) {
        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            String icaosJson = new Gson().toJson(icaos);

            try {
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try (Connection connection = dbHelper.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + TABLE + " SET ICAOS = ? WHERE UserName = ?")) {

                                preparedStatement.setString(1, icaosJson);
                                preparedStatement.setString(2, userName);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                loggerUtils.addLog("Error : " + e.getMessage());
                                getLogger().error("Error : " + e.getMessage(), e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to update ICAOS for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "ICAOS", icaos, TABLE);
            }
        }
    }

    public CompletableFuture<List<String>> getIcaos(String userName) {
        // Basic sanitization
        userName = userName.replace("'", "");
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();
            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean userExists) {
                        if (userExists) {
                            try {
                                dbHelper.get(TABLE, "ICAOS", "UserName", finalUserName, new Callback<Object>() {
                                    @Override
                                    public void onResult(Object result) {
                                        if (result != null) {
                                            Type type = new TypeToken<List<String>>() {
                                            }.getType();
                                            List<String> icaos = new Gson().fromJson((String) result, type);
                                            future.complete(icaos);
                                        } else {
                                            future.complete(Collections.emptyList());
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        logAndCompleteExceptionally(throwable, future);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            future.complete(Collections.emptyList());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logAndCompleteExceptionally(throwable, future);
                    }
                });
            } catch (SQLException e) {
                logAndCompleteExceptionally(e, future);
            }
        } else {
            // MongoDB logic
            try {
                if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                    Object icaosValue = backendMongoDBManager.getObject("userName", userName, "ICAOS", TABLE);
                    System.out.println("MongoDB ICAOS value for user " + userName + ": " + icaosValue);

                    if (icaosValue != null) {
                        try {
                            Type type = new TypeToken<List<String>>() {
                            }.getType();
                            List<String> icaosList = new Gson().fromJson(icaosValue.toString(), type);
                            System.out.println("Parsed ICAOS list: " + icaosList);
                            future.complete(icaosList);
                        } catch (Exception parseEx) {
                            System.err.println("Error parsing ICAOS JSON: " + parseEx.getMessage());
                            parseEx.printStackTrace();
                            future.complete(Collections.emptyList());
                        }
                    } else {
                        System.out.println("ICAOS value is null for user " + userName);
                        future.complete(Collections.emptyList());
                    }
                } else {
                    System.out.println("User does not exist in MongoDB for userName: " + userName);
                    future.complete(Collections.emptyList());
                }
            } catch (Exception e) {
                logAndCompleteExceptionally(e, future);
            }
        }

        return future;
    }

    public void addToIcaos(String userName, String icao) {
        getIcaos(userName).thenAccept(icaos -> {
            if (!icaos.contains(icao))
                icaos.add(icao);
            setIcaos(userName, icaos);
        }).exceptionally(throwable -> {
            List<String> icaos = new ArrayList<>();
            icaos.add(icao);
            setIcaos(userName, icaos);
            loggerUtils.addLog("Error : " + throwable.getMessage());
            getLogger().error("Error : " + throwable.getMessage(), throwable);

            return null;
        });
    }

    public void removeFromIcaos(String userName, String icao) {
        getIcaos(userName).thenAccept(icaos -> {
            if (!icaos.contains(icao))
                icaos.remove(icao);
            setIcaos(userName, icaos);
        }).exceptionally(throwable -> {
            loggerUtils.addLog("Error : " + throwable.getMessage());
            getLogger().error("Error : " + throwable.getMessage(), throwable);
            return null;
        });
    }

    public void setLastUsed(String userName, String date) {
        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try (Connection connection = dbHelper.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + TABLE + " SET LastUsed = ? WHERE UserName = ?")) {

                                preparedStatement.setString(1, date);
                                preparedStatement.setString(2, userName);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to update LastUsed for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                backendMongoDBManager.updateData("userName", userName, "lastUsed", date, TABLE);
            }
        }
    }

    public CompletableFuture<String> getLastUsed(String userName) {
        userName = userName.replace("'", ""); // Basic sanitization, though prepared statements handle this better
        CompletableFuture<String> future = new CompletableFuture<>();

        if (isMySQLOrSQLite()) {
            createTableIfNotExists();

            DatabaseHelper dbHelper = getDatabaseHelper();

            try {
                String finalUserName = userName;
                dbHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            try {
                                dbHelper.get(TABLE, "LastUsed", "UserName", finalUserName, String.class, new Callback<String>() {
                                    @Override
                                    public void onResult(String result) {
                                        future.complete(result);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                        future.completeExceptionally(throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Failed to retrieve LastUsed for user: " + userName, e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                future.complete((String) backendMongoDBManager.getObject("userName", userName, "lastUsed", TABLE));
            }
        }
        return future;
    }

    public List<String> getAllUserNames() {
        List<String> users = new ArrayList<>();
        if (isMySQLOrSQLite()) {

            DatabaseHelper dbHelper = getDatabaseHelper();

            try (Connection connection = dbHelper.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet res = statement.executeQuery("SELECT UserName FROM " + TABLE)) {

                while (res.next()) {
                    users.add(res.getString("UserName").replace("'", ""));
                }
            } catch (SQLException e) {
                getLogger().log(Level.ERROR, "Error retrieving user names", e);
                throw new RuntimeException(e);
            }
        } else {
            List<Document> documents = backendMongoDBManager.getAllDocuments(TABLE);
            documents.forEach(document -> {
                users.add(document.getString("userName"));
            });
        }

        return users;
    }

    public CompletableFuture<Integer> getID(String userName) {
        createTableIfNotExists();
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (getDatabaseClass() == MySQL.class || getDatabaseClass() == SQLite.class) {
            DatabaseHelper databaseHelper = getDatabaseHelper();
            try {
                databaseHelper.exists(TABLE, "UserName", userName, new Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean exists) {
                        if (exists) {
                            try {
                                databaseHelper.get(TABLE, "ID", "UserName", userName, new Callback<Object>() {
                                    @Override
                                    public void onResult(Object object) {
                                        future.complete((int) object);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        loggerUtils.addLog("Error : " + throwable.getMessage());
                                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                                        future.completeExceptionally(throwable);
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            future.complete(0); // User not found
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (getDatabaseClass() == MongoDBManager.class) {
            if (backendMongoDBManager.exists("userName", userName, TABLE)) {
                int id = (int) backendMongoDBManager.getObject("userName", userName, "id", TABLE);
                future.complete(id);
            }
            future.completeExceptionally(new IllegalStateException("Unknown database class"));
        }

        return future;
    }

    public void changeValue(String userName, String where, String data) {
        createTableIfNotExists();
        if (getAllUserNames().contains(userName))
            MySQL.updateDataAsync(TABLE, where, "'" + data + "'", "UserName='" + userName + "'", new MySQL.Callback<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    if (aBoolean)
                        System.out.println("Value changed successfully");
                }

                @Override
                public void onError(Throwable throwable) {
                    loggerUtils.addLog("Error : " + throwable.getMessage());
                    getLogger().error("Error : " + throwable.getMessage(), throwable);
                }
            });
    }

    public void deleteUser(String user) {
        if (isMySQLOrSQLite()) {
            createTableIfNotExists();
            if (getDatabaseClass() == MySQL.class) {
                MySQL.existsAsync(TABLE, "UserName", user, new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (aBoolean)
                            MySQL.deleteDataInTableAsync(TABLE, "UserName='" + user + "'", new MySQL.Callback<Boolean>() {
                                @Override
                                public void onResult(Boolean aBoolean) {
                                    if (aBoolean)
                                        System.out.println("User deleted successfully");
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    loggerUtils.addLog("Error : " + throwable.getMessage());
                                    getLogger().error("Error : " + throwable.getMessage(), throwable);
                                }
                            });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } else if (getDatabaseClass() == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.exists(TABLE, "UserName", user, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (backendMongoDBManager.exists("userName", user, TABLE)) {
                    backendMongoDBManager.removeDocument("userName", user, TABLE);
                }
            }
        }
    }

    public boolean createData(String userName) {
        if (isMySQLOrSQLite()) {
            createTableIfNotExistsUtilities();
            if (getDatabaseClass() == MySQL.class) {
                try (Connection connection = MySQL.getConnection();
                     PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM " + UTILITIES_TABLE + " WHERE UserName = ?")) {

                    selectStatement.setString(1, userName);
                    try (ResultSet existsResult = selectStatement.executeQuery()) {
                        if (!existsResult.next()) { // Insert only if the user does not exist
                            try (PreparedStatement preparedStatement = connection.prepareStatement(
                                    "INSERT INTO " + UTILITIES_TABLE + " (UserName, Online, Version, HasUpdate, LastUpdated) VALUES (?, ?, ?, ?, ?)")) {

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
                    loggerUtils.addLog("Error : " + ex.getMessage());
                    getLogger().error("Error : " + ex.getMessage(), ex);
                }
            } else if (getDatabaseClass() == SQLite.class) {
                try (Connection connection = SQLite.connect();
                     PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM " + UTILITIES_TABLE + " WHERE UserName = ?")) {

                    selectStatement.setString(1, userName);
                    try (ResultSet existsResult = selectStatement.executeQuery()) {
                        if (!existsResult.next()) { // Insert only if the user does not exist
                            try (PreparedStatement preparedStatement = connection.prepareStatement(
                                    "INSERT INTO " + UTILITIES_TABLE + " (UserName, Online, Version, HasUpdate, LastUpdated) VALUES (?, ?, ?, ?, ?)")) {

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
                    loggerUtils.addLog("Error : " + ex.getMessage());
                    getLogger().error("Error : " + ex.getMessage(), ex);
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
        if (isMySQLOrSQLite()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateDataAsync(UTILITIES_TABLE, "Version", version, "UserName='" + userName + "'", new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (aBoolean)
                            System.out.println("Version updated successfully");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } else if (getDatabaseClass() == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.update(UTILITIES_TABLE, "Version", version, "UserName", userName, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "Version", version, UTILITIES_TABLE);
        }
    }

    public CompletableFuture<String> getVersion(String userName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (isMySQLOrSQLite()) {
            DatabaseHelper dbHelper = getDatabaseHelper();
            try {
                dbHelper.get(UTILITIES_TABLE, "Version", "UserName", userName, new Callback<Object>() {
                    @Override
                    public void onResult(Object result) {
                        future.complete((String) result);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                loggerUtils.addLog("Error : " + e.getMessage());
                getLogger().error("Error : " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            future.complete(backendMongoDBManager.getDocument("userName", userName, UTILITIES_TABLE).get("Version").toString());
        }
        return future;
    }

    public void setOnline(String userName, boolean online) {
        createData(userName);
        if (isMySQLOrSQLite()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateDataAsync(UTILITIES_TABLE, "Online", "" + online, "UserName='" + userName + "'", new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (aBoolean)
                            System.out.println("Online status updated successfully");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } else if (getDatabaseClass() == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.update(UTILITIES_TABLE, "Online", online + "", "UserName", userName, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "Online", online, UTILITIES_TABLE);
        }
    }

    public CompletableFuture<Boolean> isOnline(String userName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (isMySQLOrSQLite()) {
            DatabaseHelper dbHelper = getDatabaseHelper();
            try {
                dbHelper.get(UTILITIES_TABLE, "Online", "UserName", userName, new Callback<Object>() {
                    @Override
                    public void onResult(Object result) {
                        if (result instanceof Boolean)
                            future.complete((Boolean) result);
                        else
                            future.complete(false);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                loggerUtils.addLog("Error : " + e.getMessage());
                getLogger().error("Error : " + e.getMessage(), e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                future.complete((Boolean) backendMongoDBManager.getObject("userName", userName, "Online", UTILITIES_TABLE));
        }
        return future;
    }

    public void setHasUpdate(String userName, boolean hasUpdate) {
        createData(userName);
        if (isMySQLOrSQLite()) {
            if (getDatabaseClass() == MySQL.class) {
                MySQL.updateDataAsync(UTILITIES_TABLE, "HasUpdate", "" + hasUpdate, "UserName='" + userName + "'", new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (aBoolean)
                            System.out.println("Has Update status updated successfully");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            } else if (getDatabaseClass() == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.update(UTILITIES_TABLE, "HasUpdate", String.valueOf(hasUpdate), "UserName", userName, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "HasUpdate", hasUpdate, UTILITIES_TABLE);
        }
    }

    public CompletableFuture<Boolean> hasUpdate(String userName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (isMySQLOrSQLite()) {
            DatabaseHelper dbHelper = getDatabaseHelper();
            try {
                dbHelper.get(UTILITIES_TABLE, "HasUpdate", "UserName", userName, new Callback<Object>() {
                    @Override
                    public void onResult(Object result) {
                        if (result instanceof Boolean)
                            future.complete((Boolean) result);
                        else
                            future.complete(false);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                loggerUtils.addLog("Error : " + e.getMessage());
                getLogger().error("Error : " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                future.complete((boolean) backendMongoDBManager.getObject("userName", userName, "HasUpdate", UTILITIES_TABLE));
        }
        return future;
    }

    public void setLastUpdated(String userName, String date) {
        date = date.replace("'", "");
        createData(userName);
        if (isMySQLOrSQLite()) {
            if (getDatabaseClass() == MySQL.class)
                MySQL.updateDataAsync(UTILITIES_TABLE, "LastUpdated", date, "UserName='" + userName + "'", new MySQL.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean aBoolean) {
                        if (aBoolean)
                            System.out.println("Last Updated date updated successfully");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                    }
                });
            else if (getDatabaseClass() == SQLite.class) {
                DatabaseHelper databaseHelper = getDatabaseHelper();
                try {
                    databaseHelper.update(UTILITIES_TABLE, "LastUpdated", date, "UserName", userName, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                backendMongoDBManager.updateData("userName", userName, "LastUpdated", date, UTILITIES_TABLE);
        }
    }

    public CompletableFuture<String> getLastUpdated(String userName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (isMySQLOrSQLite()) {
            DatabaseHelper dbHelper = getDatabaseHelper();
            try {
                dbHelper.get(UTILITIES_TABLE, "LastUpdated", "UserName", userName, new Callback<Object>() {
                    @Override
                    public void onResult(Object result) {
                        future.complete((String) result);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (SQLException e) {
                loggerUtils.addLog("Error : " + e.getMessage());
                getLogger().error("Error : " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            if (backendMongoDBManager.exists("userName", userName, UTILITIES_TABLE))
                future.complete(backendMongoDBManager.getDocument("userName", userName, UTILITIES_TABLE).get("LastUpdated").toString());
        }
        return future;
    }

    public boolean changePassword(String userName, String oldPassword, String newPassword) {
        byte[] oldPw = null;
        if (isMySQLOrSQLite()) {
            if (getDatabaseClass() == MySQL.class) {
                try {
                    ResultSet resultSet;
                    try (PreparedStatement statement = MySQL.getConnection().prepareStatement("SELECT * FROM accounts WHERE password=?")) {
                        statement.setBytes(1, new PasswordHasher().hashPassword(oldPassword));
                        resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            oldPw = resultSet.getBytes("Password");
                        }
                    }
                    try (PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("UPDATE accounts SET password=? WHERE username=?")) {
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
                    getLogger().log(Level.ERROR, "Failed to retrieve data", ex);
                    return false;
                }
            } else if (getDatabaseClass() == SQLite.class) {
                try {
                    ResultSet resultSet;
                    try (PreparedStatement statement = SQLite.connect().prepareStatement("SELECT * FROM accounts WHERE password=?")) {
                        statement.setBytes(1, new PasswordHasher().hashPassword(oldPassword));
                        resultSet = statement.executeQuery();
                    }
                    if (resultSet.next()) {
                        oldPw = resultSet.getBytes("password");
                    }
                    try (PreparedStatement preparedStatement = SQLite.connect().prepareStatement("UPDATE accounts SET password=? WHERE username=?")) {
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
                    getLogger().log(Level.ERROR, "Failed to retrieve data", ex);
                    return false;
                }
            }
        } else {
            if (backendMongoDBManager.exists("username", userName, TABLE)) {
                oldPw = retrieveBinaryData(TABLE, "userName", userName, "password");
                if (oldPw != null) {
                    backendMongoDBManager.updateData("userName", userName, "password", new PasswordHasher().hashPassword(newPassword), TABLE);
                    return true;
                }
            }
        }
        return false;
    }

    public void updateFromUserData(UserData userData) {
        if (backendMongoDBManager.exists("userName", userData.getUserName(), TABLE))
            backendMongoDBManager.updateAll("userName", userData.getUserName(), userData, TABLE);
    }

    public UserData loadUserDataFromMongoDB(String userName) {
        if (backendMongoDBManager.exists("userName", userName, TABLE))
            return new Gson().fromJson(backendMongoDBManager.getDocument("userName", userName, TABLE).toJson(), UserData.class);
        return null;
    }

    public BackendMongoDBManager getBackendMongoDBManager() {
        return backendMongoDBManager;
    }

    public List<MySQLData> getAllUserData() {
        List<MySQLData> users = new ArrayList<>();
        if (isMySQLOrSQLite()) {

            DatabaseHelper dbHelper = getDatabaseHelper();
            // Fetch all rows at once
            try (Connection connection = dbHelper.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM accounts")) {

                while (rs.next()) {
                    Type type = new TypeToken<List<String>>() {
                    }.getType();
                    MySQLData userData = new MySQLData(
                            rs.getObject("id"),
                            rs.getString("UserName"),
                            rs.getBytes("Password"),
                            rs.getInt("Used"),
                            rs.getInt("MapOpened"),
                            rs.getInt("FilesDownloaded"),
                            new Gson().fromJson(rs.getString("ICAOS"), type),
                            rs.getString("LastUsed")
                    );
                    System.out.println(userData + " added");
                    users.add(userData);
                }
            } catch (SQLException e) {
                getLogger().error("Error fetching user data", e);
            }
        } else {
            for (Document document : backendMongoDBManager.getAllDocuments(TABLE))
                users.add(new Gson().fromJson(document.toJson(), MySQLData.class));
        }
        return users;
    }

    public void saveUser(String userName, MySQLData mySQLData) {
        setIcaos(userName, mySQLData.getIcaos());
        setUsed(userName, mySQLData.getUsed());
        setFilesDownloaded(userName, mySQLData.getFilesDownloaded());
        setLastUsed(userName, mySQLData.getLastUsed());
        setMapOpened(userName, mySQLData.getMapOpened());
    }
}
