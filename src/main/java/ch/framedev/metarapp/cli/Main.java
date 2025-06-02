package ch.framedev.metarapp.cli;

import ch.framedev.metarapp.cli.data.LastSearch;
import ch.framedev.metarapp.cli.data.ListOfIcaos;
import ch.framedev.metarapp.cli.data.Remember;
import ch.framedev.metarapp.cli.data.UserData;
import ch.framedev.metarapp.cli.handlers.Handles;
import ch.framedev.metarapp.cli.requests.AirportRequest;
import ch.framedev.metarapp.cli.requests.MetarRequest;
import ch.framedev.metarapp.cli.utils.Database;
import ch.framedev.metarapp.cli.utils.LoadCountryCode;
import ch.framedev.metarapp.cli.utils.LocaleNotFoundException;
import ch.framedev.metarapp.cli.utils.LocaleUtils;
import ch.framedev.metarapp.cli.utils.TypeIndex;
import ch.framedev.metarapp.cli.utils.UpdateService;
import ch.framedev.metarapp.cli.utils.Variables;
import ch.framedev.simplejavautils.*;
import ch.framedev.yamlutils.FileConfiguration;
import jline.console.ConsoleReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import ch.framedev.metarapp.cli.utils.Locale;

public class Main {

    public static String VERSION = "1.2.5";
    public static FileConfiguration settings;
    public static LastSearch lastSearch;
    public static Database database;
    public static boolean loggedIn = false, TESTING = false;
    public static String userName, password;
    public static SimpleJavaUtils utils = new SimpleJavaUtils();

    public static UserData userData;
    public static LoadCountryCode loadCountryCode;

    private static final String penguinMacClearConsole = "clear";
    private static final String windowsClearConsole = "cls";
    private static final String os = System.getProperty("os.name");
    private static final Logger logger = Logger.getLogger("MetarAPP");

    private static LocaleUtils localeUtils;

    /**
     * The main entry point of the application.
     * Configures the basic logging, initializes the database, sets up necessary
     * files and settings, creates required files, and handles command line
     * arguments.
     * If the command line arguments indicate a download operation, it downloads
     * Metar and Airport data for the specified ICAO code and exits the application.
     * Otherwise, it initializes the ConsoleReader, deletes old application
     * versions, sets the prompt, handles user login, and processes user commands.
     *
     * @param args the command line arguments passed to the application
     * @throws IOException if an I/O error occurs during the execution of the main
     *                     method
     */
    public static void main(String[] args) throws IOException, LocaleNotFoundException {
        BasicConfigurator.configure(); // Configures basic logging
        getLogger().info("Application starting..."); // Logs the application starting message
        getLogger().info("Current Directory is » " + getFilePath()); // Logs the current directory information
        setupFiles(); // Sets up necessary files and folders
        setupSettings(); // Sets up the application settings
        createFiles(); // Creates necessary files and folders required for the application
        setupLocales();
        database = new Database(); // Initializes the database
        // Saves the application settings
        if (!((String) settings.get("customLocaleFilePath")).equalsIgnoreCase("your-file-path.yml")) {
            localeUtils = new LocaleUtils(String.valueOf(settings.get("customLocaleFilePath")));
        }
        try {
            localeUtils = new LocaleUtils(
                    Objects.requireNonNull(Locale.fromLocale((String) settings.get("locale"))));
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Something went wrong!", ex);
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("download")) { // Checks if download operation is requested
            if (exists(args[0])) { // Checks if the specified airport exists
                new MetarRequest(args[0]).downloadAsJson(true); // Downloads Metar data as JSON
                new AirportRequest(args[0]).downloadAsJson(true); // Downloads Airport data as JSON
                String downloadMessage = localeUtils.getString("download-to-folder", "%s",
                        settings.get("download-folder"));
                System.out.println(downloadMessage); // Prints success message
                System.exit(1); // Exits the application
            } else {
                System.out.println(
                        ConsoleColors.RED + "The airport '" + args[0] + "' does not exist!" + ConsoleColors.RESET);
                System.exit(1); // Exits the application
            }
        }

        /**if (UpdateService.hasUpdate()) {
            String[] patterns = { "%NEWVERSION%", "%OLDVERSION%" };
            String[] data = { UpdateService.getLatestVersion(), VERSION };
            List<String> infoUpdate = getLocaleUtils().getStringListReplace("new-version-available", patterns, data);
            new TextUtils().printBox(infoUpdate.toArray(new String[0]));
        }*/

        loadCountryCode = new LoadCountryCode();
        try (ConsoleReader reader = new ConsoleReader()) { // Initializes the ConsoleReader in a try-with-resources
            // block
            UpdateService.deleteOldVersions(); // Deletes old application versions
            reader.setPrompt(ConsoleColors.GREEN + "MetarAPP-CLI» " + ConsoleColors.RESET); // Sets the prompt for user
            // input
            handleLogin(reader); // Handles the user login process
            System.out.println("Language " + ConsoleColors.GREEN + "(" + localeUtils.getString("language") + ")"
                    + ConsoleColors.YELLOW + " | [File] | " + ConsoleColors.GREEN + localeUtils.getFile().toPath()
                    + ConsoleColors.RESET + " has been selected!"); // Prints Current Locale message
            Handles.handleCommands(reader); // Handles the user commands and interactions
        } catch (Exception ex) { // Catches any exceptions that occur during the execution
            logger.log(Level.ERROR, "Something went wrong!", ex); // Logs the error message
        }
    }

