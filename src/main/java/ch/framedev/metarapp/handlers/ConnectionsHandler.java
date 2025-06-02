package ch.framedev.metarapp.handlers;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 08.11.2024 16:24
 */

import ch.framedev.metarapp.main.Main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static ch.framedev.metarapp.main.Main.utils;

public class ConnectionsHandler {

    private Properties properties;

    public ConnectionsHandler() {
        File file = new File(Main.getFilePath() + "files", "connections.properties");
        if(!new File(Main.getFilePath() + "files", "connections.properties").exists()) {
            new File(Main.getFilePath() + "files", "connections.properties").getParentFile().mkdir();
            try {
            Files.copy(utils.getFromResourceFile("connections.properties", Main.class).toPath(), file.toPath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            loadProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadProperties() throws IOException {
        Properties resourceProperties = new Properties();
        this.properties = new Properties();
        resourceProperties.load(new FileReader(new File(Main.getFilePath() + "files", "connections.properties")));
        properties = resourceProperties;
        resourceProperties.store(new FileWriter(new File(Main.getFilePath() + "files", "connections.properties")), "Connections Properties");
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public boolean contains(String key) {
        return this.properties.containsKey(key);
    }
}