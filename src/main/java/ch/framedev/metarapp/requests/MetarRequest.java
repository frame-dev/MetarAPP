package ch.framedev.metarapp.requests;

import ch.framedev.metarapp.data.MetarData;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.events.RequestStatusCodeEvent;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.ErrorMessages;
import ch.framedev.simplejavautils.TextUtils;
import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetarRequest {

    /**
     * The Airport ICAO Code
     * Required for the API
     */
    @NotNull
    private String icao;
    /**
     * The Response from the API
     */
    private Response response;

    /**
     * The Response String from the API
     * Used for parsing the JSON
     */
    private String responseString;

    /**
     * The number of results returned by the API
     * Used to check if the request was successful
     */
    private int results;
    /**
     * The MetarData object containing the parsed data from the API
     */
    private MetarData metarData;

    /**
     * Utility class for text formatting
     */
    private final TextUtils textUtils = new TextUtils();

    /**
     * The root JSON element of the response
     * Used for parsing the JSON
     */
    private JsonElement root;

    /**
     * The API key for the CheckWX API
     * Decrypted from the connection token handler
     */
    private final String key;

    {
        try {
            key = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("metarRequest"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MetarRequest(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(4000, TimeUnit.MILLISECONDS)
                .readTimeout(4000, TimeUnit.MILLISECONDS).writeTimeout(4000, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder()
                .url("https://api.checkwx.com/metar/" + icao + "/decoded")
                .get().addHeader("X-API-Key", key)
                .build();
        this.response = client.newCall(request).execute();
        this.responseString = response.body().string();
        try {
            this.root = JsonParser.parseString(responseString);
            this.metarData = new Gson().fromJson(responseString, MetarData.class);
        } catch (JsonSyntaxException e) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText, e);
            Main.loggerUtils.addLog(errorText);
        }
        if (root != null)
            this.metarData = new Gson().fromJson(root, MetarData.class);
        else
            throw new IOException("Root is null");
        EventBus.dispatchRequestStatusCodeEvent(new RequestStatusCodeEvent("metar", String.valueOf(response.code())));
    }

    public MetarRequest() {
        icao = "";
    }

    public void setICAO(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(8000, TimeUnit.MILLISECONDS)
                .readTimeout(8000, TimeUnit.MILLISECONDS).writeTimeout(8000, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder()
                .url("https://api.checkwx.com/metar/" + icao + "/decoded")
                .get().addHeader("X-API-Key", key)
                .build();
        this.response = client.newCall(request).execute();
        responseString = response.body().string();
        try {
            this.root = JsonParser.parseString(responseString);
            this.metarData = new Gson().fromJson(responseString, MetarData.class);
        } catch (JsonSyntaxException e) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText, e);
            Main.loggerUtils.addLog(errorText);
        }
        this.results = root.getAsJsonObject().get("results").getAsInt();
        EventBus.dispatchRequestStatusCodeEvent(new RequestStatusCodeEvent("metar", String.valueOf(response.code())));

        this.metarData = new Gson().fromJson(root, MetarData.class);
    }

    public MetarData getMetarData() {
        return metarData;
    }

    public @NotNull String getIcao() {
        return icao;
    }

    public Response getResponse() {
        return response;
    }

    public JsonElement getRoot() {
        return root;
    }

    public int getResults() {
        return results;
    }

    public MetarData.Station getStation() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getStation();
    }

    public MetarData.Wind getWind() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getWind();
    }

    public String getWindPretty() {
        if (getWind() == null) return "N/A";
        int degrees = getWind().getDegrees();
        double speed_Kts = getWind().getSpeed_kts();
        return String.format("Degrees : %s\nSpeed Kts : %s", degrees, speed_Kts);
    }

    public List<MetarData.Cloud> getClouds() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getClouds();
    }

    public String getCloudsPretty() {
        StringBuilder prettyString = new StringBuilder(textUtils.centerTextWithSymbol("<Clouds>", '=', 8));
        prettyString.append("\n");
        if (getClouds().get(0).getText().equalsIgnoreCase("Clear skies")) {
            return "Clear Skies";
        }
        if (root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("ceiling") != null) {
            JsonObject ceiling = root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("ceiling").getAsJsonObject();
            List<MetarData.Cloud> clouds = getClouds();
            for (MetarData.Cloud cloud : clouds) {
                prettyString.append(textUtils.generateBox("Cloud"));
                prettyString.append("Feet : ").append(cloud.getFeet()).append("\n");
                prettyString.append("Look : ").append(cloud.getText()).append("\n");
            }
            prettyString.append("\n");
            prettyString.append(textUtils.generateBox("Ceiling"));
            prettyString.append("Feet : ").append(ceiling.get("feet")).append("\n");
            prettyString.append("Look : ").append(ceiling.get("text"));
        } else {
            List<MetarData.Cloud> clouds = getClouds();
            for (MetarData.Cloud cloud : clouds) {
                prettyString.append(textUtils.generateBox("Cloud"));
                prettyString.append("Feet : ").append(cloud.getFeet()).append("\n");
                prettyString.append("Look : ").append(cloud.getText()).append("\n");
            }
        }
        return prettyString.toString();
    }

    public MetarData.Visibility getVisibility() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getVisibility();
    }

    public String getVisibilityPretty() {
        if (getVisibility() == null) return "N/A";
        String prettyString = "<Visibility>" + "\n";
        prettyString += "Meters : " + getVisibility().getMeters();
        return prettyString;
    }

    public String getHumidityPercent() {
        return String.valueOf(metarData.getData().get(0).getHumidity().getPercent());
    }

    public MetarData.Barometer getQNH() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getBarometer();
    }

    public List<MetarData.Conditions> getConditions() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getConditions();
    }

    public String getPrettyConditions() {
        StringBuilder prettyString = new StringBuilder();
        if (getConditions() == null) return "No Rain";
        List<MetarData.Conditions> conditions = getConditions();
        for (MetarData.Conditions condition : conditions) {
            prettyString.append("Text : ").append(condition.getText()).append("\n");
            prettyString.append("Code : ").append(condition.getCode()).append("\n");
        }
        return prettyString.toString();
    }

    public String getFlightCategory() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getFlight_category();
    }

    public void downloadAsJson() throws IOException {
        File file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_metar.json");
        FileWriter writer = new FileWriter(file);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        writer.flush();
        writer.close();
    }

    public String getDateTime() {
        if (metarData == null) return null;
        if (metarData.getData().isEmpty()) return null;
        return metarData.getData().get(0).getObserved();
    }

    @Override
    public String toString() {
        return "MetarRequest{" +
                "icao='" + icao + '\'' +
                ", response=" + response +
                ", responseString='" + responseString + '\'' +
                ", results=" + results +
                ", root=" + root +
                '}';
    }
}
