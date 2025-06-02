package ch.framedev.metarapp.cli.requests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("unused")
public class AirportRequest {

    /**
     * The Airport ICAO Code
     * Required for the API
     */
    @NotNull
    final
    String icao;
    Response response;

    String responseString;

    JsonElement root;

    String key;

    {
        try {
            key = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("airportRequest"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AirportRequest(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            @NotNull
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", key); // <-- this is the important line

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }).build();
        Request request = new Request.Builder()
                .url("https://api.flightplandatabase.com/nav/airport/" + icao)
                .get().addHeader("application", "json")
                .build();
        this.response = client.newCall(request).execute();

        responseString = response.body().string();
        this.root = JsonParser.parseString(responseString);
        if(response.code() != 200)
            throw new IOException("Error!");
    }

    public String getIcao() {
        return root.getAsJsonObject().get("ICAO").getAsString();
    }

    public JsonArray getRunways() {
        return root.getAsJsonObject().get("runways").getAsJsonArray();
    }

    public JsonArray getNavAids() {
        JsonArray jsonArray = new JsonArray();
        for (JsonElement jsonObject : getRunways()) {
            jsonArray.add(jsonObject.getAsJsonObject().get("navaids").getAsJsonArray());
        }
        return jsonArray;
    }

    public JsonArray getAllIlsRunways() {
        JsonArray jsonArray = new JsonArray();
        for (JsonElement jsonElement : getNavAids()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                if (element.getAsJsonObject().has("frequency"))
                    jsonArray.add(element.getAsJsonObject());
            }
        }
        return jsonArray;
    }

    public JsonArray getAllRunways() {
        JsonArray jsonArray = new JsonArray();
        for (JsonElement jsonElement : getNavAids()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                jsonArray.add(element.getAsJsonObject());
            }
        }
        return jsonArray;
    }

    public String getILSFrequencyByRunway(String runway) {
        String ilsFrequency = "";
        for (JsonElement jsonElement : getAllIlsRunways()) {
            if (jsonElement.getAsJsonObject().get("runway").getAsString().equalsIgnoreCase(runway)) {
                if (jsonElement.getAsJsonObject().get("type").getAsString().equalsIgnoreCase("LOC-ILS")) {
                    double ilsDouble = jsonElement.getAsJsonObject().get("frequency").getAsDouble();
                    double decimalValue = ilsDouble / 1000000;
                    ilsFrequency = String.valueOf(decimalValue);
                }
            }
        }
        return ilsFrequency;
    }

    public double getBearing(String runway) {
        for (JsonElement jsonElement : getAllIlsRunways()) {
            if (jsonElement.getAsJsonObject().get("runway").getAsString().equalsIgnoreCase(runway)) {
                if (jsonElement.getAsJsonObject().has("bearing") && jsonElement.getAsJsonObject().get("type").getAsString().equalsIgnoreCase("LOC-ILS"))
                    return jsonElement.getAsJsonObject().get("bearing").getAsDouble();
            }
        }
        return 0;
    }

    public JsonElement getRoot() {
        return root;
    }

    @SuppressWarnings("unused")
    public static class BasicAuthInterceptor implements Interceptor {

        private final String credentials;

        public BasicAuthInterceptor(String user, String password) {
            this.credentials = Credentials.basic(user, password);
        }

        @Override
        public @NotNull Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

    }

    public void downloadAsJson(boolean timeFormat) throws IOException {
        File file;
        if (timeFormat)
            file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_airport_data-" + new MetarRequest(icao).getObserved().replace(":","-") + "-UTC.json");
        else
            file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_airport_data.json");
        if(!file.exists())
            file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        writer.flush();
        writer.close();
    }
}
