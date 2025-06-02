package ch.framedev.metarapp.cli.handlers;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.simplejavautils.SimpleJavaUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class Settings {

    private Properties properties;
    private final File file;

    public Settings(File file) {
        this.file = file;
        try {
            loadProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadProperties() throws IOException {
        if(!file.exists()) {
            Files.copy(Main.utils.getFromResourceFile("settings.properties", Main.class).toPath(),
                    file.toPath());
            this.properties = new Properties();
            this.properties.load(new FileReader(file));
        } else {
            Properties resourceProperties = new Properties();
            resourceProperties.load(new FileReader(new SimpleJavaUtils().getFromResourceFile("settings.properties", Main.class)));
            this.properties = new Properties();
            this.properties.load(new FileReader(file));
            resourceProperties.forEach((key, value) ->  {
                if(!properties.containsKey(key))
                    properties.put(key, value);
                if(!resourceProperties.getProperty("version").equalsIgnoreCase(properties.getProperty("version"))) {
                    properties.setProperty("version", resourceProperties.getProperty("version"));
                }
            });
            this.properties.setProperty("version", Main.VERSION);
            this.properties.store(new FileWriter(file), "");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void save() {
        try {
            this.properties.store(new FileWriter(file), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveComments(String comments) {
        try {
            this.properties.store(new FileWriter(file), comments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public boolean contains(String key) {
        return this.properties.containsKey(key);
    }
}
