package ch.framedev.metarapp.data;

/*
 * ch.framedev.metarapp.data
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 04.05.2024 21:46
 */

public enum DataEnum {

    /**
     * Represents the ID data type.
     */
    ID(0, "ID"),

    /**
     * Represents the USERNAME data type.
     */
    USERNAME(1, "UserName"),

    /**
     * Represents the PASSWORD data type.
     */
    PASSWORD(2, "Password"),

    /**
     * Represents the USED data type.
     */
    USED(3, "Used"),

    /**
     * Represents the MAP_OPENED data type.
     */
    MAP_OPENED(4, "MapOpened"),

    /**
     * Represents the FILES_DOWNLOADED data type.
     */
    FILES_DOWNLOADED(5, "FilesDownloaded"),

    /**
     * Represents the ICAOS data type.
     */
    ICAOS(6, "ICAOS"),

    /**
     * Represents the LAST_USED data type.
     */
    LAST_USED(7, "LastUsed");

    /**
     * The index of the enum constant.
     */
    final int index;
    final String columnName;

    /**
     * Constructor for the enum constants.
     *
     * @param index the index of the enum constant
     */
    DataEnum(int index, String columnName) {
        this.index = index;
        this.columnName = columnName;
    }

    /**
     * Get the index of the enum constant.
     *
     * @return the index of the enum constant
     */
    public int getIndex() {
        return index;
    }

    public String getColumnName() {
        return columnName;
    }
}
