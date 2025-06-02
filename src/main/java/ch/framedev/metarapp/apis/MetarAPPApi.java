package ch.framedev.metarapp.apis;

import ch.framedev.javajsonutils.JsonUtils;
import ch.framedev.metarapp.handlers.ChangelogsReader;
import ch.framedev.metarapp.data.AirportData;
import ch.framedev.metarapp.data.MetarData;
import ch.framedev.metarapp.guis.FullJsonGUI;
import ch.framedev.metarapp.guis.MetarGUI;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.requests.AirportRequest;
import ch.framedev.metarapp.requests.MetarRequest;
import ch.framedev.metarapp.util.UpdateHandler;
import ch.framedev.simplejavautils.SystemUtils;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MetarAPPApi is a class that provides methods for interacting with the
 * MetarAPP application.
 * It includes functionalities for retrieving METAR data, airport information,
 * and application download links.
 *
 * @author framedev
 */
public class MetarAPPApi {

    private static final String VERSION = Main.VERSION;

    private static MetarAPPApi instance;

    /**
     * Protected constructor for MetarAPPApi.
     * Initializes the instance variable.
     */
    protected MetarAPPApi() {
        instance = this;
    }

    /**
     * Returns the singleton instance of MetarAPPApi.
     * Initializes the token handler and returns the instance.
     *
     * @return The singleton instance of MetarAPPApi.
     */
    public static MetarAPPApi getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new MetarAPPApi();
        }
        // Initialize the token handler with the tokens.properties file
        // This file should contain the necessary tokens for API access
        return instance;
    }

    /**
     * Checks if a given URL is online by sending a HEAD request.
     *
     * @param url The URL to check.
     * @return True if the URL is online, false otherwise.
     * @throws IOException If an error occurs while checking the URL.
     */
    private boolean isOnline(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        return connection.getResponseCode() == 200;
    }

    /**
     * Checks if the MetarAPP API is online by sending a request to a specific ICAO
     * code.
     *
     * @return True if the API is online and responding successfully, false
     *         otherwise.
     *         If an IOException occurs during the request, the method returns
     *         false.
     */
    public boolean isMetarAPIOnline() {
        try {
            if (getMetarRequest("LSZH").getResponse() == null) {
                return false;
            }
            return getMetarRequest("LSZH").getResponse().isSuccessful();
        } catch (IOException ex) {
            Main.loggerUtils.addLog("Error While checking if api is online : " + ex.getMessage());
            Main.getLogger().error("Error while checking if api is online : " + ex.getMessage(), ex);
            return false;
        }
    }

    public boolean isAirportAPIOnline() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://api.flightplandatabase.com").build();
        try (Response response = client.newCall(request).execute()) {
            Map<?, ?> map = new JsonUtils().classFromJsonString(response.body().string(), Map.class);
            return map.containsKey("message") && map.get("message") != null
                    && ((String) map.get("message")).equalsIgnoreCase("OK");
        } catch (Exception ex) {
            Main.loggerUtils.addLog("Could not get FlightPlan Database " + ex.getMessage());
            Main.getLogger().error("Could not get FlightPlan DataBase", ex);
            return false;
        }
        /*
         * try {
         * if (getAirportRequest("LSZH").getResponse() == null) {
         * return false;
         * }
         * return getAirportRequest("LSZH").getResponse().isSuccessful();
         * } catch (IOException ex) {
         * Main.loggerUtils.addLog("Error While checking if api is online : " +
         * ex.getMessage());
         * Main.getLogger().error("Error while checking if api is online : " +
         * ex.getMessage(), ex);
         * return false;
         * }
         */
    }

    /**
     * Retrieves a list of all available versions of the MetarAPP application.
     *
     * @return A list of strings representing the available versions.
     */
    public List<String> getAllVersions() {
        try {
            return new ChangelogsReader().getVersions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllPreReleaseVersions() {
        try {
            return new ChangelogsReader().getPreReleaseVersions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the download link for the specified version of the MetarAPP
     * application based on the operating system.
     *
     * @param version The version of the MetarAPP application.
     * @return The download link for the specified version.
     */
    public String getDownloadString(String version) {
        if (new SystemUtils().getOSType() == SystemUtils.OSType.WINDOWS) {
            return "https://framedev.ch/files/metarapp/windows/MetarAPP-" + version.replace("\"", "") + ".exe";
        } else if (new SystemUtils().getOSType() == SystemUtils.OSType.MACOS
                || new SystemUtils().getOSType() == SystemUtils.OSType.LINUX
                || new SystemUtils().getOSType() == SystemUtils.OSType.OTHER) {
            return "https://framedev.ch/files/metarapp/unix/MetarAPP-" + version.replace("\"", "") + ".jar";
        }
        return "https://framedev.ch/files/metarapp/unix/MetarAPP-" + version.replace("\"", "") + ".jar";
    }

    /**
     * Returns an array of strings containing the download links for the specified
     * version of the MetarAPP application.
     * The first link is for the Windows version, and the second link is for
     * Unix-based systems (macOS, Linux, and others).
     *
     * @param version The version of the MetarAPP application for which the download
     *                links are required.
     * @return An array of strings containing the download links for the specified
     *         version of the MetarAPP application.
     * @throws IllegalArgumentException If the provided version is null or empty.
     * @throws IllegalStateException    If the version is not available for
     *                                  download.
     */
    public String[] getDownloadStringAllTypes(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        // Check if the version is available for download
        if (!isVersionAvailable(version)) {
            throw new IllegalStateException("Version " + version + " is not available for download");
        }

        // Return the download links for the specified version
        return new String[] {
                "https://framedev.ch/files/metarapp/windows/MetarAPP-" + version.replace("\"", "") + ".exe",
                "https://framedev.ch/files/metarapp/unix/MetarAPP-" + version.replace("\"", "") + ".jar"
        };
    }

    /**
     * Checks if a specified version of the MetarAPP application is available for
     * download.
     *
     * @param version The version of the MetarAPP application.
     * @return True if the version is available for download, false otherwise.
     */
    public boolean isVersionAvailable(String version) {
        try {
            return isOnline(getDownloadString(version));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the changelogs for all available versions of the MetarAPP
     * application.
     *
     * @return A list of lists of strings, where each inner list contains the
     *         changelogs for a specific version.
     */
    public List<List<String>> getAllChangelogs() {
        ChangelogsReader changelogsReader = new ChangelogsReader();
        List<List<String>> result = new ArrayList<>();
        try {
            for (String version : changelogsReader.getVersions()) {
                result.add(changelogsReader.getChangelogs(version).getBugFixes());
                result.add(changelogsReader.getChangelogs(version).getPerformanceFixes());
                result.add(changelogsReader.getChangelogs(version).getFeatures());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<List<String>> getAllPreReleaseChangelogs() {
        ChangelogsReader changelogsReader = new ChangelogsReader();
        List<List<String>> result = new ArrayList<>();
        try {
            for (String version : changelogsReader.getPreReleaseVersions()) {
                result.add(changelogsReader.getChangelogs(version).getBugFixes());
                result.add(changelogsReader.getChangelogs(version).getPerformanceFixes());
                result.add(changelogsReader.getChangelogs(version).getFeatures());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Returns a MetarRequest object for a specified ICAO code.
     *
     * @param icaoCode The ICAO code for which to retrieve the METAR data.
     * @return A MetarRequest object.
     * @throws IOException If an error occurs while creating the MetarRequest
     *                     object.
     */
    public MetarRequest getMetarRequest(String icaoCode) throws IOException {
        return new MetarRequest(icaoCode);
    }

    /**
     * Retrieves the METAR data for a specified ICAO code.
     *
     * @param icao The ICAO code for which to retrieve the METAR data.
     * @return The METAR data as a MetarData object.
     * @throws IOException If an error occurs while retrieving the METAR data.
     */
    public MetarData getMetarData(String icao) throws IOException {
        MetarRequest metarRequest = new MetarRequest(icao);
        return new Gson().fromJson(metarRequest.getRoot(), MetarData.class);
    }

    /**
     * Retrieves the airport data for a specified ICAO code.
     *
     * @param icao The ICAO code for which to retrieve the airport data.
     * @return The airport data as an AirportData object.
     * @throws IOException If an error occurs while retrieving the airport data.
     */
    public AirportData getAirportData(String icao) throws IOException {
        AirportRequest airportRequest = new AirportRequest(icao);
        return new Gson().fromJson(airportRequest.getRoot(), AirportData.class);
    }

    /**
     * Returns the version of the MetarAPP application.
     *
     * @return The version of the MetarAPP application as a string.
     */
    public String getMetarAPPVersion() {
        return VERSION;
    }

    /**
     * Retrieves the download links for the latest version of the MetarAPP
     * application.
     * The returned array contains two strings: the first link is for the Windows
     * version,
     * and the second link is for Unix-based systems (macOS, Linux, and others).
     *
     * @return An array of strings containing the download links for the latest
     *         version of the MetarAPP application.
     */
    public String[] getLatestDownloadVersion() {
        return getDownloadStringAllTypes(Main.getNewVersion());
    }

    /**
     * Returns an AirportRequest object for a specified ICAO code.
     *
     * @param icao The ICAO code for which to retrieve the airport request.
     * @return An AirportRequest object.
     * @throws IOException If an error occurs while creating the AirportRequest
     *                     object.
     *
     * @param icao
     * @return
     * @throws IOException
     */
    public AirportRequest getAirportRequest(String icao) throws IOException {
        return new AirportRequest(icao);
    }

    /**
     * Retrieves the latest version of the MetarAPP application.
     *
     * @return The latest version as a string, or null if an error occurs.
     */
    public String getLatestVersion() {
        try {
            return UpdateHandler.getLatestVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the latest pre-release version of the MetarAPP application.
     *
     * @return The latest pre-release version as a string.
     */
    public String getLatestPreReleaseVersion() {
        return UpdateHandler.getLatestPreRelease();
    }

    /**
     * Retrieves the download link for the latest pre-release version of the
     * MetarAPP application.
     *
     * @return The download link for the latest pre-release version as a string.
     */
    public String getLatestPreReleaseDownloadLink() {
        return getDownloadString(getLatestPreReleaseVersion());
    }

    /**
     * Retrieves the download link for the latest version of the MetarAPP
     * application.
     *
     * @return The download link for the latest version as a string.
     */
    public String getLatestDownloadLink() {
        return getDownloadString(getLatestVersion());
    }

    /**
     * Retrieves the last search list of ICAO codes from the user data.
     *
     * @return A list of ICAO codes representing the last search list.
     */
    public List<String> getLastSearchList() {
        List<String> icaos = MetarGUI.userData != null ? MetarGUI.userData.getIcaos() : Collections.emptyList();

        if (icaos != null)
            return new ArrayList<>(icaos);
        else
            return new ArrayList<>();
    }

    /**
     * Retrieves a list of favourite ICAO codes from the user's data.
     *
     * @return A list of strings representing the favourite ICAO codes.
     */
    public List<String> getFavouriteIcaos() {
        List<String> icaos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(new File(Main.getFilePath() + "files", "favourite_icao.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                icaos.add(line);
            }
        } catch (IOException e) {
            Main.getLogger().error("Error reading favourite ICAO file", e);
        }
        return icaos;
    }

    /**
     * Sends an ICAO code to the MetarAPP application for processing.
     * This method sets the ICAO code in the MetarGUI text field and triggers a
     * click event.
     *
     * @param icao The ICAO code to be sent.
     */
    public void sendIcaoToMetarAPP(String icao) {
        MetarGUI.instance.ICAOCODETextField.setText(icao);
        MetarGUI.instance.doClick();
    }

    /**
     * Opens the full METAR data as JSON in a new GUI window.
     *
     * @param icao The ICAO code for which to retrieve and display the full METAR
     *             data.
     */
    public void openFullAsJson(String icao) {
        try {
            new FullJsonGUI(getMetarData(icao).toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
