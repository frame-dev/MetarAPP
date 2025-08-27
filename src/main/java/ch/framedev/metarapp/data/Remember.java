package ch.framedev.metarapp.data;

import ch.framedev.metarapp.main.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Level;

import java.io.*;

public class Remember {

    private String userName;
    private String password;
    private final File rememberFile;
    private LoginData cachedLoginData;

    public Remember() {
        this.rememberFile = new File(Main.getFilePath() + "files", "remember.json");
        createFileIfNotExists();
        loadLoginData();
    }

    public Remember(String userName, String password) {
        this.rememberFile = new File(Main.getFilePath() + "files", "remember.json");
        createFileIfNotExists();
        this.userName = userName;
        this.password = password;
        this.cachedLoginData = new LoginData(userName, password);
    }

    public String getUserName() {
        return cachedLoginData != null && cachedLoginData.getUserName() != null
                ? cachedLoginData.getUserName()
                : "";
    }

    public String getPassword() {
        return cachedLoginData != null && cachedLoginData.getPassword() != null
                ? cachedLoginData.getPassword()
                : "";
    }

    /**
     * Saves the current userName and password to the remember.json file.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(rememberFile)) {
            LoginData data = new LoginData(userName, password);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(data));
            writer.flush();
            cachedLoginData = data;
        } catch (IOException exception) {
            Main.getLogger().log(Level.ERROR, "Failed to write remember file", exception);
            throw new RuntimeException(exception);
        }
    }

    public boolean exists() {
        return rememberFile.exists() && cachedLoginData != null;
    }

    private void createFileIfNotExists() {
        if (!rememberFile.exists()) {
            try {
                if (!rememberFile.createNewFile())
                    Main.getLogger().log(Level.ERROR, "Remember File cannot be created!");
            } catch (IOException exception) {
                Main.getLogger().log(Level.ERROR, "Error creating remember file: " + exception.getMessage(), exception);
            }
        }
    }

    private void loadLoginData() {
        if (rememberFile.exists()) {
            try (FileReader reader = new FileReader(rememberFile)) {
                cachedLoginData = new Gson().fromJson(reader, LoginData.class);
            } catch (IOException | NullPointerException ex) {
                Main.getLogger().log(Level.ERROR, "Could not load or find the remember.json file", ex);
                cachedLoginData = null;
            }
        }
    }
}