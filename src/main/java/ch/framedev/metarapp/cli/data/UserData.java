package ch.framedev.metarapp.cli.data;

import java.util.ArrayList;
import java.util.List;

import ch.framedev.metarapp.cli.Main;

public class UserData {

    private final Object id;
    private List<String> icaos;
    private String userName;

    public UserData(Object id, List<String> icaos, String userName) {
        this.id = id;
        this.icaos = icaos;
        this.userName = userName;
        hasMetarAPP();
    }

    public List<String> getIcaos() {
        if (icaos == null)
            new ArrayList<>();
        return icaos;
    }

    public void setIcaos(List<String> icaos) {
        this.icaos = icaos;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getId() {
        return id;
    }

    public boolean hasMetarAPP() {
        return Main.database.getLastUsed(userName) != null;
    }

    public void save() {
        Main.database.setIcaos(userName, icaos);
    }
}
