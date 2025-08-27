package ch.framedev.metarapp.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ch.framedev.metarapp.main.Main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Level;

import static ch.framedev.metarapp.main.Main.getLogger;
import static ch.framedev.metarapp.main.Main.loggerUtils;

@SuppressWarnings("unused")
public class UpdateHandler {

    @SuppressWarnings("BusyWait")
    public void init() {
        Thread thread = new Thread(() -> {
            long minutes;
            try {
                minutes = Long.parseLong(Main.settings.getString("update-check-interval", "15"));
            } catch (NumberFormatException e) {
                Main.loggerUtils.addLog("Invalid update interval, defaulting to 15 minutes.");
                minutes = 15;
            }
            if (minutes <= 0) {
                Main.loggerUtils.addLog("Update check interval is set to 0 or less, skipping update check.");
                return;
            }

            long millis = minutes * 60L * 1000L; // Use long literals to avoid overflow
            Main.loggerUtils.addLog("Update check will run every " + minutes + " minutes.");
            Main.getLogger().log(Level.INFO, "Update check will run every " + minutes + " minutes.");

            while (true) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Best practice: restore interrupt flag
                    Main.loggerUtils.addLog("Update check sleep was interrupted: " + e.getMessage());
                    break; // Exit the loop if interrupted
                }
                if (Main.branch.equalsIgnoreCase("release")) {
                    if (Main.hasUpdate()) {
                        int result = JOptionPane.showConfirmDialog(null, "Download latest Release?", "Update Available",
                                JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            Main.update().thenAccept(success -> {
                                if (success) {
                                    loggerUtils.addLog("Update downloaded successfully. Restarting application...");
                                    JOptionPane.showMessageDialog(null,
                                            "Update downloaded successfully. Restarting application...");
                                    Main.deleteOldVersionAndStartNew();
                                } else {
                                    loggerUtils.addLog("Failed to download the update.");
                                }
                            });
                        } else {
                            loggerUtils.addLog("User chose not to download the update.");
                        }
                    }
                } else if (Main.branch.equalsIgnoreCase("pre-release")) {
                    if (Main.hasUpdatePreRelease()) {
                        int result = JOptionPane.showConfirmDialog(null, "Download latest Pre-Release?",
                                "Update Available", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            Main.update().thenAccept(success -> {
                                if (success) {
                                    JOptionPane.showMessageDialog(null,
                                            "Pre-Release update downloaded successfully. Restarting application...");
                                    loggerUtils.addLog("Pre-Release update downloaded successfully. Restarting application...");

                                    Main.deleteOldVersionAndStartNew();
                                } else {
                                    loggerUtils.addLog("Failed to download the Pre-Release update.");
                                }
                            });
                        } else {
                            loggerUtils.addLog("User chose not to download the Pre-Release update.");
                        }
                    }
                }
            }
        });
        thread.setName("UpdateHandler");
        thread.setDaemon(true);
        thread.start();
    }

    private static final URL versionUrl;

    static {
        try {
            versionUrl = new URL("https://framedev.ch/files/metarapp/versions.json");
        } catch (MalformedURLException e) {
            loggerUtils.addLog("Error retrieving version URL: " + e.getMessage());
            getLogger().error("Error retrieving version URL: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String getLatestVersion() throws IOException {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            return jsonElement.getAsJsonObject().get("latest").getAsString();
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving latest version: " + e.getMessage());
            getLogger().error("Error retrieving latest version: " + e.getMessage(), e);
        }
        return null;
    }

    public static String getLatestBuildNumber() throws IOException {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            return jsonElement.getAsJsonObject().get("latest-buildNumber").getAsString();
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving latest build number: " + e.getMessage());
            getLogger().error("Error retrieving latest build number: " + e.getMessage(), e);
        }
        return null;
    }

    public static List<String> getAllBuildNumbers() {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            List<String> result = new ArrayList<>();
            for (JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("buildNumbers"))
                result.add(element.getAsString());
            return result;
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving all build numbers: " + e.getMessage());
            getLogger().error("Error retrieving all build numbers: " + e.getMessage(), e);
        }
        return null;
    }

    public static List<String> getAllVersions() {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            List<String> result = new ArrayList<>();
            for (JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("version"))
                result.add(element.getAsString());
            return result;
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving all versions: " + e.getMessage());
            getLogger().error("Error retrieving all versions: " + e.getMessage(), e);
        }
        return null;
    }

    public static String getLatestPreRelease() {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            return jsonElement.getAsJsonObject().get("latest-pre-release").getAsString();
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving latest pre-release: " + e.getMessage());
            getLogger().error("Error retrieving latest pre-release: " + e.getMessage(), e);
        }
        return null;
    }

    public static List<String> getAllPreReleases() {
        try {
            JsonElement jsonElement = JsonParser
                    .parseReader(new InputStreamReader(versionUrl.openConnection().getInputStream()));
            List<String> result = new ArrayList<>();
            for (JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("pre-release"))
                result.add(element.getAsString());
            return result;
        } catch (IOException e) {
            loggerUtils.addLog("Error retrieving all pre-releases: " + e.getMessage());
            getLogger().error("Error retrieving all pre-releases: " + e.getMessage(), e);
        }
        return null;
    }

    public static String formatVersion(String version, String buildNumber) {
        if (buildNumber.contains(version))
            return version + buildNumber.replace(version, "");
        return null;
    }
}
