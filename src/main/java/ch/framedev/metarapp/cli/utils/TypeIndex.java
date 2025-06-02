package ch.framedev.metarapp.cli.utils;

public enum TypeIndex {
    COUNTRY_CODE(0),
    REGION_NAME(1),
    IATA(2),
    ICAO(3),
    AIRPORT(4),
    LATITUDE(5),
    LONGITUDE(6);

    private final int index;

    TypeIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}