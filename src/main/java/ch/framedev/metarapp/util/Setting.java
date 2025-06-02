package ch.framedev.metarapp.util;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 24.08.2024 14:21
 */

public enum Setting {

    // General
    DARK_MODE("dark-mode"),
    AUTO_UPDATE("auto-update"),
    FIRST_TIME_RUN("first-time-run"),
    SHOW_CHANGELOGS("show-changelogs"),
    REMEMBER("remember"),
    OFFLINE_MODE("offline-mode"),
    VERSION("version"),
    DOWNLOAD_FOLDER("download-folder"),
    AUTO_LOGIN("auto-login"),
    POPUP_NEW_VERSION("popup-new-version"),
    AUTO_RESTART_AFTER_UPDATE("auto-restart-after-update"),
    LANGUAGE("language"),

    // Branch (release, pre-release)
    BRANCH("branch"),

    // Database
    DATABASE("database"),
    OWN_MYSQL_DATABASE("mysql.own-mysql-database"),
    MYSQL_HOST("mysql.mysql-host"),
    MYSQL_PORT("mysql.mysql-port"),
    MYSQL_USERNAME("mysql.mysql-username"),
    MYSQL_PASSWORD("mysql.mysql-password"),
    MYSQL_DATABASE("mysql.mysql-database"),
    MONGODB_USER("mongodb.user"),
    MONGODB_PASSWORD("mongodb.password"),
    MONGODB_HOST("mongodb.host"),
    MONGODB_PORT("mongodb.port"),
    MONGODB_DATABASE("mongodb.database"),
    SQLITE_PATH("sqlite.path"),
    SQLITE_DATABASE("sqlite.database"),

    // Search
    USE_NEW_SEARCH("defaultSearch");

    final String key;

    Setting(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
