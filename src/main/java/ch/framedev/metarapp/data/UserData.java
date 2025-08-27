package ch.framedev.metarapp.data;

import ch.framedev.metarapp.main.Main;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserData {

    private @NotNull String userName;
    private int used;
    private int mapOpened;
    private int filesDownloaded;
    private List<String> icaos;
    private String lastUsed;

    public UserData(@NotNull String userName, int used, int mapOpened, int filesDownloaded, List<String> icaos, String lastUsed) {
        this.userName = userName;
        this.used = used;
        this.mapOpened = mapOpened;
        this.filesDownloaded = filesDownloaded;
        this.icaos = icaos;
        this.lastUsed = lastUsed;
    }

    public @NotNull String getUserName() {
        return userName;
    }

    public void setUserName(@NotNull String userName) {
        this.userName = userName;
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

    /**
     * Saves the user data to the database.
     * This method should be called whenever the user data is modified.
     */
    public void save() {
        Main.database.setUsed(userName, used);
        Main.database.setMapOpened(userName, mapOpened);
        Main.database.setFilesDownloaded(userName, filesDownloaded);
        Main.database.setIcaos(userName, icaos);
        Main.database.setLastUsed(userName, lastUsed);
        Main.loggerUtils.addLog("Saved Userdata for " + userName);
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
