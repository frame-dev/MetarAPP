package ch.framedev.metarapp.handlers;

import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.Changelog;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.ErrorMessages;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static ch.framedev.metarapp.main.Main.*;

public class ChangelogsReader {

    private final List<Changelog> changelogs;

    public ChangelogsReader() {
        try {
            Type type = new TypeToken<List<Changelog>>() {
            }.getType();
            this.changelogs = new Gson().fromJson(new FileReader(utils.getFromResourceFile("changelogs.json", Main.class)), type);
            if (!new File(Main.getFilePath() + "files").exists())
                if (new File(Main.getFilePath() + "files").mkdirs()) {
                    Main.getLogger().log(Level.ERROR, "Failed to create files folder. " + ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY);
                    loggerUtils.addLog("Failed to create files folder. " + ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY);
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY, 
                            "Failed to create files folder."));
                }
            utils.copyFileTo(utils.getFromResourceFile("changelogs.json"), new File(Main.getFilePath() + "files", "changelogs.json"));
        } catch (Exception ex) {
            String errorText = ErrorMessages.getErrorLoadingChangelogsFile(utils.getFromResourceFile("changelogs.json").toPath().toString(),
                    ErrorCode.ERROR_LOAD.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_LOAD, errorText));
            Main.getLogger().log(Level.ERROR, errorText, ex);
            loggerUtils.addLog(errorText);
            throw new RuntimeException(ex);
        }
    }

    public List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        for (Changelog changelog : changelogs) {
            versions.add(changelog.getVersion());
        }
        return versions;
    }

    public List<String> getPreReleaseVersions() {
        List<String> versions = new ArrayList<>();
        for (Changelog changelog : changelogs) {
            if (changelog.getVersion().contains("PRE-RELEASE"))
                versions.add(changelog.getVersion());
        }
        return versions;
    }

    public Changelog getFromVersion(String version) {
        for (Changelog changelog : changelogs) {
            if (changelog.getVersion().equalsIgnoreCase(version)) {
                return changelog;
            }
        }
        return null;
    }

    public List<Changelog> getAllPreReleaseChangelogs() {
        List<Changelog> changelogList = new ArrayList<>();
        for (Changelog changelog : changelogs) {
            if (changelog.getVersion().contains("PRE-RELEASE")) {
                changelogList.add(changelog);
            }
        }
        return changelogList;
    }

    public Changelog getChangelogs(String version) {
        String editedVersion = version;
        if (editedVersion.contains("\""))
            editedVersion = editedVersion.replace("\"", "");
        return getFromVersion(editedVersion);
    }

    public String getChangelogAsString(String version) {
        Changelog changelog = getChangelogs(version);
        if (changelog == null)
            return "Version not found.";
        StringBuilder builder = new StringBuilder();
        builder.append("Version: ").append(changelog.getVersion()).append("\n");
        builder.append("Date: ").append(changelog.getDate()).append("\n");
        builder.append("Performance Fixes:\n");
        for (String fix : changelog.getPerformanceFixes()) {
            builder.append("- ").append(fix).append("\n");
        }
        builder.append("Bug Fixes:\n");
        for (String fix : changelog.getBugFixes()) {
            builder.append("- ").append(fix).append("\n");
        }
        builder.append("Features:\n");
        for (String feature : changelog.getFeatures()) {
            builder.append("- ").append(feature).append("\n");
        }
        return builder.toString();
    }

    public boolean isVersionReleased(String version) {
        Changelog changelog = getFromVersion(version);
        if (changelog == null) return false;
        return getFromVersion(version).isRelease();
    }
}
