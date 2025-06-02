package ch.framedev.metarapp.util;

import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;

public enum Locale {

    EN_EN(1, "en-En"),
    DE_DE(2, "de-De"),
    FR_FR(3, "fr-Fr"),
    RU_RU(4, "ru-Ru"),
    IT_IT(5, "it-It"),
    FALLBACK(-1, "fallback");

    final int id;
    final String localeString;

    Locale(int id, String localeString) {
        this.id = id;
        this.localeString = localeString;
    }

    public int getId() {
        return id;
    }

    public String getLocaleString() {
        return localeString;
    }

    public static Locale fromLocaleString(String localeString) throws LocaleNotFoundException {
        for (Locale locale : Locale.values()) {
            if (locale.getLocaleString().equalsIgnoreCase(localeString)) {
                return locale;
            }
        }
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorLocaleNotFound");
        errorText = errorText.replace("%LOCALE%", localeString);
        errorText = errorText.replace("%ERRORCODE%", ErrorCode.ERROR_LOCALE_NOT_FOUND.getError());
        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_LOCALE_NOT_FOUND, errorText));
        Main.loggerUtils.addLog(errorText);
        throw new LocaleNotFoundException(errorText);
    }
}
