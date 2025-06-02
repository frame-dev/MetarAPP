package ch.framedev.metarapp.util;

public interface Plugin {
    void initialize();
    void start();
    void stop();

    String getName();
    String getVersion();
    String getDescription();
    String getAuthor();
    String getWebsite();

    String getNewVersion();
    String getDownloadLink();
}
