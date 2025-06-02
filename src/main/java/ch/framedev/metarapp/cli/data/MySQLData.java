package ch.framedev.metarapp.cli.data;

import com.google.gson.Gson;

import ch.framedev.metarapp.cli.Main;

import java.util.Arrays;

public class MySQLData {

    Object id;
    String userName;
    byte[] password;
    int used, mapOpened, filesDownloaded;
    String icaos, lastUsed;

    public MySQLData(String userName) {
        Object id = Main.database.getID(userName);
        byte[] password = Main.database.getPassword(userName);
        int used = Main.database.getUsed(userName);
        int mapOpened = Main.database.getMapOpened(userName);
        int filesDownloaded = Main.database.getFilesDownloaded(userName);
        String icaos = new Gson().toJson(Main.database.getIcaos(userName));
        String lastUsed = Main.database.getLastUsed(userName);
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.used = used;
        this.mapOpened = mapOpened;
        this.filesDownloaded = filesDownloaded;
        this.icaos = icaos;
        this.lastUsed = lastUsed;
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

    public String getIcaos() {
        return icaos;
    }

    public void setIcaos(String icaos) {
        this.icaos = icaos;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
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
