package ch.framedev.metarapp.data;

import ch.framedev.javamysqlutils.MySQL;
import ch.framedev.metarapp.main.Main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySQLData {

    private Object id;
    private String userName;
    private byte[] password;
    private int used, mapOpened, filesDownloaded;
    private List<String> icaos;
    private String lastUsed;

    public MySQLData(String userName) {
        initializeUser(userName).thenRun(() -> System.out.println("User initialization successful!")).exceptionally(ex -> {
            System.err.println("Error initializing user: " + ex.getMessage());
            return null;
        });
    }

    public MySQLData(Object id, String userName, byte[] password, int used, int mapOpened, int filesDownloaded, List<String> icaos, String lastUsed) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.used = used;
        this.mapOpened = mapOpened;
        this.filesDownloaded = filesDownloaded;
        this.icaos = icaos;
        this.lastUsed = lastUsed;
    }

    public CompletableFuture<MySQLData> initializeUserAsync(String userName) {
        return initializeUser(userName).thenApply(v -> this);
    }

    public CompletableFuture<Void> initializeUser(String userName) {
        this.userName = userName;

        CompletableFuture<Void> idFuture;
        if (Main.database.getDatabaseClass() == MySQL.class) {
            idFuture = Main.database.getID(userName)
                    .thenAccept(id -> this.id = id)
                    .exceptionally(ex -> {
                        logError("Failed to fetch ID for user: " + userName, ex);
                        return null;
                    });
        } else if (!Main.database.isMySQLOrSQLite()) {
            // Fallback for MongoDB
            this.id = Main.database.getBackendMongoDBManager().getObject("userName", userName, "_id", "accounts");
            idFuture = CompletableFuture.completedFuture(null);
        } else {
            idFuture = CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> passwordFuture = Main.database.getPassword(userName)
                .thenAccept(bytes -> {
                    logInfo("Fetched X: " + Arrays.toString(bytes));
                    this.password = bytes;
                })
                .exceptionally(ex -> {
                    logError("Failed to fetch password for user: " + userName, ex);
                    return null;
                });

        CompletableFuture<Void> usedFuture = Main.database.getUsed(userName)
                .thenAccept(used -> {
                    logInfo("Fetched X: " + used);
                    if (used == null) {
                        logInfo("No 'used' value set for user: " + userName);
                        this.used = 0;
                    } else {
                        setUsed(used);
                    }
                })
                .exceptionally(ex -> {
                    logError("Failed to fetch 'used' value for user: " + userName, ex);
                    return null;
                });

        CompletableFuture<Void> mapOpenedFuture = Main.database.getMapOpened(userName)
                .thenAccept(mapOpened -> {
                    logInfo("Fetched X: " + mapOpened);
                    this.mapOpened = mapOpened;
                })
                .exceptionally(ex -> {
                    logError("Failed to fetch 'mapOpened' for user: " + userName, ex);
                    return null;
                });

        CompletableFuture<Void> filesDownloadedFuture = Main.database.getFilesDownloaded(userName)
                .thenAccept(filesDownloaded -> this.filesDownloaded = filesDownloaded)
                .exceptionally(ex -> {
                    logError("Failed to fetch 'filesDownloaded' for user: " + userName, ex);
                    return null;
                });

        CompletableFuture<Void> icaosFuture = Main.database.getIcaos(userName)
                .thenAccept(icaosList -> {
                    if (icaosList != null && !icaosList.isEmpty()) {
                        this.icaos = icaosList;
                        logInfo("ICAOs for user " + userName + ": " + this.icaos);
                    } else {
                        this.icaos = new ArrayList<>(); // Set to "Error" in case of failure
                        logInfo("No ICAOs set for user: " + userName);
                    }
                })
                .exceptionally(ex -> {
                    logError("Failed to fetch 'ICAOs' for user: " + userName, ex);
                    this.icaos = new ArrayList<>(); // Set to "Error" in case of failure
                    return null;
                });

        CompletableFuture<Void> lastUsedFuture = Main.database.getLastUsed(userName)
                .thenAccept(lastUsed -> this.lastUsed = lastUsed)
                .exceptionally(ex -> {
                    logError("Failed to fetch 'lastUsed' for user: " + userName, ex);
                    return null;
                });

        // Combine all futures to ensure they all complete before proceeding
        return CompletableFuture.allOf(
                idFuture,
                passwordFuture,
                usedFuture,
                mapOpenedFuture,
                filesDownloadedFuture,
                icaosFuture,
                lastUsedFuture
        ).thenRun(() -> logInfo("User initialization completed for: " + userName));
    }

    private void logError(String message, Throwable throwable) {
        Main.getLogger().error(message, throwable);
        Main.loggerUtils.addLog(message + ": " + throwable.getMessage());
    }

    private void logInfo(String message) {
        Main.getLogger().info(message);
        Main.loggerUtils.addLog(message);
    }


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getMapOpened() {
        return mapOpened;
    }

    public void setMapOpened(int mapOpened) {
        this.mapOpened = mapOpened;
    }

    public int getFilesDownloaded() {
        return filesDownloaded;
    }

    public void setFilesDownloaded(int filesDownloaded) {
        this.filesDownloaded = filesDownloaded;
    }

    public List<String> getIcaos() {
        return icaos;
    }

    public void setIcaos(List<String> icaos) {
        this.icaos = icaos;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void save() {
        Main.database.saveUser(userName, this);
    }

    @Override
    public String toString() {
        return "MySQLData{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", password=" + Arrays.toString(password) +
                ", used=" + used +
                ", mapOpened=" + mapOpened +
                ", filesDownloaded=" + filesDownloaded +
                ", icaos='" + icaos + '\'' +
                ", lastUsed='" + lastUsed + '\'' +
                '}';
    }
}
