package ch.framedev.metarapp.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.framedev.metarapp.cli.data.AirportData;
import ch.framedev.metarapp.cli.data.MetarData;
import ch.framedev.metarapp.cli.requests.AirportRequest;
import ch.framedev.metarapp.cli.requests.MetarRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Singleton class providing access to the MetarCLIAPI instance.
 */
public class MetarCLIAPI {

    private static MetarCLIAPI instance;

    /**
     * Private constructor to prevent instantiation of the class.
     */
    private MetarCLIAPI() {
        instance = this;
    }

    /**
     * Returns the singleton instance of the MetarCLIAPI class.
     * If the instance is null, a new instance is created.
     *
     * @return the singleton instance of the MetarCLIAPI class
     */
    public static MetarCLIAPI getInstance() {
        if (instance == null)
            new MetarCLIAPI();
        return instance;
    }

    /**
     * Returns the full JSON representation of the METAR data for the specified ICAO code.
     * If the 'pretty' parameter is true, the JSON is formatted with indentation and line breaks.
     *
     * @param icao the ICAO code of the airport
     * @param pretty a boolean indicating whether to format the JSON with indentation and line breaks
     * @return the full JSON representation of the METAR data for the specified ICAO code
     * @throws RuntimeException if an IOException occurs during the request
     */
    public String getMetarFullJson(@NotNull String icao, boolean pretty) throws RuntimeException {
        try {
            if (pretty)
                return new GsonBuilder().setPrettyPrinting().create().toJson(new MetarRequest(icao).getRoot());
            else
                return new GsonBuilder().create().toJson(new MetarRequest(icao).getRoot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the METAR data for the specified ICAO code.
     *
     * @param icao the ICAO code of the airport
     * @return the METAR data for the specified ICAO code
     * @throws IOException if an error occurs during the request
     */
    public MetarData getMetarData(@NotNull String icao) throws IOException {
        MetarRequest metarAPI = new MetarRequest(icao);
        return new Gson().fromJson(metarAPI.getRoot(), MetarData.class);
    }

    /**
     * Returns the airport data for the specified ICAO code.
     *
     * @param icao the ICAO code of the airport
     * @return the airport data for the specified ICAO code
     * @throws IOException if an error occurs during the request
     */
    public AirportData getAirportData(@NotNull String icao) throws IOException {
        AirportRequest airportRequest = new AirportRequest(icao);
        return new Gson().fromJson(airportRequest.getRoot(), AirportData.class);
    }

    /**
     * Returns a new instance of the MetarRequest class for the specified ICAO code.
     *
     * @param icao the ICAO code of the airport
     * @return a new instance of the MetarRequest class for the specified ICAO code
     * @throws IOException if an error occurs during the request
     */
    public MetarRequest getMetarRequest(@NotNull String icao) throws IOException {
        return new MetarRequest(icao);
    }

    /**
     * Returns a new instance of the AirportRequest class for the specified ICAO code.
     *
     * @param icao the ICAO code of the airport
     * @return a new instance of the AirportRequest class for the specified ICAO code
     * @throws IOException if an error occurs during the request
     */
    public AirportRequest getAirportRequest(@NotNull String icao) throws IOException {
        return new AirportRequest(icao);
    }
}
