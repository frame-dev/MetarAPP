package ch.framedev.metarapp.cli.data;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MetarData {
    private int results;
    private List<AirportWeather> data;

    // Getters and Setters

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<AirportWeather> getData() {
        return data;
    }

    public void setData(List<AirportWeather> data) {
        this.data = data;
    }

    public static class AirportWeather {
        private String icao;
        private Barometer barometer;
        private List<Cloud> clouds;
        private Dewpoint dewpoint;
        private Elevation elevation;
        private String flight_category;
        private Humidity humidity;
        private String observed;
        private Station station;
        private Temperature temperature;
        private String raw_text;
        private Visibility visibility;
        private Wind wind;

        // Getters and Setters

        public String getIcao() {
            return icao;
        }

        public void setIcao(String icao) {
            this.icao = icao;
        }

        public Barometer getBarometer() {
            return barometer;
        }

        public void setBarometer(Barometer barometer) {
            this.barometer = barometer;
        }

        public List<Cloud> getClouds() {
            return clouds;
        }

        public void setClouds(List<Cloud> clouds) {
            this.clouds = clouds;
        }

        public Dewpoint getDewpoint() {
            return dewpoint;
        }

        public void setDewpoint(Dewpoint dewpoint) {
            this.dewpoint = dewpoint;
        }

        public Elevation getElevation() {
            return elevation;
        }

        public void setElevation(Elevation elevation) {
            this.elevation = elevation;
        }

        public String getFlight_category() {
            return flight_category;
        }

        public void setFlight_category(String flight_category) {
            this.flight_category = flight_category;
        }

        public Humidity getHumidity() {
            return humidity;
        }

        public void setHumidity(Humidity humidity) {
            this.humidity = humidity;
        }

        public String getObserved() {
            return observed;
        }

        public void setObserved(String observed) {
            this.observed = observed;
        }

        public Station getStation() {
            return station;
        }

        public void setStation(Station station) {
            this.station = station;
        }

        public Temperature getTemperature() {
            return temperature;
        }

        public void setTemperature(Temperature temperature) {
            this.temperature = temperature;
        }

        public String getRaw_text() {
            return raw_text;
        }

        public void setRaw_text(String raw_text) {
            this.raw_text = raw_text;
        }

        public Visibility getVisibility() {
            return visibility;
        }

        public void setVisibility(Visibility visibility) {
            this.visibility = visibility;
        }

        public Wind getWind() {
            return wind;
        }

        public void setWind(Wind wind) {
            this.wind = wind;
        }

        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }
    }

    public static class Barometer {
        private double hg;
        private double hpa;
        private double kpa;
        private double mb;

        // Getters and Setters

        public double getHg() {
            return hg;
        }

        public void setHg(double hg) {
            this.hg = hg;
        }

        public double getHpa() {
            return hpa;
        }

        public void setHpa(double hpa) {
            this.hpa = hpa;
        }

        public double getKpa() {
            return kpa;
        }

        public void setKpa(double kpa) {
            this.kpa = kpa;
        }

        public double getMb() {
            return mb;
        }

        public void setMb(double mb) {
            this.mb = mb;
        }
    }

    public static class Cloud {
        private int base_feet_agl;
        private int base_meters_agl;
        private String code;
        private String text;
        private int feet;
        private int meters;

        // Getters and Setters

        public int getBase_feet_agl() {
            return base_feet_agl;
        }

        public void setBase_feet_agl(int base_feet_agl) {
            this.base_feet_agl = base_feet_agl;
        }

        public int getBase_meters_agl() {
            return base_meters_agl;
        }

        public void setBase_meters_agl(int base_meters_agl) {
            this.base_meters_agl = base_meters_agl;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getFeet() {
            return feet;
        }

        public void setFeet(int feet) {
            this.feet = feet;
        }

        public int getMeters() {
            return meters;
        }

        public void setMeters(int meters) {
            this.meters = meters;
        }
    }

    public static class Dewpoint {
        private int celsius;
        private int fahrenheit;

        // Getters and Setters

        public int getCelsius() {
            return celsius;
        }

        public void setCelsius(int celsius) {
            this.celsius = celsius;
        }

        public int getFahrenheit() {
            return fahrenheit;
        }

        public void setFahrenheit(int fahrenheit) {
            this.fahrenheit = fahrenheit;
        }
    }

    public static class Elevation {
        private double feet;
        private double meters;

        // Getters and Setters

        public double getFeet() {
            return feet;
        }

        public void setFeet(double feet) {
            this.feet = feet;
        }

        public double getMeters() {
            return meters;
        }

        public void setMeters(double meters) {
            this.meters = meters;
        }
    }

    public static class Humidity {
        private int percent;

        // Getters and Setters

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }
    }

    public static class Station {
        private Geometry geometry;
        private String location;
        private String name;
        private String type;

        // Getters and Setters

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Geometry {
        private List<Double> coordinates;
        private String type;

        // Getters and Setters

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Temperature {
        private int celsius;
        private int fahrenheit;

        // Getters and Setters

        public int getCelsius() {
            return celsius;
        }

        public void setCelsius(int celsius) {
            this.celsius = celsius;
        }

        public int getFahrenheit() {
            return fahrenheit;
        }

        public void setFahrenheit(int fahrenheit) {
            this.fahrenheit = fahrenheit;
        }
    }

    public static class Visibility {
        private String miles;
        private double miles_float;
        private String meters;
        private double meters_float;

        // Getters and Setters

        public String getMiles() {
            return miles;
        }

        public void setMiles(String miles) {
            this.miles = miles;
        }

        public double getMiles_float() {
            return miles_float;
        }

        public void setMiles_float(double miles_float) {
            this.miles_float = miles_float;
        }

        public String getMeters() {
            return meters;
        }

        public void setMeters(String meters) {
            this.meters = meters;
        }

        public double getMeters_float() {
            return meters_float;
        }

        public void setMeters_float(double meters_float) {
            this.meters_float = meters_float;
        }
    }

    public static class Wind {
        private int degrees;
        private int speed_kph;
        private int speed_kts;
        private int speed_mph;
        private int speed_mps;

        // Getters and Setters

        public int getDegrees() {
            return degrees;
        }

        public void setDegrees(int degrees) {
            this.degrees = degrees;
        }

        public int getSpeed_kph() {
            return speed_kph;
        }

        public void setSpeed_kph(int speed_kph) {
            this.speed_kph = speed_kph;
        }

        public int getSpeed_kts() {
            return speed_kts;
        }

        public void setSpeed_kts(int speed_kts) {
            this.speed_kts = speed_kts;
        }

        public int getSpeed_mph() {
            return speed_mph;
        }

        public void setSpeed_mph(int speed_mph) {
            this.speed_mph = speed_mph;
        }

        public int getSpeed_mps() {
            return speed_mps;
        }

        public void setSpeed_mps(int speed_mps) {
            this.speed_mps = speed_mps;
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }

    public void downloadData(File target) throws RuntimeException {
        if(target.getParentFile() != null) {
            target.getParentFile().mkdirs();
        }
        if(!target.exists()) {
            try {
                target.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try(FileWriter writer = new FileWriter(target)) {
            writer.write(toString());
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
