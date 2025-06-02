package ch.framedev.metarapp.util;

/**
 * This enum represents the indices of different types of information in a data record.
 * Each enum constant represents a specific type and its corresponding index in the record.
 */
public enum TypeIndex {

    /**
     * Represents the index of the country code in the data record.
     */
    COUNTRY_CODE(0),

    /**
     * Represents the index of the region name in the data record.
     */
    REGION_NAME(1),

    /**
     * Represents the index of the IATA code in the data record.
     */
    IATA(2),

    /**
     * Represents the index of the ICAO code in the data record.
     */
    ICAO(3),

    /**
     * Represents the index of the airport name in the data record.
     */
    AIRPORT(4),

    /**
     * Represents the index of the latitude in the data record.
     */
    LATITUDE(5),

    /**
     * Represents the index of the longitude in the data record.
     */
    LONGITUDE(6);

    private final int index;

    /**
     * Constructs a new TypeIndex with the given index.
     *
     * @param index the index of the type in the data record
     */
    TypeIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the index of the type in the data record.
     *
     * @return the index of the type
     */
    public int getIndex() {
        return index;
    }
}