package ch.framedev.metarapp.cli.requests;

import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MetarRequest {

    /**
     * The Airport ICAO Code
     * Required for the API
     */
    @NotNull
    String icao;
    Response response;

    String responseString;

    JsonElement root;
    final String key;

    {
        try {
            key = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("metarRequest"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MetarRequest(@NotNull String icao) throws IOException {

        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("https://api.checkwx.com/metar/" + icao + "/decoded")
                .get().addHeader("X-API-Key", key)
                .build();
        this.response = client.newCall(request).execute();
        responseString = response.body().string();
        this.root = JsonParser.parseString(responseString);
        if(response.code() != 200 || this.root.getAsJsonObject().get("results").getAsInt() == 0)
            throw new IOException("Error!");
    }

    public MetarRequest() {
        icao = "";
    }

    public void setICAO(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("https://api.checkwx.com/metar/" + icao + "/decoded")
                .get().addHeader("X-API-Key", key)
                .build();
        this.response = client.newCall(request).execute();
        responseString = response.body().string();
        this.root = JsonParser.parseString(responseString);
    }

    public Response getResponse() {
        return response;
    }

    public JsonElement getRoot() {
        return root;
    }

    public JsonObject getStation() {
        if (response == null) return null;
        return (JsonObject) root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("station");
    }

    public JsonObject getWind() {
        if (response == null) return null;
        return (JsonObject) root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("wind");
    }

    public String getWindPretty() {
        String degrees = getWind().get("degrees").getAsString();
        String speed_Kts = getWind().get("speed_kts").getAsString();
        return String.format("Degrees : %s\nSpeed Kts : %s", degrees, speed_Kts);
    }

    public JsonArray getClouds() {
        if (response == null) return null;
        return (JsonArray) root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("clouds");
    }

    public String getCloudsPretty() {
        StringBuilder prettyString = new StringBuilder("<Clouds>" + "\n");
        if (getClouds().get(0).getAsJsonObject().get("text").getAsString().equalsIgnoreCase("Clear skies")) {
            return "Clear Skies";
        }
        if (root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("ceiling") != null) {
            JsonObject ceiling = root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("ceiling").getAsJsonObject();
            JsonArray clouds = getClouds();
            for (int i = 0; i < clouds.size(); i++) {
                prettyString.append("Cloud" + "\n");
                prettyString.append("Feet : ").append(clouds.get(i).getAsJsonObject().get("feet")).append("\n");
                prettyString.append("Look : ").append(clouds.get(i).getAsJsonObject().get("text").getAsString()).append("\n");
            }
            prettyString.append("\n");
            prettyString.append("ceiling" + "\n");
            prettyString.append("Feet : ").append(ceiling.get("feet")).append("\n");
            prettyString.append("Look : ").append(ceiling.get("text"));
        } else {
            JsonArray clouds = getClouds();
            for (int i = 0; i < clouds.size(); i++) {
                prettyString.append("Cloud" + "\n");
                prettyString.append("Feet : ").append(clouds.get(i).getAsJsonObject().get("feet")).append("\n");
                prettyString.append("Look : ").append(clouds.get(i).getAsJsonObject().get("text").getAsString()).append("\n");
            }
        }
        return prettyString.toString();
    }

    public JsonObject getVisibility() {
        if (response == null) return null;
        return (JsonObject) root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("visibility");
    }

    public String getVisibilityPretty() {
        String prettyString = "<Visibility>" + "\n";
        prettyString += "Meters : " + getVisibility().get("meters").getAsString();
        return prettyString;
    }

    public String getHumidityPercent() {
        return String.valueOf(root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("humidity").getAsJsonObject().get("percent"));
    }

    public JsonObject getQNH() {
        if (response == null) return null;
        return root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("barometer").getAsJsonObject();
    }

    public JsonArray getConditions() {
        if (response == null) return null;
        return (JsonArray) root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("conditions");
    }

    public String getPrettyConditions() {
        StringBuilder prettyString = new StringBuilder();
        if (getConditions() == null) return "No Rain";
        JsonArray conditions = getConditions().getAsJsonArray();
        for (JsonElement condition : conditions) {
            prettyString.append("Text : ").append(condition.getAsJsonObject().get("text")).append("\n");
        }
        return prettyString.toString();
    }

    public String getFlightCategory() {
        return root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("flight_category").getAsString();
    }

    public String getObserved() {
        return root.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("observed").getAsString();
    }

    public void downloadAsJson(boolean timeFormat) throws IOException {
        File file;
        if (timeFormat)
            file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_metar-" + getObserved().replace(":", "-") + "-UTC.json");
        else
            file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_metar.json");
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.mkdir();
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        writer.flush();
        writer.close();
    }
}
