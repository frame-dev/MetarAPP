package ch.framedev.metarapp.cli.handlers;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.cli.utils.LocaleUtils;
import ch.framedev.simplejavautils.ConsoleColors;
import ch.framedev.simplejavautils.TextUtils;
import jline.console.ConsoleReader;
import org.apache.log4j.Level;

import static ch.framedev.metarapp.cli.Main.getLocaleUtils;
import static ch.framedev.metarapp.cli.Main.lastSearch;
import static ch.framedev.metarapp.cli.handlers.Handles.createLines;

/**
 * Represents a class for handling user input related to unit conversion.
 */
public class ConverterInput {

    // Instance variable to store the LocaleUtils object
    LocaleUtils localeUtils = Main.getLocaleUtils();

    /**
     * Constructs a new ConverterInput object and initiates the input handling process.
     */
    public ConverterInput() {
        // Initialize TextUtils for unit conversion
        TextUtils textUtils = new TextUtils();
        try (ConsoleReader reader = new ConsoleReader()) {
            while (true) {
                // Prompt the user for input
                reader.setPrompt(ConsoleColors.GREEN + "MetarAPP-CLI|" + ConsoleColors.YELLOW + "Converter» " + ConsoleColors.RESET);
                System.out.println(localeUtils.getString("questionConverter"));
                String answer = reader.readLine();
                String[] arg = null;
                System.out.println(createLines(20, "-"));
                if (answer.contains(" ")) {
                    // Split the input into arguments
                    arg = answer.split(" ");
                }
                if (answer.equalsIgnoreCase("back")) {
                    break;
                } else if(answer.equalsIgnoreCase("exit")) {
                    System.out.println("Application will be terminated.");
                    lastSearch.save();
                    System.out.println("Application exited!");
                    Handles.exit = true;
                    break;
                } else if (arg != null && arg[0].equalsIgnoreCase("milestometers") && arg.length == 2) {
                    // Convert miles to meters and display the result
                    double miles = Double.parseDouble(arg[1]);
                    System.out.println("Meters = " + textUtils.milesToMeters(miles));
                } else if (arg != null && arg[0].equalsIgnoreCase("meterstomiles") && arg.length == 2) {
                    // Convert meters to miles and display the result
                    double meters = Double.parseDouble(arg[1]);
                    System.out.println("Miles = " + textUtils.metersToMiles(meters));
                } else if (answer.equalsIgnoreCase("help")) {
                    // Display help information
                    handleHelp();
                } else {
                    System.out.println(getLocaleUtils().getString("commandNotFound", "%Answer", answer));
                }
                System.out.println(createLines(20, "="));
                reader.flush();
            }
        } catch (Exception ex) {
            // Log any errors that occur during input handling
            Main.getLogger().log(Level.ERROR, "An error occurred", ex);
        }
    }

    /**
     * Displays help information for unit conversion commands.
     */
    public void handleHelp() {
        // Retrieve the list of help information for unit conversion commands and print each item
        localeUtils.getStringList("converterHelp").forEach(System.out::println);
    }
}
