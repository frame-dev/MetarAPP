package ch.framedev.metarapp.data;

import com.google.gson.GsonBuilder;

import java.util.List;

@SuppressWarnings("unused")
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

    public void setICAO(String ICAO) {
        this.ICAO = ICAO;
    }

    public String getIATA() {
        return IATA;
    }

    public void setIATA(String IATA) {
        this.IATA = IATA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getMagneticVariation() {
        return magneticVariation;
    }

    public void setMagneticVariation(double magneticVariation) {
        this.magneticVariation = magneticVariation;
    }

    public Timezone getTimezone() {
        return timezone;
    }

    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    public Times getTimes() {
        return times;
    }

    public void setTimes(Times times) {
        this.times = times;
    }

    public int getRunwayCount() {
        return runwayCount;
    }

    public void setRunwayCount(int runwayCount) {
        this.runwayCount = runwayCount;
    }

    public List<Runway> getRunways() {
        return runways;
    }

    public void setRunways(List<Runway> runways) {
        this.runways = runways;
    }

    public List<Frequency> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(List<Frequency> frequencies) {
        this.frequencies = frequencies;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    // Getters and setters for the above fields
    public static class Timezone {
        private String name;
        private int offset;

        // Getters and setters for the above fields

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
    }

    public static class Times {
        private String sunrise;
        private String sunset;
        private String dawn;
        private String dusk;

        // Getters and setters for the above fields

        public String getSunrise() {
            return sunrise;
        }

        public void setSunrise(String sunrise) {
            this.sunrise = sunrise;
        }

        public String getSunset() {
            return sunset;
        }

        public void setSunset(String sunset) {
            this.sunset = sunset;
        }

        public String getDawn() {
            return dawn;
        }

        public void setDawn(String dawn) {
            this.dawn = dawn;
        }

        public String getDusk() {
            return dusk;
        }

        public void setDusk(String dusk) {
            this.dusk = dusk;
        }
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

        // Getters and setters for the above fields

        public String getIdent() {
            return ident;
        }

        public void setIdent(String ident) {
            this.ident = ident;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getBearing() {
            return bearing;
        }

        public void setBearing(double bearing) {
            this.bearing = bearing;
        }

        public String getSurface() {
            return surface;
        }

        public void setSurface(String surface) {
            this.surface = surface;
        }

        public List<String> getMarkings() {
            return markings;
        }

        public void setMarkings(List<String> markings) {
            this.markings = markings;
        }

        public List<String> getLighting() {
            return lighting;
        }

        public void setLighting(List<String> lighting) {
            this.lighting = lighting;
        }

        public double getThresholdOffset() {
            return thresholdOffset;
        }

        public void setThresholdOffset(double thresholdOffset) {
            this.thresholdOffset = thresholdOffset;
        }

        public double getOverrunLength() {
            return overrunLength;
        }

        public void setOverrunLength(double overrunLength) {
            this.overrunLength = overrunLength;
        }

        public List<End> getEnds() {
            return ends;
        }

        public void setEnds(List<End> ends) {
            this.ends = ends;
        }

        public List<Navaid> getNavaids() {
            return navaids;
        }

        public void setNavaids(List<Navaid> navaids) {
            this.navaids = navaids;
        }
    }

    public static class End {
        private String ident;
        private double lat;
        private double lon;

        // Getters and setters for the above fields

        public String getIdent() {
            return ident;
        }

        public void setIdent(String ident) {
            this.ident = ident;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
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
        // Other fields

        // Getters and setters for the above fields

        public String getIdent() {
            return ident;
        }

        public void setIdent(String ident) {
            this.ident = ident;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public String getAirport() {
            return airport;
        }

        public void setAirport(String airport) {
            this.airport = airport;
        }

        public String getRunway() {
            return runway;
        }

        public void setRunway(String runway) {
            this.runway = runway;
        }

        public double getFrequency() {
            return frequency;
        }

        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }
    }

    public static class Frequency {
        private String type;
        private double frequency;
        private String name;

        // Getters and setters for the above fields

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getFrequency() {
            return frequency;
        }

        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Weather {
        private String METAR;
        private String TAF;

        // Getters and setters for the above fields

        public String getMETAR() {
            return METAR;
        }

        public void setMETAR(String METAR) {
            this.METAR = METAR;
        }

        public String getTAF() {
            return TAF;
        }

        public void setTAF(String TAF) {
            this.TAF = TAF;
        }
    }

    public boolean hasResults() {
        return ICAO != null && !ICAO.isEmpty() && runways != null && !runways.isEmpty() && weather != null;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}