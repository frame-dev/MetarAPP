package ch.framedev.metarapp.cli.utils;

public class LocaleNotFoundException extends Exception {

    public LocaleNotFoundException(String message) {
        super(message);
    }

    public LocaleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocaleNotFoundException(Throwable cause) {
        super(cause);
    }

    public LocaleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public LocaleNotFoundException() {
    }
}
