package ch.framedev.metarapp.util;

import ch.framedev.metarapp.main.Main;

@SuppressWarnings("unused")
public class ErrorMessages {

    public static String getErrorLocaleDoesNotExists(String filePath, String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorLocaleDoesNotExists");
        errorText = errorText.replace("%FILE%", filePath);
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorLocaleNotFound(String locale, String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorLocaleNotFound");
        errorText = errorText.replace("%LOCALE%", locale);
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorFailedToReadFile(String file, String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorFailedToReadFile");
        errorText = errorText.replace("%FILE%", file);
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorLoadingChangelogsFile(String file, String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorLoadingChangelogsFile");
        errorText = errorText.replace("%FILE%", file);
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorJsonParse(String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorJsonParse");
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorNewVersionOpening() {
        return (String) Main.localeUtils.getErrorConfiguration().get("errorNewVersionOpening");
    }

    public static String getErrorShowNewVersion() {
        return (String) Main.localeUtils.getErrorConfiguration().get("errorShowNewVersion");
    }

    public static String getErrorUpdate(String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorFailedToUpdate");
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorCreateFileOrDirectory(String errorCode) {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorFailedToCreateFileOrDirectory");
        errorText = errorText.replace("%ERRORCODE%", errorCode);
        return errorText;
    }

    public static String getErrorZipFile() {
        String errorText = (String) Main.localeUtils.getErrorConfiguration().get("errorFailedToZp");
        errorText = errorText.replace("%ERRORCODE%", ErrorCode.ERROR_ZIPPING_FILE.getError());
        return errorText;
    }
}
