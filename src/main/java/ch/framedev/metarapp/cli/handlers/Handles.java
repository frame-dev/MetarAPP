package ch.framedev.metarapp.cli.handlers;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.cli.MetarCLIAPI;
import ch.framedev.metarapp.cli.data.MetarData;
import ch.framedev.metarapp.cli.data.Remember;
import ch.framedev.metarapp.cli.data.UserData;
import ch.framedev.metarapp.cli.guis.AdminGUI;
import ch.framedev.metarapp.cli.guis.ConverterGui;
import ch.framedev.metarapp.cli.requests.AirportRequest;
import ch.framedev.metarapp.cli.requests.MetarRequest;
import ch.framedev.metarapp.cli.utils.Locale;
import ch.framedev.metarapp.cli.utils.LocaleNotFoundException;
import ch.framedev.metarapp.cli.utils.LocaleUtils;
import ch.framedev.metarapp.cli.utils.UpdateService;
import ch.framedev.simplejavautils.ConsoleColors;
import jline.console.ConsoleReader;
import org.apache.log4j.Level;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Handles {

    static boolean exit = false;

    /**
     * Handles the user commands and interactions based on the user input.
     * This method processes the user input and executes the corresponding commands
     * or actions based on the input provided by the user.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs during the command handling
     *                     process
     */
    public static void handleCommands(ConsoleReader reader) throws IOException, URISyntaxException {
        if (Main.loggedIn) {
            Main.userData = new UserData(Main.database.getID(Main.userName), Main.database.getIcaos(Main.userName), Main.userName);
            do {
                if (!exit) {
                    String message = Main.getLocaleUtils().getString("question");
                    System.out.println(message);
                    String answer = reader.readLine();
                    System.out.println(createLines(20, "-"));
                    String[] arg = null;
                    if (answer.contains(" ")) {
                        arg = answer.split(" ");
                    }
                    if (answer.equalsIgnoreCase("help")) {
                        handleHelpCommand();
                    } else if (answer.equalsIgnoreCase("exit")) {
                        System.out.println("Application will be terminated.");
                        Main.lastSearch.save();
                        System.out.println("Application exited!");
                        break;
                    } else if (arg != null && arg[0].equalsIgnoreCase("metar")) {
                        handleMetarCommand(arg);
                    } else if (arg != null && arg[0].equalsIgnoreCase("airport")) {
                        handleAirportCommand(arg);
                    } else if (arg != null && arg[0].equalsIgnoreCase("download")) {
                        handleDownloadCommand(arg);
                    } else if (answer.equalsIgnoreCase("lastsearch")) {
                        for (String last : Main.userData.getIcaos())
                            System.out.println(last);
                    } else if (answer.equalsIgnoreCase("clear")) {
                        Main.clearTerminal(reader);
                        System.out.println("Screen Cleared");
                    } else if (answer.equalsIgnoreCase("update")) {
                        if (!UpdateService.getLatestVersion().equalsIgnoreCase(Main.VERSION)) {
                            List<String> messages = Main.getLocaleUtils().getStringListReplace("download-new-version",
                                    new String[] { "%NEWVERSION" }, new Object[] { UpdateService.getLatestVersion() });
                            messages.forEach(System.out::println);
                            UpdateService.downloadLatestVersionAndStart();
                        } else
                            System.out.println(Main.getLocaleUtils().getString("noUpdate"));
                    } else if (answer.equalsIgnoreCase("info")) {
                        handleInfoCommand();
                    } else if (answer.equalsIgnoreCase("admin") || arg != null && arg.length == 2
                            && arg[0].equalsIgnoreCase("admin") && arg[1].equalsIgnoreCase("gui")) {
                        if (Main.userName.equalsIgnoreCase("admin")) {
                            if (arg != null && arg.length == 2 && arg[0].equalsIgnoreCase("admin")
                                    && arg[1].equalsIgnoreCase("gui")) {
                                if (Desktop.isDesktopSupported()) {
                                    new AdminGUI();
                                } else
                                    System.out.println(Main.getLocaleUtils().getString("desktopNotSupported"));
                            } else {
                                System.out.println("Users = " + Main.database.getAllUserNames());
                            }
                        } else {
                            System.out.println("You are not an Admin!");
                        }
                    } else if (answer.equalsIgnoreCase("logout")) {
                        handleLogout(reader);
                    } else if (answer.equalsIgnoreCase("converter") || arg != null && arg.length == 2
                            && arg[0].equalsIgnoreCase("converter") && arg[1].equalsIgnoreCase("gui")) {
                        if (arg != null && arg.length == 2 && arg[0].equalsIgnoreCase("converter")
                                && arg[1].equalsIgnoreCase("gui")) {
                            if (Desktop.isDesktopSupported()) {
                                new ConverterGui();
                            } else if (arg[0].equalsIgnoreCase("converter")) {
                                System.out.println(Main.getLocaleUtils().getString("desktopNotSupportedUseCLI"));
                                new ConverterInput();
                            }
                        } else {
                            new ConverterInput();
                        }
                    } else if (arg != null && arg.length >= 2 && arg[0].equalsIgnoreCase("locale")) {
                        if (arg[1].equalsIgnoreCase("list")) {
                            for (Locale locale : Locale.values())
                                if (locale != Locale.FALLBACK)
                                    System.out.println(locale.getLocale() + " (" + locale.getDescription() + ")");
                            continue;
                        }
                        handleLocale(arg[1]);
                    } else if (answer.equalsIgnoreCase("openmap")) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI("https://framedev.ch/files/metarapp/weather.html"));
                        } else {
                            System.out.println(Main.getLocaleUtils().getString("desktopNotSupported"));
                        }
                    } else if (answer.equalsIgnoreCase("showsettings")) {
                        showSettingsFile();
                    } else if (answer.equalsIgnoreCase("showcountrycodes")) {
                        System.out.println("Name,Code");
                        for (String[] country : Main.loadCountryCode.getCountryCodeList())
                            System.out.println(country[0] + "," + country[1]);
                    } else {
                        System.out.println(Main.getLocaleUtils().getString("commandNotFound", "%Answer", answer));
                    }
                    System.out.println(createLines(20, "="));
                    reader.flush();
                }
            } while (true);
        } else {
            System.out.println("You need to be Logged in!");
            System.exit(1);
        }
        System.exit(1);
    }

    public static String createLines(int amount, String symbol) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            line.append(symbol);
        }
        return line.toString();
    }

    /**
     * Handles the download command for retrieving Metar and Airport data for a
     * specified ICAO code and saving the data as JSON files.
     * If the specified ICAO code exists, the method initiates the download process
     * for Metar and Airport data based on the provided parameters.
     * If the third argument is provided and is either "true" or "false", the method
     * downloads the data with or without timestamps, respectively.
     * If the third argument is not provided or is invalid, the method downloads the
     * data without timestamps.
     * After downloading the data, the method updates the last search, adds the ICAO
     * code to the user's list of ICAOs, saves the last search, increments the count
     * of downloaded files for the user, and updates the user's list of ICAOs.
     * Finally, the method prints a success message indicating the successful
     * download to the specified folder.
     *
     * @param arg an array containing the command and its arguments
     * @throws IOException if an I/O error occurs during the download process
     */
    public static void handleDownloadCommand(String[] arg) throws IOException {
        if (Main.exists(arg[1])) {
            if (arg.length == 3) {
                if (arg[2] != null && arg[2].equalsIgnoreCase("false")
                        || arg[2] != null && arg[2].equalsIgnoreCase("true")) {
                    new MetarRequest(arg[1]).downloadAsJson(Boolean.parseBoolean(arg[2]));
                    new AirportRequest(arg[1]).downloadAsJson(Boolean.parseBoolean(arg[2]));
                } else {
                    new MetarRequest(arg[1]).downloadAsJson(false);
                    new AirportRequest(arg[1]).downloadAsJson(false);
                }
            } else {
                new MetarRequest(arg[1]).downloadAsJson(false);
                new AirportRequest(arg[1]).downloadAsJson(false);
            }
            Main.lastSearch.add(arg[1].toUpperCase());
            Main.database.addToIcaos(Main.userName, arg[1].toUpperCase());
            Main.lastSearch.save();
            Main.database.addFilesDownloaded(Main.userName, 1);
            if (!Main.userData.getIcaos().contains(arg[1].toUpperCase()))
                Main.userData.getIcaos().add(arg[1].toUpperCase());
            String downloadMessage = Main.getLocaleUtils().getString("download-to-folder", "%s",
                    Main.settings.get("download-folder"));
            System.out.println(downloadMessage);
        } else {
            System.out.println(Main.getLocaleUtils().getString("icaoNotFound", "%Icao", arg[1].toUpperCase()));
        }
    }

    /**
     * Displays the help information for using the MetarAPP-CLI application.
     * Provides instructions and usage guidelines for various commands and features
     * of the application, including metar, airport, download, lastsearch, update,
     * info, converter, and admin commands.
     * If the user is an admin, additional admin commands are also displayed.
     */
    public static void handleHelpCommand() {
        List<String> help = Main.getLocaleUtils().getStringList("help");
        help.forEach(System.out::println);
        if (Main.userName.equalsIgnoreCase("admin")) {
            System.out.println("For Admin use : <admin gui/admin>");
        }
    }

    /**
     * Handles the airport command for retrieving airport data based on the
     * specified ICAO code and displaying the data to the console.
     * If the specified ICAO code exists, the method retrieves the airport data
     * using the MetarCLIAPI and prints the data to the console.
     * After retrieving and displaying the airport data, the method updates the last
     * search, adds the ICAO code to the user's list of ICAOs, saves the last
     * search, and updates the user's list of ICAOs.
     * If the specified ICAO code does not exist, the method prints an error message
     * indicating that the ICAO code was not found.
     *
     * @param arg an array containing the command and its arguments
     * @throws IOException if an I/O error occurs during the retrieval or display of
     *                     airport data
     */
    public static void handleAirportCommand(String[] arg) throws IOException {
        if (Main.exists(arg[1])) {
            System.out.println(MetarCLIAPI.getInstance().getAirportData(arg[1].toUpperCase()));
            Main.lastSearch.add(arg[1].toUpperCase());
            Main.database.addToIcaos(Main.userName, arg[1].toUpperCase());
            Main.lastSearch.save();
            if (!Main.userData.getIcaos().contains(arg[1].toUpperCase()))
               Main.userData.getIcaos().add(arg[1].toUpperCase());
        } else {
            System.out.println(Main.getLocaleUtils().getString("icaoNotFound", "%Icao", arg[1].toUpperCase()));
        }
    }

    /**
     * Handles the Metar command for retrieving Metar data based on the specified
     * ICAO code and displaying the data to the console.
     * If the specified ICAO code exists, the method retrieves the Metar data using
     * the MetarCLIAPI and prints the data to the console.
     * After retrieving and displaying the Metar data, the method updates the last
     * search, adds the ICAO code to the user's list of ICAOs, saves the last
     * search, and updates the user's list of ICAOs.
     * If the specified ICAO code does not exist, the method prints an error message
     * indicating that the ICAO code was not found.
     *
     * @param arg an array containing the command and its arguments
     * @throws IOException if an I/O error occurs during the retrieval or display of
     *                     Metar data
     */
    public static void handleMetarCommand(String[] arg) throws IOException {
        if (Main.exists(arg[1])) {
            MetarData metarData = MetarCLIAPI.getInstance().getMetarData(arg[1]);
            System.out.println(metarData);
            Main.lastSearch.add(arg[1].toUpperCase());
            Main.database.addToIcaos(Main.userName, arg[1].toUpperCase());
            Main.lastSearch.save();
            if (!Main.userData.getIcaos().contains(arg[1].toUpperCase()))
                Main.userData.getIcaos().add(arg[1].toUpperCase());
        } else {
            System.out.println(Main.getLocaleUtils().getString("icaoNotFound", "%Icao", arg[1]));
        }
    }

    /**
     * Displays the application version and user information.
     * Prints the application version, user ID, username, availability of MetarAPP,
     * and the list of ICAO codes associated with the user.
     */
    public static void handleInfoCommand() {
        System.out.println("Application Version : " + Main.VERSION);
        System.out.println("Selected Locale : " + Main.getLocaleUtils().getLocale());
        System.out.println("User Information");
        System.out.println("Id : " + Main.userData.getId());
        System.out.println("Username : " + Main.userData.getUserName());
        System.out.println("Has MetarAPP : " + Main.userData.hasMetarAPP());
        System.out.println("Icaos : " + Main.userData.getIcaos());
    }

    /**
     * Handles the logout process for the user.
     * Sets the loggedIn flag to false, updates the Remember object to set the
     * dontAskBoolean to true, and saves the changes.
     * Clears the username and password fields, sets the remember property to
     * "false" in the settings, and saves the settings.
     * Invokes the handleLogin method to prompt the user for login or registration.
     *
     * @param reader the ConsoleReader object used for input/output operations
     * @throws IOException if an I/O error occurs
     */
    public static void handleLogout(ConsoleReader reader) throws IOException {
        Main.loggedIn = false;
        new Remember().setDontAskBoolean(true); // Update the Remember object to set the dontAskBoolean to true
        new Remember().save(); // Save the changes made to the Remember object
        Main.userName = ""; // Clear the username field
        Main.password = ""; // Clear the password field
        Main.settings.set("remember", false); // Set the remember property to "false" in the settings
        Main.settings.save(); // Save the settings
        Main.handleLogin(reader); // Prompt the user for login or registration
    }

    public static void handleLocale(String locale) {
        try {
            Main.setLocaleUtils(new LocaleUtils(Objects.requireNonNull(Locale.fromLocale(locale))));
            System.out.println(
                    Main.getLocaleUtils().getString("localeChanged", "%Locale", Main.getLocaleUtils().getString("language")));
            Main.settings.set("locale", locale);
           Main. settings.save();
        } catch (LocaleNotFoundException ex) {
            Main.getLogger().log(Level.ERROR, "Error while change locale", ex);
            System.out.println("Locale '" + locale + "' not found!");
            System.out.println("Use Fallback locale (en)");
            System.out.println(ConsoleColors.WHITE_BOLD + "To see all available Locales please type "
                    + ConsoleColors.GREEN_BOLD + "(locale list)" + ConsoleColors.RESET);
            Main.settings.set("locale", "fallback");
           Main.settings.save();
            try {
                Main.setLocaleUtils(new LocaleUtils(Locale.FALLBACK));
            } catch (LocaleNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void showSettingsFile() {
        for (Map.Entry<String, Object> entry : Main.settings.getData().entrySet()) {
            System.out.println("Key = " + entry.getKey() + " : " + " Value = " + entry.getValue());
        }
    }
}
