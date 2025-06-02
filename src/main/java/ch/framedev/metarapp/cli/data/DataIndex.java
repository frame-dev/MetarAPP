package ch.framedev.metarapp.cli.data;

/*
 * ch.framedev.metarappcli.data
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 04.05.2024 21:46
 */

public enum DataIndex {

    /**
     * Represents the ID data type.
     */
    ID(0),

    /**
     * Represents the USERNAME data type.
     */
    USERNAME(1),

    /**
     * Represents the PASSWORD data type.
     */
    PASSWORD(2),

    /**
     * Represents the USED data type.
     */
    USED(3),

    /**
     * Represents the MAP_OPENED data type.
     */
    MAP_OPENED(4),

    /**
     * Represents the FILES_DOWNLOADED data type.
     */
    FILES_DOWNLOADED(5),

    /**
     * Represents the ICAOS data type.
     */
    ICAOS(6),

    /**
     * Represents the LAST_USED data type.
     */
    LAST_USED(7);

    /**
     * The index of the enum constant.
     */
    final int index;

    /**
     * Constructor for the enum constants.
     *
     * @param index the index of the enum constant
     */
    DataIndex(int index) {
        this.index = index;
    }

    /**
     * Get the index of the enum constant.
     *
     * @return the index of the enum constant
     */
    public int getIndex() {
        return index;
    }
}