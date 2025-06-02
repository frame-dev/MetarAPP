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

    public Remember() {
        this.rememberFile = new File(Main.getFilePath() + "files","remember.json");
        createFileIfNotExists();
    }

    public Remember(String userName, String password) {
        this.rememberFile = new File(Main.getFilePath() + "files","remember.json");
        createFileIfNotExists();
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        try {
            return new Gson().fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class).getUserName();
        } catch (FileNotFoundException | NullPointerException ex) {
            Main.getLogger().log(Level.ERROR, "Could not load or find the remember.json file", ex);
            return "";
        }
    }

    public String getPassword() {
        try {
            if(new Gson().fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class).getPassword() != null) {
                return new Gson().fromJson(new FileReader(new File(Main.getFilePath() + "files", "remember.json")), LoginData.class).getPassword();
            }
        } catch (FileNotFoundException | NullPointerException ex) {
            Main.getLogger().log(Level.ERROR, "Could not load or find the remember.json file", ex);
            return "Password";
        }
		return "Password";
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(rememberFile);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(new LoginData(userName, password)));
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            Main.getLogger().log(Level.ERROR, "Failed to write remember file", exception);
            throw new RuntimeException(exception);
        }

    }

    public boolean exists() {
        return getPassword() != null;
    }

    private void createFileIfNotExists() {
        if(!rememberFile.exists()) {
            try {
                if(!rememberFile.createNewFile())
                    Main.getLogger().log(Level.ERROR, "Remember File cannot be created!");
            } catch (IOException exception) {
                Main.getLogger().log(Level.ERROR, "Error creating remember file: " + exception.getMessage(), exception);
            }
        }
    }
}
