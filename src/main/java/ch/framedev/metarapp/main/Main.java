package ch.framedev.metarapp.main;

import ch.framedev.metarapp.database.Database;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.guis.LoginFrame;
import ch.framedev.metarapp.handlers.ChangelogsReader;
import ch.framedev.metarapp.handlers.ConnectionsHandler;
import ch.framedev.metarapp.util.*;
import ch.framedev.simplejavautils.SimpleJavaUtils;
import ch.framedev.simplejavautils.SystemUtils;
import ch.framedev.simplejavautils.TextUtils;
import ch.framedev.simplejavautils.TokenHandler;
import ch.framedev.yamlutils.FileConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static String[] args;
    public static FileConfiguration settings;
    public static ChangelogsReader changelogsReader;

    // TODO Change on every Version
    public static final String VERSION = "1.6.4.2";
    public static String preRelease = "1.6.4.2-PRE-RELEASE";
    public static final String BUILD_NUMBER = "1.6.4.2-1012";

    // Development variables
    // TODO Change on publish
    public static final boolean DEVELOPMENT = false;
    public static final boolean TESTING = false;

    public static TokenHandler tokenHandler;
    public static ConnectionsHandler connectionTokenHandler;

    // Database
    public static Database database;

    private static final Logger logger = Logger.getLogger("MetarAPP");

    public static SimpleJavaUtils utils = new SimpleJavaUtils();

    public static LocaleUtils localeUtils;
    public static Variables variables;
    public static LoggerUtils loggerUtils;
    public static String from;

    // Update Branch
    public static String branch;

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Main.args = args;
        // Create Files Directory for Last Search
        createFilesDirectory();

        // Instantiates the TokenHandler Field
        tokenHandler = new TokenHandler("tokens.properties");
        // Instantiates the ConnectionTokenHandler Field for connections
        connectionTokenHandler = new ConnectionsHandler();

        // Logger Utils
        loggerUtils = new LoggerUtils();

        setupSettingsAndMoveFiles();

        // Deletes the old installed Versions
        if (!deleteOldVersion())
            getLogger().error("Could not delete old installed Version!");

        if (hasUpdate() && branch.equalsIgnoreCase("release")) {
            new TextUtils().printBox("There is a new version available!", "[" + getNewVersion() + "]");
        } else if (hasUpdatePreRelease()) {
            new TextUtils().printBox("There is a new Pre-Release version available!",
                    "[" + getLatestPreRelease() + "]");
        }

        // Setup Variables
        variables = new Variables();
        if (variables.initialize())
            logger.info("Initializing variables completed");
        else
            logger.error("Initializing variables failed");

        createDownloadsFolder();
        File documentsFolder = createDocumentsFolder();

        try {
            Files.copy(utils.getFromResourceFile("documents/error_explanation.txt", Main.class).toPath(),
                    new File(documentsFolder, "error_explanation.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(utils.getFromResourceFile("documents/api_explanation.md", Main.class).toPath(),
                    new File(documentsFolder, "api_explanation.md").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(utils.getFromResourceFile("documents/plugin_example.md", Main.class).toPath(),
                    new File(documentsFolder, "plugin_example.md").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            changelogsReader = new ChangelogsReader();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // instantiates the localeUtils Field
        try {
            Locale locale = Locale.fromLocaleString((String) settings.get("language"));
            localeUtils = new LocaleUtils(locale);
        } catch (LocaleNotFoundException | NullPointerException e) {
            logger.log(Level.ERROR, e.getMessage());
            localeUtils = new LocaleUtils();
            if (Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(null, "Locale not found using Fallback Language!");
            }
            loggerUtils.addLog("Locale not found using Fallback Language!");
        }

        if (!Desktop.isDesktopSupported() && args.length == 1 && args[0].equalsIgnoreCase("cli")) {
            try {
                ch.framedev.metarapp.cli.Main.main(args);
            } catch (IOException | ch.framedev.metarapp.cli.utils.LocaleNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
        // Database initialization
        database = new Database();
        if (database.isMySQLOrSQLite()) {
            database.createTableIfNotExists();
            database.createTableIfNotExistsUtilities();
        }
        try {
            if (!database.existsUser("admin"))
                database.createAdminAccount();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Load plugins at startup
        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.loadPlugins();
        pluginManager.initializePlugins();
        pluginManager.enablePlugins();
        pluginManager.listPlugins();

        UpdateHandler updateHandler = new UpdateHandler();
        updateHandler.init();

        if (Desktop.isDesktopSupported()) {
            if (!(boolean) Main.settings.get("show-changelogs")) {
                showChangelogs();
            }
            // Launches LoginGUI to Continue
            if (netIsAvailable()) {
                // new LoginGUI();
                LoginFrame.main(args);
            } else {
                try {
                    loggerUtils.addLog("No Network is available : " + ErrorCode.ERROR_NO_NETWORK.getError());
                    logger.log(Level.ERROR, "No Network is available : " + ErrorCode.ERROR_NO_NETWORK.getError());
                    EventBus.dispatchErrorEvent(
                            new ErrorEvent(ErrorCode.ERROR_NO_NETWORK, "No Network is available"));
                    JOptionPane.showMessageDialog(null,
                            "You cannot use the Program properly without a Network connection");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "The API is not Online! or no Network");
                    loggerUtils.addLog("The API is not Online ! Or you have no Network Connection"
                            + ErrorCode.ERROR_API_DOWN.getError() + " : " + e.getMessage());
                    logger.log(Level.ERROR, "The API is not Online ! Or you have no Network Connection"
                            + ErrorCode.ERROR_API_DOWN.getError() + " : " + e.getMessage());
                    EventBus.dispatchErrorEvent(
                            new ErrorEvent(ErrorCode.ERROR_API_DOWN,
                                    "The API is not Online ! Or you have no Network Connection"));
                    throw new RuntimeException(e);
                }
                LoginUtils.userNameStatic = JOptionPane.showInputDialog("Username");
            }
            settings.set("version", VERSION);
            settings.save();
        }

        // new VersionFile().uploadVersions();
    }

    private static void createDownloadsFolder() {
        File downloadDirectory = new File(getFilePath() + settings.get("download-folder"));
        System.out.println("Files directory path: " + downloadDirectory.getAbsolutePath());

        // Attempt to create the directory
        if (!downloadDirectory.mkdirs()) {
            // Check if the directory already exists
            if (downloadDirectory.exists() && downloadDirectory.isDirectory()) {
                System.out.println("Directory already exists.");
            } else {
                // Print an error message if the directory does not exist
                System.out.println("Error: Directory creation failed and directory does not exist.");
                loggerUtils.addLog("Error: Directory creation failed and directory does not exist.");
                throw new RuntimeException(
                        "Could not create Files Directory in " + getFilePath() + Main.settings.get("download-folder"));
            }
        }
    }

    public static void setupSettingsAndMoveFiles() {
        if (isOs(SystemUtils.OSType.MACOS) || isOs(SystemUtils.OSType.LINUX) || isOs(SystemUtils.OSType.OTHER)) {
            if (!new File(getFilePath() + "files", "settings.yml").exists()) {
                new File(getFilePath() + "files", "settings.yml").getParentFile().mkdir();
                try {
                    new File(getFilePath() + "files", "settings.yml").createNewFile();
                    Files.copy(utils.getFromResourceFile("settings.yml", Main.class).toPath(),
                            new File(getFilePath() + "files", "settings.yml").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("setup Settings");
            settings = new FileConfiguration(utils.getFromResourceFile("settings.yml", Main.class),
                    new File(getFilePath() + "files", "settings.yml"));
            settings.set("version", VERSION);

            // Create ICAO's File
            try {
                Files.copy(utils.getFromResourceFile("iata-icao.csv", Main.class).toPath(),
                        new File(getFilePath() + "files/iata-icao.csv").toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Files.copy(utils.getFromResourceFile("CountryCodes.csv", Main.class).toPath(),
                        new File(getFilePath() + "files/CountryCodes.csv").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (isOs(SystemUtils.OSType.WINDOWS)) {
            if (!new File(getFilePath() + "files", "settings.yml").exists()) {
                new File(getFilePath() + "files", "settings.yml").getParentFile().mkdir();
                try {
                    new File(getFilePath() + "files", "settings.yml").createNewFile();
                    Files.copy(utils.getFromResourceFile("settings.yml", Main.class).toPath(),
                            new File(getFilePath() + "files", "settings.yml").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("setup Settings");
            settings = new FileConfiguration(utils.getFromResourceFile("settings.yml", Main.class),
                    new File(getFilePath() + "files", "settings.yml"));
            settings.set("version", VERSION);

            // Create ICAO's File
            try {
                Files.copy(utils.getFromResourceFile("iata-icao.csv", Main.class).toPath(),
                        new File(getFilePath() + "files\\iata-icao.csv").toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Files.copy(utils.getFromResourceFile("CountryCodes.csv", Main.class).toPath(),
                        new File(getFilePath() + "files\\CountryCodes.csv").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        settings.set("buildNumber", BUILD_NUMBER);
        settings.save();

        // Set branch
        if (!TESTING)
            branch = settings.getString("branch");
        else
            branch = "pre-release";

    }

    private static void createFilesDirectory() {
        File filesDirectory = new File(getFilePath() + "files");
        System.out.println("Files directory path: " + filesDirectory.getAbsolutePath());
        try {
            setupLocales();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Attempt to create the directory
        if (!filesDirectory.mkdirs()) {
            // Check if the directory already exists
            if (filesDirectory.exists() && filesDirectory.isDirectory()) {
                System.out.println("Directory already exists.");
            } else {
                // Print an error message if the directory does not exist
                System.out.println("Error: Directory creation failed and directory does not exist. "
                        + ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                loggerUtils.addLog("Error: Directory creation failed and directory does not exist. "
                        + ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY,
                        "Could not create Files Directory in " + getFilePath() + "files"));
                throw new RuntimeException("Could not create Files Directory in " + getFilePath() + "files");
            }
        }
    }

    /**
     * Creates the documents folder if it does not exist.
     *
     * @return the File object representing the document's folder.
     *         If the documents folder cannot be created, it logs an error message
     *         and returns null.
     */
    private static @NotNull File createDocumentsFolder() {
        File documentsFolder = new File(Variables.DOCUMENTS_DIRECTORY);
        if (!documentsFolder.exists()) {
            if (!documentsFolder.mkdirs()) {
                System.out.println("Documents folder cannot be created");
                loggerUtils.addLog("Documents folder cannot be created");
            }
        }
        return documentsFolder;
    }

    /**
     * Sets up the locales for the application by creating the necessary folders and
     * copying locale resource files.
     * If the "locales" folder does not exist, it creates the folder and its parent
     * directories.
     * Then,
     * it copies the English and German local resource files from the application's
     * resources to the specified file path,
     * replacing existing files if necessary.
     *
     * @throws IOException if an I/O error occurs during the setup of locales
     */
    public static void setupLocales() throws IOException {
        // Check if the "locales" folder exists, create it if it doesn't
        if (!new File(getFilePath() + "locales").exists()) {
            if (!new File(getFilePath() + "locales").mkdirs()) {
                loggerUtils.addLog("Could not create the " + getFilePath() + "locales folder");
                throw new IOException("Could not create the " + getFilePath() + "locales folder");
            }
        }

        copyResourceFiles(getFilePath() + "locales");
    }

    /**
     * Copies the locales folder to the specified location
     *
     * @param destinationDirPath Destination directory
     */
    private static void copyResourceFiles(String destinationDirPath) {
        String[] resourceFiles = {
                "locales/fallback.yml",
                "locales/fr-Fr.yml",
                "locales/ru-Ru.yml",
                "locales/it-It.yml",
                "locales/de-De.yml",
                "locales/en-En.yml",
                "locales/errorMessages_de-De.yml",
                "locales/errorMessages_en-En.yml",
                "locales/errorMessages_ru-Ru.yml",
                "locales/errorMessages_it-It.yml"
        };

        for (String resourcePath : resourceFiles) {
            try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new IOException("Resource not found: " + resourcePath);
                }

                File destinationFile = new File(destinationDirPath, new File(resourcePath).getName());
                Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File saved successfully: " + destinationFile.getAbsolutePath());

            } catch (IOException e) {
                getLogger().error(e.getMessage(), e);
                throw new RuntimeException("Error copying resource file: " + resourcePath, e);
            }
        }
    }

    /**
     * Check if the network is available by attempting to connect to a specific URL.
     *
     * @return true if the network is available, false otherwise
     */
    public static boolean netIsAvailable() {
        try {
            final URL url = new URL("https://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns all changelogs in a formatted string.
     * <p>
     * This method retrieves all versions and their associated changelogs from the
     * changelogsReader and constructs a formatted string containing all the
     * changelogs.
     *
     * @return a string containing all the changelogs, formatted with version
     *         numbers and associated changelogs
     */
    public static String showAllChangelogs() {
        StringBuilder changeLogsBuilder = new StringBuilder();
        List<String> preReleaseVersions = changelogsReader.getPreReleaseVersions();
        List<String> versions = changelogsReader.getVersions();
        versions.removeIf(s -> s.contains("PRE-RELEASE"));
        Collections.reverse(versions);
        Collections.reverse(preReleaseVersions);

        // Iterate through each version and its associated changelogs
        changeLogsBuilder.append("Release Versions: ").append(System.lineSeparator());
        versions.forEach(version -> {
            Changelog changelog = changelogsReader.getChangelogs(version);
            if (changelog != null) {
                StringBuilder changeLogBuilder = new StringBuilder();

                // Retrieve changelog details
                List<String> bugFixes = changelog.getBugFixes();
                List<String> performanceFixes = changelog.getPerformanceFixes();
                List<String> features = changelog.getFeatures();

                // Calculate max length for the separator line
                int maxLength = Math.max(
                        bugFixes.stream().mapToInt(String::length).max().orElse(0),
                        Math.max(
                                performanceFixes.stream().mapToInt(String::length).max().orElse(0),
                                features.stream().mapToInt(String::length).max().orElse(0)));

                // Create separator line
                // Generate a separator of '-' repeated (maxLength + 10) times
                String separator = new String(new char[maxLength + 10]).replace('\0', '-');

                // Build the changelog section for the current version
                changeLogBuilder.append("\n").append(separator).append("\n");
                changeLogBuilder.append("Changelogs for Version ").append(version).append("\n");
                changeLogBuilder.append(separator).append("\n");

                if (!bugFixes.isEmpty()) {
                    changeLogBuilder.append("Bug Fixes:\n");
                    bugFixes.forEach(bugFix -> changeLogBuilder.append(" - ").append(bugFix).append("\n"));
                }

                if (!performanceFixes.isEmpty()) {
                    changeLogBuilder.append("\nPerformance Fixes:\n");
                    performanceFixes.forEach(
                            performanceFix -> changeLogBuilder.append(" - ").append(performanceFix).append("\n"));
                }

                if (!features.isEmpty()) {
                    changeLogBuilder.append("\nFeatures:\n");
                    features.forEach(feature -> changeLogBuilder.append(" - ").append(feature).append("\n"));
                }

                changeLogBuilder.append(separator).append("\n");

                // Append the changelogs of the current version to the main builder
                changeLogsBuilder.append(changeLogBuilder);
            }
        });

        // Add pre-release versions to the main builder
        changeLogsBuilder.append(System.lineSeparator()).append(System.lineSeparator())
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Pre Releases: ").append(System.lineSeparator());
        preReleaseVersions.forEach(version -> {
            Changelog changelog = changelogsReader.getChangelogs(version);
            if (changelog != null) {
                StringBuilder changeLogBuilder = new StringBuilder();

                // Retrieve changelog details
                List<String> bugFixes = changelog.getBugFixes();
                List<String> performanceFixes = changelog.getPerformanceFixes();
                List<String> features = changelog.getFeatures();

                // Calculate max length for the separator line
                int maxLength = Math.max(
                        bugFixes.stream().mapToInt(String::length).max().orElse(0),
                        Math.max(
                                performanceFixes.stream().mapToInt(String::length).max().orElse(0),
                                features.stream().mapToInt(String::length).max().orElse(0)));

                // Create separator line
                // Generate a separator of '-' repeated (maxLength + 10) times
                String separator = new String(new char[maxLength + 10]).replace('\0', '-');

                // Build the changelog section for the current version
                changeLogBuilder.append("\n").append(separator).append("\n");
                changeLogBuilder.append("Changelogs for Version ").append(version).append("\n");
                changeLogBuilder.append(separator).append("\n");

                if (!bugFixes.isEmpty()) {
                    changeLogBuilder.append("Bug Fixes:\n");
                    bugFixes.forEach(bugFix -> changeLogBuilder.append(" - ").append(bugFix).append("\n"));
                }

                if (!performanceFixes.isEmpty()) {
                    changeLogBuilder.append("\nPerformance Fixes:\n");
                    performanceFixes.forEach(
                            performanceFix -> changeLogBuilder.append(" - ").append(performanceFix).append("\n"));
                }

                if (!features.isEmpty()) {
                    changeLogBuilder.append("\nFeatures:\n");
                    features.forEach(feature -> changeLogBuilder.append(" - ").append(feature).append("\n"));
                }

                changeLogBuilder.append(separator).append("\n");

                // Append the changelogs of the current version to the main builder
                changeLogsBuilder.append(changeLogBuilder);
            }
        });

        return changeLogsBuilder.toString();
    }

    /**
     * Display the changelogs for the current version and mark the version as shown.
     * <p>
     * This method retrieves the changelogs for the current version from the
     * changelogsReader and displays them in a dialog window.
     * It also sets the "show-changelogs" property to "true" and saves the settings.
     */
    public static void showChangelogs() {
        String changeLogs;
        if (branch.equalsIgnoreCase("release")) {
            changeLogs = changelogsReader.getChangelogAsString(VERSION) +
                    "Version Released? : " + changelogsReader.isVersionReleased(VERSION);
        } else {
            changeLogs = changelogsReader.getChangelogAsString(preRelease) +
                    "Version Released? : " + changelogsReader.isVersionReleased(preRelease);
        }
        JOptionPane.showMessageDialog(null, changeLogs, "Changelogs", JOptionPane.PLAIN_MESSAGE);
        Main.settings.set("show-changelogs", true);
        Main.settings.save();
    }

    public static String getFileSuffix(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ""; // No suffix found
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Deletes old versions of the application files from the directory.
     * It iterates through the files in the directory and deletes any file that
     * matches the criteria for an old version.
     */
    public static boolean deleteOldVersion() {
        File dir = new File(getFilePath());
        File[] files = dir.listFiles();

        if (files == null) {
            return true;
        }

        for (File file : files) {
            String fileName = file.getName();

            if ((fileName.endsWith(".exe") || fileName.endsWith(".jar")) && fileName.startsWith("MetarAPP")) {
                String suffix = getFileSuffix(fileName); // e.g., "jar" or "exe"
                boolean isCurrentVersion = false;

                String expectedFileName = "";
                if ("release".equalsIgnoreCase(branch)) {
                    expectedFileName = "MetarAPP-" + VERSION + "." + suffix;
                } else if ("pre-release".equalsIgnoreCase(branch)) {
                    expectedFileName = "MetarAPP-" + preRelease + "." + suffix;
                }

                isCurrentVersion = fileName.equalsIgnoreCase(expectedFileName);

                System.out.println(
                        "Checking: " + fileName + " | Expected: " + expectedFileName + " | Match: " + isCurrentVersion);

                if (!isCurrentVersion && file.isFile()) {
                    System.out.println("Deleting old version: " + fileName);
                    if (!file.delete()) {
                        JOptionPane.showMessageDialog(null, "Error while deleting old version: " + fileName);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Deletes old versions of the application files from the directory and starts
     * the new version.
     * <p>
     * This method iterates through the files in the directory, identifies the old
     * versions, and deletes them.
     * It then downloads the new version from the specified URL, saves it to the
     * directory, and starts the new version.
     *
     * @throws RuntimeException if an I/O error occurs during the deletion of old
     *                          versions or the download of the new version.
     */
    public static void deleteOldVersionAndStartNew() {
        // Your existing code to delete old versions
        // Get the list of files in the directory
        deleteOldVersion();

        String fileUrl;
        String extension;
        String version = getNewVersion();
        if (branch.equalsIgnoreCase("pre-release"))
            version = getLatestPreRelease();
        if (isOs(SystemUtils.OSType.WINDOWS)) {
            fileUrl = "https://framedev.ch/files/metarapp/windows/MetarAPP-" + version + ".exe";
            extension = ".exe";
        } else {
            fileUrl = "https://framedev.ch/files/metarapp/unix/MetarAPP-" + version + ".jar";
            extension = ".jar";
        }
        InputStream in;
        try {
            in = new URL(fileUrl).openStream();
            Files.copy(in, new File(getFilePath() + "MetarAPP-" + version + extension).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String newFilePath = getFilePath() + "MetarAPP-" + version;

        // Check if the new version is an EXE or JAR file and append the appropriate
        // extension
        if (new File(newFilePath + ".exe").exists()) {
            newFilePath += ".exe";
        } else if (new File(newFilePath + ".jar").exists()) {
            newFilePath += ".jar";
        } else {
            // Handle the case if neither EXE nor JAR file exists,
            // You may want to display an error message or take appropriate action
            System.out.println("New version file not found!");
            loggerUtils.addLog("New version file not found");
            logger.error("New version file not found!");
        }

        // Now start the new version of your application
        File newVersionFile = new File(newFilePath); // Adjust this to get the path of your new version file
        if (Desktop.isDesktopSupported() && newVersionFile.exists()) {
            try {
                Desktop.getDesktop().open(newVersionFile);
                loggerUtils.addLog("Updated Successfully!");
                System.exit(1);
            } catch (IOException e) {
                logger.log(Level.ERROR, ErrorMessages.getErrorNewVersionOpening(), e);
                loggerUtils.addLog(ErrorMessages.getErrorNewVersionOpening() + ": " + e.getMessage());
            }
        }
    }

    /**
     * This method retrieves the file path of the current class.
     * It checks the operating system type and constructs the file path accordingly.
     *
     * @return the file path of the current class
     * @throws RuntimeException if an error occurs during the construction of the
     *                          file path
     */
    public static String getFilePath() {
        try {
            if (new SystemUtils().getOSType() == SystemUtils.OSType.WINDOWS) {
                // For Windows, append a backslash at the end of the file path
                return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()
                        .getPath() + "\\";
            } else {
                // For other operating systems (like macOS, Linux, or others), append a forward
                // slash at the end of the file path
                return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()
                        .getPath() + "/";
            }
        } catch (URISyntaxException e) {
            // If an error occurs during the construction of the file path, throw a
            // RuntimeException
            throw new RuntimeException(e);
        }
    }

    public static boolean hasUpdate() {
        if (branch.equalsIgnoreCase("release"))
            try {
                if (UpdateHandler.getLatestBuildNumber() != null) {
                    return !UpdateHandler.getLatestBuildNumber().equalsIgnoreCase(BUILD_NUMBER);
                }
            } catch (Exception ex) {
                return !getNewVersion().equalsIgnoreCase(VERSION);
            }
        else
            return hasUpdatePreRelease();
        return false;
    }

    public static boolean hasUpdatePreRelease() {
        try {
            if (UpdateHandler.getLatestPreRelease() != null) {
                return !UpdateHandler.getLatestPreRelease().equalsIgnoreCase(preRelease);
            }
        } catch (Exception ex) {
            return !getLatestPreRelease().equalsIgnoreCase(preRelease);
        }
        return false;
    }

    /**
     * This method retrieves the latest version number from the specified URL.
     *
     * @return the latest version number as a string
     */
    public static String getNewVersion() {
        try {
            return UpdateHandler.getLatestVersion();
        } catch (IOException ex) {
            // Log the error message and rethrow the exception
            logger.log(Level.ERROR, ErrorMessages.getErrorShowNewVersion(), ex);
            loggerUtils.addLog(ErrorMessages.getErrorShowNewVersion());
        }

        // If an error occurs, return the current version number
        return VERSION;
    }

    public static String getLatestPreRelease() {
        return UpdateHandler.getLatestPreRelease();
    }

    /**
     * Update the Program
     *
     * @return return if update is available and successfully Downloaded
     */
    public static CompletableFuture<Boolean> update() {
        if (hasUpdate()) {
            if (branch.equalsIgnoreCase("release")) {
                return download(getNewVersion());
            } else if (branch.equalsIgnoreCase("pre-release")) {
                return download(getLatestPreRelease());
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    public static CompletableFuture<Boolean> download(String release) {
        if (new SystemUtils().getOSType() == SystemUtils.OSType.WINDOWS) {
            StringBuilder sb = new StringBuilder();
            sb.append(getFilePath());
            sb.deleteCharAt(getFilePath().length() - 1);
            String result = sb.toString();
            FileDownloader downloader = new FileDownloader(
                    "https://framedev.ch/files/metarapp/windows/MetarAPP-" + release + ".exe", result,
                    "MetarAPP-" + release + ".exe");
            return downloader.startDownload();
            // download("https://framedev.ch/files/metarapp/windows/MetarAPP-" + release +
            // ".exe", result, "MetarAPP-" + release + ".exe");
        } else if (new SystemUtils().getOSType() == SystemUtils.OSType.MACOS
                || new SystemUtils().getOSType() == SystemUtils.OSType.LINUX
                || new SystemUtils().getOSType() == SystemUtils.OSType.OTHER) {
            StringBuilder sb = new StringBuilder();
            sb.append(getFilePath());
            sb.deleteCharAt(getFilePath().length() - 1);
            String result = sb.toString();
            FileDownloader downloader = new FileDownloader(
                    "https://framedev.ch/files/metarapp/unix/MetarAPP-" + release + ".jar", result,
                    "MetarAPP-" + release + ".jar");
            return downloader.startDownload();
            // download("https://framedev.ch/files/metarapp/unix/MetarAPP-" + release +
            // ".jar", result, "MetarAPP-" + release + ".jar");
        }
        return CompletableFuture.completedFuture(false);
    }

    public static void download(String fileUrl, String location, String fileNameWithExtensions) {
        File file;
        if (location != null) {
            file = new File(location, fileNameWithExtensions);
            if (file.getParentFile() != null && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
        } else {
            file = new File(fileNameWithExtensions);
        }
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            URL url = new URL(fileUrl);
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(file);
            final byte[] data = new byte[4096];
            int count;
            while ((count = in.read(data, 0, 4096)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isOs(SystemUtils.OSType osType) {
        return new SystemUtils().getOSType() == osType;
    }

    /**
     * Returns the Logger
     *
     * @return returns the Logger
     */
    public static Logger getLogger() {
        return logger;
    }
}