    /**
     * Sets up the necessary files and folders for the application.
     * This method checks for the existence of the "files" folder and the
     * "settings.properties" file within the application file path.
     * If the "files" folder does not exist, it attempts to create the folder and
     * logs an error if the creation fails.
     * If the "settings.properties" file does not exist, it attempts to create the
     * file and logs an error if the creation fails.
     *
     * @throws IOException if an I/O error occurs during the file setup process
     */
    private static void setupFiles() throws IOException {
        if (!new File(getFilePath(), "files").mkdir()) {
            if (!new File(getFilePath(), "files").exists()) {
                logger.log(Level.ERROR, "files folder could not be created!");
            }
        }
        if (!new File(getFilePath() + "files/settings.properties").createNewFile())
            if (!new File(getFilePath() + "files/settings.properties").exists())
                logger.log(Level.ERROR, "settings.properties could not be created!");
    }

    /**
     * Sets up the locales for the application by creating the necessary folders and
     * copying locale resource files.
     * If the "locales" folder does not exist, it creates the folder and its parent
     * directories.
     * Then, it copies the English and German locale resource files from the
     * application's resources to the specified file path, replacing existing files
     * if necessary.
     *
     * @throws IOException if an I/O error occurs during the setup of locales
     */
    public static void setupLocales() throws IOException {
        // Check if the "locales" folder exists, create it if it doesn't
        if (!new File(getFilePath() + "locales").exists()) {
            if (!new File(getFilePath() + "locales").mkdirs())
                throw new IOException("Could not create the " + getFilePath() + "locales folder");
        }
        // Copy English locale resource file to the specified file path, replacing
        // existing file if necessary
        Files.copy(utils.getFromResourceFile("locale/en-En.yml", Main.class).toPath(),
                new File(getFilePath() + "locales/en-En.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
        // Copy German locale resource file to the specified file path, replacing
        // existing file if necessary
        Files.copy(utils.getFromResourceFile("locale/de-De.yml", Main.class).toPath(),
                new File(getFilePath() + "locales/de-De.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
        // Copy French locale resource file to the specified file path, replacing
        // existing file if necessary
        Files.copy(utils.getFromResourceFile("locale/fr-Fr.yml", Main.class).toPath(),
                new File(getFilePath() + "locales/fr-Fr.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);// Copy
                                                                                                             // French
                                                                                                             // locale
                                                                                                             // resource
                                                                                                             // file to
                                                                                                             // the
                                                                                                             // specified
                                                                                                             // file
                                                                                                             // path,
                                                                                                             // replacing
        // Copy Russia locale resource file to the specified file path, replacing
        // existing file if necessary
        Files.copy(utils.getFromResourceFile("locale/ru-Ru.yml", Main.class).toPath(),
                new File(getFilePath() + "locales/ru-Ru.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Creates necessary files and folders required for the application to function
     * properly.
     * This method checks for the existence of specific files and folders and
     * creates them if they do not exist.
     * It also copies resource files from the application's resources to the
     * specified file path if they are not already present.
     *
     * @throws IOException if an I/O error occurs during the file creation or
     *                     copying process
     */
    private static void createFiles() throws IOException {
        // Check and create README.md file
        if (!new File(getFilePath() + Variables.DIRECTORY_FOLDER, "README.md").exists())
            Files.copy(utils.getFromResourceFile("cli/README.md", Main.class).toPath(),
                    new File(getFilePath() + Variables.DIRECTORY_FOLDER, "README.md").toPath());

        // Check and create iata-icao.csv file
        if (!new File(getFilePath() + Variables.DIRECTORY_FOLDER, "iata-icao.csv").exists()) {
            try {
                Files.copy(utils.getFromResourceFile("csvFiles/iata-icao.csv", Main.class).toPath(),
                        new File(getFilePath() + Variables.DIRECTORY_FOLDER, "iata-icao.csv").toPath());
            } catch (IOException e) {
                logger.log(Level.ERROR, "File could not be Moved!", e);
            }
        }

        // Check and create CountryCodes.csv file
        if (!new File(getFilePath() + Variables.DIRECTORY_FOLDER, "CountryCodes.csv").exists()) {
            try {
                Files.copy(utils.getFromResourceFile("csvFiles/CountryCodes.csv", Main.class).toPath(),
                        new File(getFilePath() + Variables.DIRECTORY_FOLDER, "CountryCodes.csv").toPath());
            } catch (IOException e) {
                logger.log(Level.ERROR, "File could not be Moved!", e);
            }
        }

        // Check and create DOWNLOAD_FOLDER if it does not exist
        if (!new File(getFilePath() + Variables.DOWNLOAD_FOLDER).exists())
            if (!new File(getFilePath() + Variables.DOWNLOAD_FOLDER).mkdir())
                logger.log(Level.ERROR, "Download folder could not be created!");
    }

    /**
     * Sets up the application settings by initializing the settings object with the
     * properties file located at the specified file path.
     * Retrieves the value of the "testing" property from the settings and assigns
     * it to the TESTING variable.
     */
    private static void setupSettings() {
        if (!new File(getFilePath() + "files", "settings.yml").exists()) {
            new File(getFilePath() + "files", "settings.yml").getParentFile().mkdir();
            try {
                new File(getFilePath() + "files", "settings.yml").createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        settings = new FileConfiguration(utils.getFromResourceFile("cli/settings.yml", Main.class),
                new File(getFilePath() + "files", "settings.yml"));
        settings.load();
    }

    /**
     * Handles the user login process based on the application settings and user
     * input.
     * If the remember option is set to true in the settings, the method calls the
     * rememberLogin method to log in the user using stored credentials.
     * The method then retrieves the operating system type and version and prompts
     * the user for login if the remember option is set to false or if the user is
     * not already logged in.
     * If the user has not chosen to not be asked again, the method calls the
     * askRemember method to prompt the user to choose whether to remember the login
     * credentials.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs during the login process
     */
    public static void handleLogin(ConsoleReader reader) throws IOException {
        if ((boolean) settings.get("remember")) {
            rememberLogin();
        }
        System.out.println(new SystemUtils().getOSType() + " : " + System.getProperty("os.version"));
        if (!(boolean) settings.get("remember") || !loggedIn) {
            loginReader(reader);
        }
        if (!new Remember().getDontAskBoolean()) {
            askRemember(reader);
        }
    }

    /**
     * Logs in the user with the specified username and password.
     * If the provided username and password match the credentials stored in the
     * database, the user is successfully logged in.
     * Upon successful login, the loggedIn flag is set to true, a new LastSearch
     * object is initialized for the user, and the password is stored for future
     * use.
     *
     * @param scanner the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs during the login process
     */
    public static void login(ConsoleReader scanner) throws IOException {
        System.out.println("UserName?");
        userName = scanner.readLine();
        System.out.println("Password?");
        String password = scanner.readLine();
        if (database.isUserRight(userName, password)) {
            System.out.println(localeUtils.getString("loggedIn"));
            loggedIn = true;
            lastSearch = new LastSearch(userName);
            Main.password = password;
        }
    }

    /**
     * Logs in the user with the specified username and password.
     * If the provided username and password match the credentials stored in the
     * database, the user is successfully logged in.
     * Upon successful login, the loggedIn flag is set to true, a new LastSearch
     * object is initialized for the user, and the password is stored for future
     * use.
     *
     * @param userName the username of the user attempting to log in
     * @param password the password of the user attempting to log in
     */
    public static void login(String userName, String password) {
        if (database.isUserRight(userName, password)) {
            System.out.println(localeUtils.getString("loggedIn"));
            loggedIn = true;
            lastSearch = new LastSearch(userName);
            Main.password = password;
        }
    }

    /**
     * Checks the existence of a specified ICAO code in the list of ICAO codes.
     * Retrieves the ICAO code from the list of ICAOs and checks if it exists based
     * on the specified type index and ICAO code.
     *
     * @param icao the ICAO code to be checked for existence
     * @return true if the specified ICAO code exists in the list of ICAOs, false
     *         otherwise
     */
    public static boolean exists(String icao) {
        return new ListOfIcaos().getICAO(TypeIndex.ICAO, icao) != null;
    }

    /**
     * Retrieves the file path of the application based on the operating system.
     * If the operating system is Windows, the method returns the file path with a
     * backslash as the separator.
     * For other operating systems, the method returns the file path with a forward
     * slash as the separator.
     * If an error occurs while getting the file path, a runtime exception is thrown
     * with the original exception as the cause.
     *
     * @return the file path of the application
     */
    public static String getFilePath() {
        try {
            if (new SystemUtils().getOSType() == SystemUtils.OSType.WINDOWS) {
                return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()
                        .getPath() + "\\";
            } else {
                return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()
                        .getPath() + "/";
            }
        } catch (URISyntaxException e) {
            logger.log(Level.ERROR, "Error while getting File Path", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the terminal screen and buffer using the provided ConsoleReader
     * object.
     * This method sets the system property "org.jline.terminal.dumb" to "true",
     * clears the screen and buffer using the clearScreen method of the
     * ConsoleReader,
     * flushes the output stream, enables history, and executes the appropriate
     * command to clear the terminal screen based on the operating system.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs while clearing the terminal
     */
    public static void clearTerminal(ConsoleReader reader) throws IOException {
        System.setProperty("org.jline.terminal.dumb", "true");
        reader.clearScreen();
        reader.flush();
        reader.setHistoryEnabled(true);
        if (!os.startsWith("Windows")) {
            Runtime.getRuntime().exec(new String[] { penguinMacClearConsole }); // Executes the command to clear the
            // terminal screen on
            // non-Windows systems
        } else {
            Runtime.getRuntime().exec(new String[] { windowsClearConsole }); // Executes the command to clear the
            // terminal screen on
            // Windows systems
        }
    }

    /**
     * Prompts the user to choose whether to remember the login credentials, and
     * handles the corresponding actions based on the user's choice.
     * If the user chooses to remember the login, the method updates the settings
     * and saves the remember option.
     * If the user chooses not to remember the login, the password is set to null
     * for security reasons.
     * If the user chooses not to be asked again, a new Remember object is created
     * with the specified username, password, and true for the dontAskBoolean, and
     * the object is saved.
     * If the remember option is set to true, a new Remember object is created with
     * the specified username and password, and the object is saved.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs
     */
    public static void askRemember(ConsoleReader reader) throws IOException {
        System.out.println(localeUtils.getString("rememberAsk"));
        String rememberBoolean = reader.readLine();
        if (rememberBoolean.equalsIgnoreCase("true") || rememberBoolean.equalsIgnoreCase("false")) {
            settings.set("remember", Boolean.parseBoolean(rememberBoolean));
            settings.save();
            if (rememberBoolean.equalsIgnoreCase("false")) {
                password = null;
            }
        } else if (rememberBoolean.equalsIgnoreCase("dontask")) {
            settings.set("remember", true);
            settings.save();
        }
        if (rememberBoolean.equalsIgnoreCase("dontask")) {
            Remember remember = new Remember(userName, password, true);
            remember.save();
            password = null;
        } else if ((boolean) settings.get("remember")) {
            Remember remember = new Remember(userName, password);
            remember.save();
            password = null;
        }
        System.out.println();
        clearTerminal(reader);
    }

    /**
     * Handles the user login or registration process based on the user input.
     * Prompts the user to choose between login, register, or resetpassword options
     * and performs the corresponding action based on the user's choice.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs
     */
    public static void loginReader(ConsoleReader reader) throws IOException {
        System.out.println("Login or Register? (login, register, resetpassword)");
        String answerLogin = reader.readLine();
        switch (answerLogin) {
            case "login":
                login(reader); // Calls the login method to handle user login
                break;
            case "register":
                System.out.println("UserName?");
                String userNameRegister = reader.readLine();
                System.out.println("Password?");
                String passwordRegister = reader.readLine();
                try {
                    if (database.createAccount(userNameRegister, new PasswordHasher().hashPassword(passwordRegister))) {
                        System.out.println(passwordRegister);
                        System.out.println(localeUtils.getString("registered"));
                        login(reader); // After successful registration, prompts the user to log in
                    }
                } catch (SQLException e) {
                    Logger.getLogger("MetarAPP-CLI").log(Level.ERROR, "Error while creating Account", e);
                    System.out.println("Something went wrong!");
                }
                break;
            case "resetpassword":
                resetPassword(reader); // Calls the resetPassword method to handle password reset
                break;
            default:
                System.out.println("Invalid option. Please choose 'login', 'register', or 'resetpassword'.");
        }
    }

    /**
     * Logs in the user using the stored credentials from the Remember object.
     * Sets the loggedIn flag to true, retrieves the username and password from the
     * Remember object,
     * logs in the user using the retrieved credentials, initializes the LastSearch
     * object for the user,
     * and sets the password to null for security reasons.
     */
    public static void rememberLogin() {
        loggedIn = true;
        userName = new Remember().getUserName(); // Retrieve the username from the Remember object
        password = new Remember().getPassword(); // Retrieve the password from the Remember object
        login(userName, password); // Log in the user using the retrieved credentials
        lastSearch = new LastSearch(userName); // Initialize the LastSearch object for the user
        password = null; // Set the password to null for security reasons
    }

    /**
     * Resets the password for a user in the system.
     * Prompts the user for their username, old password, and new password, and
     * updates the password in the database if the old password matches the hashed
     * password from the database.
     * If the old password matches, the user is prompted to enter a new password,
     * and the password is updated in the database.
     * If the password update is successful, a success message is displayed, and the
     * user is prompted to log in with the new password.
     * If the password update fails, an error message is displayed, and the
     * application exits with status code 2.
     * If the old password does not match the one stored in the database, an error
     * message is displayed, and the application exits with status code 2.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs
     */
    public static void resetPassword(ConsoleReader reader) throws IOException {
        System.out.println("Username?");
        String userNameReset = reader.readLine();

        // Prompt for old password
        System.out.println("Old Password?");
        String oldPassword = reader.readLine();

        // Retrieve hashed password from the database for the specified username
        byte[] hashedPasswordFromDB = database.getPassword(userNameReset);

        // Verify if the old password matches the hashed password from the database
        if (hashedPasswordFromDB != null && new PasswordHasher().verifyPassword(oldPassword, hashedPasswordFromDB)) {
            // Proceed with password reset
            // Prompt for new password and update the password in the database
            System.out.println("New Password?");
            String newPassword = reader.readLine();

            // Update the password in the database
            if (database.changePassword(userNameReset, oldPassword, newPassword)) {
                System.out.println("Successfully Changed Password!");
                System.out.println("You need to Login with the new password.");
                login(reader);
            } else {
                System.out.println("Failed to change password. Please try again later.");
                System.exit(2);
            }
        } else {
            // Password provided does not match the one stored in the database
            System.out.println("Password is wrong! Please contact the administrator.");
            System.exit(2);
        }
    }

    /**
     * Retrieves the logger instance for the MetarAPP application.
     * This method returns the logger instance used for logging application events
     * and messages.
     *
     * @return the logger instance for the MetarAPP application
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Retrieves the LocaleUtils instance for the MetarAPP application.
     * This method returns the LocaleUtils instance used for managing
     * locale-specific strings and resources.
     *
     * @return the LocaleUtils instance for the MetarAPP application
     */
    public static LocaleUtils getLocaleUtils() {
        return localeUtils;
    }

    /**
     * Retrieves the LocaleUtils instance for the MetarAPP application.
     * This method returns the LocaleUtils instance used for managing
     * locale-specific strings and resources.
     *
     * @return the LocaleUtils instance for the MetarAPP application
     */
    public static void setLocaleUtils(LocaleUtils localeUtils) {
        Main.localeUtils = localeUtils;
    }
}
