package ch.framedev.metarapp.cli.utils;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.simplejavautils.SimpleJavaUtils;
import ch.framedev.yamlutils.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Currently supported languages are (en-En/de-De)
 */
public class LocaleUtils {

    private final FileConfiguration configuration;
    private final File file;
    private Locale locale;
    private FileConfiguration cachedFallback;

    /**
     * Constructs a new instance of LocaleUtils for the specified locale.
     * If the locale is FALLBACK, it loads the default locale configuration from the
     * resource file.
     * If the locale file exists in the user's configuration directory, it loads the
     * configuration from there.
     * If the locale file does not exist, it throws a LocaleNotFoundException.
     *
     * @param locale The locale for which to load the configuration.
     * @throws LocaleNotFoundException If the specified locale file does not exist.
     */
    public LocaleUtils(Locale locale) throws LocaleNotFoundException {
        this.locale = locale;
        if (locale == Locale.FALLBACK) {
            this.configuration = new FileConfiguration(new SimpleJavaUtils()
                    .getFromResourceFile("locale/" + locale.getLocaleString() + ".yml", Main.class));
            this.configuration.load();
            file = new SimpleJavaUtils().getFromResourceFile("locale/" + locale.getLocaleString() + ".yml", Main.class);
        } else {
            if (new File(Main.getFilePath() + "locales/" + locale.getLocaleString() + ".yml").exists()) {
                this.configuration = new FileConfiguration(
                        new File(Main.getFilePath() + "locales/" + locale.getLocaleString() + ".yml"));
                this.configuration.load();
                file = new File(Main.getFilePath() + "locales/" + locale.getLocaleString() + ".yml");
            } else {
                throw new LocaleNotFoundException("The locale " + locale.getLocaleString() + " does not exist!");
            }
        }
    }

    /**
     * Constructs a new instance of LocaleUtils using a custom locale file path.
     * If the specified locale file exists, it loads the configuration from there.
     * If the locale file does not exist, it throws a LocaleNotFoundException.
     *
     * @param customFilePath The file path of the custom locale file.
     * @throws LocaleNotFoundException If the specified locale file does not exist.
     */
    public LocaleUtils(String customFilePath) throws LocaleNotFoundException {
        File file = new File(customFilePath);
        if (file.exists()) {
            this.configuration = new FileConfiguration(file);
            this.configuration.load();
            this.file = file;
        } else {
            throw new LocaleNotFoundException("The locale " + customFilePath + " does not exist!");
        }
    }

    /**
     * Retrieves the fallback locale configuration.
     * If the fallback configuration has not been loaded yet, it loads it from the
     * resource file.
     *
     * @return The loaded fallback locale configuration.
     */
    public FileConfiguration getFallback() {
        if (cachedFallback == null) {
            cachedFallback = new FileConfiguration(new SimpleJavaUtils()
                    .getFromResourceFile("locale/" + Locale.FALLBACK.getLocaleString() + ".yml", Main.class));
            cachedFallback.load();
        }
        return cachedFallback;
    }

    /**
     * Retrieves the file associated with the current locale configuration.
     *
     * @return The file object representing the locale configuration file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Retrieves a string value associated with the given key from the locale
     * configuration.
     * If the key is not found in the current locale configuration, it retrieves the
     * value from the fallback locale configuration.
     *
     * @param key The key to retrieve the string value for.
     * @return The string value associated with the given key. If the key is not
     *         found in both configurations, it returns null.
     */
    public String getString(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        if (configuration.containsKey(key)) {
            return (String) configuration.get(key);
        }
        return getFallback().getString(key);
    }

    /**
     * Retrieves a string value associated with the given key from the locale
     * configuration, replacing the specified pattern with the provided value.
     * If the key is not found in the current locale configuration, it retrieves the
     * value from the fallback locale configuration.
     *
     * @param key     The key to retrieve the string value for.
     * @param pattern The pattern to be replaced in the retrieved string value.
     * @param value   The value to replace the pattern with.
     * @return The modified string value associated with the given key. If the key
     *         is not
     *         found in both configurations, it returns null.
     */
    public String getString(String key, String pattern, Object value) {
        return getString(key).replace(pattern, value.toString());
    }

    /**
     * Retrieves a list of string values associated with the given key from the
     * locale
     * configuration. If the key is not found in the current locale configuration,
     * it retrieves
     * the list from the fallback locale configuration.
     *
     * @param key The key to retrieve the list of string values for.
     * @return The list of string values associated with the given key. If the key
     *         is not
     *         found in both configurations, it returns the list from the fallback
     *         configuration.
     *         If the list from the current configuration is smaller than the
     *         fallback list,
     *         it returns the fallback list.
     */
    public List<String> getStringList(String key) {
        List<String> fallbackList = getFallback().getStringList(key);
        List<String> configList = configuration.getStringList(key);

        // If configList is null, return the fallback list directly
        if (configList == null) {
            return fallbackList;
        }

        // Compare sizes of lists and return the one with larger size
        if (configList.size() < fallbackList.size()) {
            return fallbackList;
        }

        return configList;
    }

    /**
     * Retrieves a list of string values associated with the given key from the
     * locale
     * configuration, replacing the specified patterns with the provided values.
     * If the key is not found in the current locale configuration, it retrieves
     * the list from the fallback locale configuration.
     *
     * @param key     The key to retrieve the list of string values for.
     * @param pattern An array of patterns to be replaced in the retrieved string
     *                values.
     * @param value   An array of values to replace the patterns with.
     * @return The modified list of string values associated with the given key. If
     *         the key
     *         is not found in both configurations, it returns the list from the
     *         fallback
     *         configuration. If the list from the current configuration is smaller
     *         than the
     *         fallback list, it returns the fallback list.
     * @throws IllegalArgumentException If the lengths of the pattern and value
     *                                  arrays are not equal.
     */
    public List<String> getStringListReplace(String key, String[] pattern, Object[] value) {
        if (pattern.length != value.length) {
            throw new IllegalArgumentException("Pattern and value arrays must have the same length");
        }
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
     * Retrieves the current locale used by the LocaleUtils instance.
     *
     * @return The current locale.
     */
    public Locale getLocale() {
        return locale;
    }
}
