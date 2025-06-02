package ch.framedev.metarapp.cli.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing different locales.
 *
 * @author framedev
 * @version 1.0
 */
public enum Locale {

    /**
     * English locale.
     */
    EN_EN("en-En", "en", "English"),

    /**
     * German locale.
     */
    DE_DE("de-De", "de", "German"),

    /**
     * French locale.
     */
    FR_FR("fr-Fr", "fr", "French"),

    /**
     * Russian locale.
     */
    RU_RU("ru-Ru", "ru", "Russian"),

    /**
     * Spanish locale.
     */
    ES_ES("es-Es", "es", "Spanish"),

    /**
     * Fallback locale.
     */
    FALLBACK("fallback", "fallback", "Fallback");

    /**
     * The string representation of the locale.
     */
    final String localeString, locale, description;

    private static final Map<String, Locale> LOCALE_MAP = new HashMap<>();

    static {
        for (Locale locale : Locale.values()) {
            LOCALE_MAP.put(locale.getLocaleString().toLowerCase(), locale);
            LOCALE_MAP.put(locale.getLocale().toLowerCase(), locale);
        }
    }

    /**
     * Constructor for the Locale enum.
     *
     * @param localeString the string representation of the locale
     */
    Locale(String localeString, String locale, String description) {
        this.localeString = localeString;
        this.locale = locale;
        this.description = description;
    }

    /**
     * Get the string representation of the locale.
     *
     * @return the string representation of the locale
     */
    public String getLocaleString() {
        return localeString;
    }

    public String getLocale() {
        return locale;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the Locale object from its string representation.
     *
     * @param localeString the string representation of the locale
     * @return the Locale object
     * @throws LocaleNotFoundException if the locale is not found
     */
    public static Locale fromLocaleString(String localeString) throws LocaleNotFoundException {
        Locale locale = LOCALE_MAP.get(localeString.toLowerCase());
        if (locale != null) {
            return locale;
        }
        throw new LocaleNotFoundException("Locale not found: " + localeString);
    }

    public static Locale fromLocale(String locale) throws LocaleNotFoundException {
        Locale localeObj = LOCALE_MAP.get(locale.toLowerCase());
        if (localeObj != null) {
            return localeObj;
        }
        throw new LocaleNotFoundException("Locale not found: " + locale);
    }
}
