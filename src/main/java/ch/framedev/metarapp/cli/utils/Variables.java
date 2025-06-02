package ch.framedev.metarapp.cli.utils;

import static ch.framedev.metarapp.cli.Main.*;

public class Variables {

    public static String DIRECTORY_FOLDER = (String) settings.get("directory-folder");
    public static String DOWNLOAD_FOLDER = (String) settings.get("download-folder");
    public static String OWN_MYSQL_HOST = (String) settings.get("mysql.mysql-host");
    public static String OWN_MYSQL_USER = (String) settings.get("mysql.mysql-user");
    public static String OWN_MYSQL_PASSWORD = (String) settings.get("mysql.mysql-password");
    public static String OWN_MYSQL_DATABASE = (String) settings.get("mysql.mysql-database");
    public static int OWN_MYSQL_PORT = (int) settings.get("mysql.mysql-port");
    public static boolean OWN_DATABASE = (boolean) settings.get("mysql.use-own-database");

}
