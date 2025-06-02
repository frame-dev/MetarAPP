package ch.framedev.metarapp.events;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventBus {
    private static final Set<Consumer<CallRefreshEvent>> refreshListeners = new HashSet<>();
    private static final Set<Consumer<SendIcaoEvent>> sendIcaoListeners = new HashSet<>();
    private static final Set<Consumer<DownloadedFileEvent>> downloadedFileListeners = new HashSet<>();
    private static final Set<Consumer<DisplayMetarEvent>> displayMetarListeners = new HashSet<>();
    private static final Set<Consumer<LoginEvent>> loginListeners = new HashSet<>();
    private static final Set<Consumer<DatabaseSendEvent>> databaseSendListeners = new HashSet<>();
    private static final Set<Consumer<DatabaseErrorEvent>> databaseErrorListeners = new HashSet<>();
    private static final Set<Consumer<DatabaseChangeEvent>> databaseChangeListeners = new HashSet<>();
    private static final Set<Consumer<LogoutEvent>> logoutListeners = new HashSet<>();
    private static final Set<Consumer<ErrorEvent>> errorListeners = new HashSet<>();
    private static final Set<Consumer<RequestStatusCodeEvent>> requestStatusCodeListeners = new HashSet<>();

    public static void registerRefreshListener(Consumer<CallRefreshEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!refreshListeners.contains(listener)) {
            refreshListeners.add(listener);
        }
    }

    public static void dispatchRefreshEvent(CallRefreshEvent event) {
        for (Consumer<CallRefreshEvent> listener : refreshListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerSendIcaoListeners(Consumer<SendIcaoEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!sendIcaoListeners.contains(listener)) {
            sendIcaoListeners.add(listener);
        }
    }

    public static String dispatchSendIcaoEvent(SendIcaoEvent event) {
        for (Consumer<SendIcaoEvent> listener : sendIcaoListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
        return event.getIcao();
    }

    public static void registerDownloadedFileListener(Consumer<DownloadedFileEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!downloadedFileListeners.contains(listener)) {
            downloadedFileListeners.add(listener);
        }
    }

    public static void dispatchDownloadedFileEvent(DownloadedFileEvent event) {
        for (Consumer<DownloadedFileEvent> listener : downloadedFileListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerDisplayMetarListener(Consumer<DisplayMetarEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!displayMetarListeners.contains(listener)) {
            displayMetarListeners.add(listener);
        }
    }

    public static void dispatchDisplayMetarEvent(DisplayMetarEvent event) {
        for (Consumer<DisplayMetarEvent> listener : displayMetarListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerLoginListener(Consumer<LoginEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!loginListeners.contains(listener)) {
            loginListeners.add(listener);
        }
    }

    public static void dispatchLoginEvent(LoginEvent event) {
        for (Consumer<LoginEvent> listener : loginListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerDatabaseSendListener(Consumer<DatabaseSendEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!databaseSendListeners.contains(listener)) {
            databaseSendListeners.add(listener);
        }
    }

    public static void dispatchDatabaseSendEvent(DatabaseSendEvent event) {
        for (Consumer<DatabaseSendEvent> listener : databaseSendListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerDatabaseErrorListener(Consumer<DatabaseErrorEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!databaseErrorListeners.contains(listener)) {
            databaseErrorListeners.add(listener);
        }
    }

    public static void dispatchDatabaseErrorEvent(DatabaseErrorEvent event) {
        for (Consumer<DatabaseErrorEvent> listener : databaseErrorListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void registerDatabaseChangeListener(Consumer<DatabaseChangeEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!databaseChangeListeners.contains(listener)) {
            databaseChangeListeners.add(listener);
        }
    }

    public static String dispatchDatabaseChangeEvent(DatabaseChangeEvent event) {
        for (Consumer<DatabaseChangeEvent> listener : databaseChangeListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
        return event.getDatabaseType();
    }

    public static void unregisterDatabaseChangeListener(Consumer<DatabaseChangeEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        databaseChangeListeners.remove(listener);
    }

    public static void registerLogoutListener(Consumer<LogoutEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!logoutListeners.contains(listener)) {
            logoutListeners.add(listener);
        }
    }

    public static String dispatchLogoutEvent(LogoutEvent event) {
        for (Consumer<LogoutEvent> listener : logoutListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
        return event.getMessage();
    }

    public static void unregisterLogoutListener(Consumer<LogoutEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        logoutListeners.remove(listener);
    }

    public static void registerErrorListener(Consumer<ErrorEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!errorListeners.contains(listener)) {
            errorListeners.add(listener);
        }
    }

    public static void dispatchErrorEvent(ErrorEvent event) {
        for (Consumer<ErrorEvent> listener : errorListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void unregisterErrorListener(Consumer<ErrorEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        requestStatusCodeListeners.remove(listener);
    }

    public static void RequestStatusCodeListener(Consumer<RequestStatusCodeEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!requestStatusCodeListeners.contains(listener)) {
            requestStatusCodeListeners.add(listener);
        }
    }

    public static void dispatchRequestStatusCodeEvent(RequestStatusCodeEvent event) {
        for (Consumer<RequestStatusCodeEvent> listener : requestStatusCodeListeners) {
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            listener.accept(event);
        }
    }

    public static void unregisterRequestStatusCodeListener(Consumer<RequestStatusCodeEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        requestStatusCodeListeners.remove(listener);
    }

    public static void unregisterRefreshListener(Consumer<CallRefreshEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        refreshListeners.remove(listener);
    }

    public static void unregisterSendIcaoListener(Consumer<SendIcaoEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        sendIcaoListeners.remove(listener);
    }

    public static void unregisterDownloadedFileListener(Consumer<DownloadedFileEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        downloadedFileListeners.remove(listener);
    }

    public static void unregisterDisplayMetarListener(Consumer<DisplayMetarEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        displayMetarListeners.remove(listener);
    }

    public static void unregisterLoginListener(Consumer<LoginEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        loginListeners.remove(listener);
    }

    public static void unregisterDatabaseSendListener(Consumer<DatabaseSendEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        databaseSendListeners.remove(listener);
    }

    public static void unregisterDatabaseErrorListener(Consumer<DatabaseErrorEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        databaseErrorListeners.remove(listener);
    }

    public static void clearAllListeners() {
        refreshListeners.clear();
        sendIcaoListeners.clear();
        downloadedFileListeners.clear();
        displayMetarListeners.clear();
        loginListeners.clear();
        databaseSendListeners.clear();
        databaseErrorListeners.clear();
    }
}