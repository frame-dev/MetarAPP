package ch.framedev.metarapp.cli.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.framedev.metarapp.cli.Main;

import java.io.*;

public class Remember {
    private String userName;
    private String password;
    private final File rememberFile;
    private boolean dontAskBoolean;

    public Remember() {
        this.rememberFile = new File(Main.getFilePath() + "files", "remember.json");
        if (!rememberFile.exists()) {
            try {
                rememberFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setDontAskBoolean(boolean dontAskBoolean) {
        this.dontAskBoolean = dontAskBoolean;
    }

    public Remember(String userName, String password) {
        this.rememberFile = new File(Main.getFilePath() + "files", "remember.json");
        if (!rememberFile.exists()) {
            try {
                rememberFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.userName = userName;
        this.password = password;
    }

    public Remember(String userName, String password, boolean dontAskBoolean) {
        this.rememberFile = new File(Main.getFilePath() + "files", "remember.json");
        if (!rememberFile.exists()) {
            try {
                rememberFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.userName = userName;
        this.password = password;
        this.dontAskBoolean = dontAskBoolean;
    }

    public String getUserName() {
        try {
            return new Gson()
                    .fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class)
                    .getUserName();
        } catch (FileNotFoundException | NullPointerException ignore) {
            return "";
        }
    }

    public String getPassword() {
        try {
            if (new Gson()
                    .fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class)
                    .getPassword() != null) {
                return new Gson().fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")),
                        LoginData.class).getPassword();
            }
        } catch (FileNotFoundException | NullPointerException ex) {
            return "Password";
        }
        return "Password";
    }

    public boolean getDontAskBoolean() {
        try {
            return new Gson()
                    .fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class)
                    .isDontAskBoolean();
        } catch (FileNotFoundException | NullPointerException ignore) {
            return false;
        }
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(rememberFile);
            writer.write(new GsonBuilder().setPrettyPrinting().create()
                    .toJson(new LoginData(userName, password, dontAskBoolean)));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean exists() {
        return getPassword() != null;
    }
}
