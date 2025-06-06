package ch.framedev.metarapp.cli.data;

import com.google.gson.GsonBuilder;

import java.util.List;

public class AirportData {
    private String ICAO;
    private String IATA;
    private String name;
    private String regionName;
    private double elevation;
    private double lat;
    private double lon;
    private double magneticVariation;
    private Timezone timezone;
    private Times times;
    private int runwayCount;
    private List<Runway> runways;
    private List<Frequency> frequencies;
    private Weather weather;

    public String getICAO() {
        return ICAO;
    }

    public String getIATA() {
        return IATA;
    }

    public String getName() {
        return name;
    }

    public String getRegionName() {
        return regionName;
    }

    public double getElevation() {
        return elevation;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getMagneticVariation() {
        return magneticVariation;
    }

    public Timezone getTimezone() {
        return timezone;
    }

    public Times getTimes() {
        return times;
    }

    public int getRunwayCount() {
        return runwayCount;
    }

    public List<Runway> getRunways() {
        return runways;
    }

    public List<Frequency> getFrequencies() {
        return frequencies;
    }

    public Weather getWeather() {
        return weather;
    }

    // Getters and setters for the above fields

    @SuppressWarnings("unused")
    static class Timezone {
        private String name;
        private int offset;
        // Getters and setters
    }
    @SuppressWarnings("unused")
    static class Times {
        private String sunrise;
        private String sunset;
        private String dawn;
        private String dusk;
        // Getters and setters
    }

    public static class Runway {
        private String ident;
        private double width;
        private double length;
        private double bearing;
        private String surface;
        private List<String> markings;
        private List<String> lighting;
        private double thresholdOffset;
        private double overrunLength;
        private List<End> ends;
        private List<Navaid> navaids;
        // Getters and setters

        public String getIdent() {
            return ident;
        }

        public double getWidth() {
            return width;
        }

        public double getLength() {
            return length;
        }

        public double getBearing() {
            return bearing;
        }

        public String getSurface() {
            return surface;
        }

        public List<String> getMarkings() {
            return markings;
        }

        public List<String> getLighting() {
            return lighting;
        }

        public double getThresholdOffset() {
            return thresholdOffset;
        }

        public double getOverrunLength() {
            return overrunLength;
        }

        public List<End> getEnds() {
            return ends;
        }

        public List<Navaid> getNavaids() {
            return navaids;
        }
    }

    public static class End {
        private String ident;
        private double lat;
        private double lon;
        // Getters and setters

        public String getIdent() {
            return ident;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }
    }

    public static class Navaid {
        private String ident;
        private String type;
        private double lat;
        private double lon;
        private String airport;
        private String runway;
        private double frequency;
        private String name;
        private double elevation;
        private double range;
        private double slope;  // Only for GS type
        private double bearing;  // Only for LOC-ILS type
        // Getters and setters

        public String getIdent() {
            return ident;
        }

        public String getType() {
            return type;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public String getAirport() {
            return airport;
        }

        public String getRunway() {
            return runway;
        }

        public double getFrequency() {
            return frequency;
        }

        public String getName() {
            return name;
        }

        public double getElevation() {
            return elevation;
        }

        public double getRange() {
            return range;
        }

        public double getSlope() {
            return slope;
        }

        public double getBearing() {
            return bearing;
        }
    }

    public static class Frequency {
        private String type;
        private double frequency;
        private String name;
        // Getters and setters

        public String getType() {
            return type;
        }

        public double getFrequency() {
            return frequency;
        }

        public String getName() {
            return name;
        }
    }

    public static class Weather {
        private String METAR;
        private String TAF;
        // Getters and setters

        public String getMETAR() {
            return METAR;
        }

        public String getTAF() {
            return TAF;
        }
    }

    public boolean hasResults() {
        return ICAO != null && !ICAO.isEmpty() && runways != null && !runways.isEmpty() && weather != null;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }
}