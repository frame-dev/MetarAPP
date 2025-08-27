package ch.framedev.metarapp.util;

import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.simplejavautils.SimpleJavaUtils;
import ch.framedev.yamlutils.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Currently supported languages are (en-En/de-De.yml)
 */
public class LocaleUtils {

    /**
     * The FileConfiguration instance used to load and store locale data.
     */
    private final FileConfiguration configuration;

    /**
     * The File instance representing the locale file.
     */
    private final File file;

    /**
     * The Locale instance representing the current locale.
     */
    private Locale locale;

    /**
     * Constructs a new instance of LocaleUtils using a specific locale.
     * If the provided locale does not exist, a LocaleNotFoundException is thrown.
     * If the provided locale is FALLBACK, the fallback locale file is loaded.
     *
     * @param locale The specific locale to use.
     * @throws LocaleNotFoundException If the provided locale does not exist.
     */
    public LocaleUtils(Locale locale) throws LocaleNotFoundException {
        if (locale != Locale.FALLBACK) {
            String filePath = Main.getFilePath() + "locales/" + locale.getLocaleString() + ".yml";
            File file = new File(filePath);
            if (file.exists()) {
                this.configuration = new FileConfiguration(file);
                this.configuration.load();
                this.file = file;
                this.locale = locale;
            } else {
                String errorText = ErrorMessages.getErrorLocaleNotFound(locale.getLocaleString(), ErrorCode.ERROR_LOCALE_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_LOCALE_NOT_FOUND, errorText));
                Main.loggerUtils.addLog(errorText);
                throw new LocaleNotFoundException(errorText);
            }
        } else {
            this.configuration = new FileConfiguration(new SimpleJavaUtils().getFromResourceFile("locales/fallback.yml"));
            this.configuration.load();
            this.file = new SimpleJavaUtils().getFromResourceFile("locales/fallback.yml");
            this.locale = locale;
        }
    }

    /**
     * Constructs a new instance of LocaleUtils using the fallback locale file.
     * The fallback locale file is "locales/fallback.yml" and is loaded if no specific locale is provided.
     *
     * @throws NoClassDefFoundError If the SimpleJavaUtils class is not found in the classpath.
     */
    public LocaleUtils() {
        // Initialize the FileConfiguration with the fallback locale file
        this.configuration = new FileConfiguration(new SimpleJavaUtils().getFromResourceFile("locales/fallback.yml"));
        // Load the configuration file
        this.configuration.load();
        // Set the file reference to the fallback locale file
        file = new SimpleJavaUtils().getFromResourceFile("locales/fallback.yml");
        Main.getLogger().error("No specific locale provided, using fallback locale.");
    }

    /**
     * Constructs a new instance of LocaleUtils using a custom locale file path.
     *
     * @param customFilePath The path to the custom locale file.
     * @throws LocaleNotFoundException If the custom locale file does not exist.
     */
    public LocaleUtils(String customFilePath) throws LocaleNotFoundException {
        File file = new File(customFilePath);
        if (file.exists()) {
            this.configuration = new FileConfiguration(file);
            this.configuration.load();
            this.file = file;
        } else {
            String errorText = ErrorMessages.getErrorLocaleDoesNotExists(customFilePath, ErrorCode.ERROR_LOAD.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_LOAD, errorText));
            Main.loggerUtils.addLog(errorText);
            throw new LocaleNotFoundException(errorText);
        }
    }

    public FileConfiguration getErrorConfiguration() {
        FileConfiguration configuration;
        if (locale == Locale.FALLBACK)
            configuration = new FileConfiguration(new SimpleJavaUtils().getFromResourceFile("locales/errorMessages_" + Locale.EN_EN.getLocaleString() + ".yml"));
        else
            configuration = new FileConfiguration(new SimpleJavaUtils().getFromResourceFile("locales/errorMessages_" + locale.getLocaleString() + ".yml"));
        // Load the configuration file
        configuration.load();
        return configuration;
    }

    public FileConfiguration getFallbackConfiguration() {
        return new FileConfiguration(new SimpleJavaUtils().getFromResourceFile("locales/fallback.yml"));
    }

    /**
     * Retrieves the File instance representing the locale file.
     *
     * @return The File instance representing the locale file.
     * This file is used to load and store locale data.
     */
    public File getFile() {
        return file;
    }

    /**
     * This method retrieves a string from the locale configuration file.
     *
     * @param key The key under which the string is stored in the configuration file.
     * @return The string value associated with the given key.
     * @throws NullPointerException If the retrieved string is null.
     */
    public String getString(String key) {
        if(!configuration.containsKey(key))
            return getFallbackConfiguration().getString(key);
        return (String) configuration.get(key);
    }

    /**
     * This method retrieves a string from the locale configuration file and replaces a pattern with a given value.
     *
     * @param key     The key under which the string is stored in the configuration file.
     * @param pattern The pattern to be replaced in the retrieved string.
     * @param value   The value to replace the pattern with.
     * @return The retrieved string with the pattern replaced by the given value.
     * @throws NullPointerException If the retrieved string is null.
     */
    public String getString(String key, String pattern, Object value) {
        return getString(key).replace(pattern, value.toString());
    }

    /**
     * This method retrieves a list of strings from the locale configuration file.
     *
     * @param key The key under which the list is stored in the configuration file.
     * @return A list of strings corresponding to the given key.
     * @throws ClassCastException If the value associated with the given key is not a list of strings.
     */
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        if(!configuration.containsKey(key))
            //noinspection unchecked
            return (List<String>) getFallbackConfiguration().get(key);
        //noinspection unchecked
        return (List<String>) configuration.get(key);
    }

    public List<String> getStringListReplace(String key, String[] pattern, Object[] value) {
        List<String> list = getStringList(key);
        List<String> newList = new ArrayList<>();
        for (String listValue : list) {
            String modified = listValue;
            for (int i = 0; i < pattern.length; i++) {
                modified = modified.replace(pattern[i], value[i].toString());
            }
            newList.add(modified);
        }
        return newList;
    }

    /**
     * This Method returns the used Locale field like EN_EN
     *
     * @return return the locale field
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Retrieves the FileConfiguration instance used to load and store locale data.
     *
     * @return The FileConfiguration instance representing the locale data.
     * This instance is used to interact with the locale file, allowing for reading and writing of locale-specific data.
     */
    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
