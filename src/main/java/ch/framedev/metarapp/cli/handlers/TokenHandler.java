package ch.framedev.metarapp.cli.handlers;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.simplejavautils.SimpleJavaUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TokenHandler {

    private Properties properties;
    private final File file;

    public TokenHandler() {
        this.file = new SimpleJavaUtils().getFromResourceFile("tokens.properties", Main.class);
        try {
            loadProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadProperties() throws IOException {
        Properties resourceProperties = new Properties();
        resourceProperties.load(new FileReader(new SimpleJavaUtils().getFromResourceFile("tokens.properties", Main.class)));
        this.properties = new Properties();
        this.properties.load(new FileReader(file));
        resourceProperties.forEach((key, value) -> {
            if (!properties.containsKey(key))
                properties.put(key, value);
        });
    }

    @SuppressWarnings("unused")
    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    @SuppressWarnings("unused")
    public boolean contains(String key) {
        return this.properties.containsKey(key);
    }
}