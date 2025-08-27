package ch.framedev.metarapp.requests;

import ch.framedev.metarapp.data.AirportData;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.events.RequestStatusCodeEvent;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.ErrorMessages;
import com.google.gson.*;
import okhttp3.*;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class AirportRequest {

    /**
     * The Airport ICAO Code
     * Required for the API
     */
    @NotNull
    final
    private String icao;

    /**
     * This is the Response from the API
     */
    private final Response response;

    /**
     * This is the Response from the API as Json String
     */

    private final String responseString;

    /**
     * This is the Response from the API as JsonElement
     */
    private JsonElement root;
    private AirportData airportData;

    protected transient final String key;

    {
        try {
            key = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("airportRequest"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AirportRequest(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().callTimeout(16000, TimeUnit.MILLISECONDS)
                .readTimeout(16000, TimeUnit.MILLISECONDS).writeTimeout(16000, TimeUnit.MILLISECONDS).addInterceptor(new Interceptor() {
                    @NotNull
                    public Response intercept(Interceptor.@NotNull Chain chain) throws IOException {
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
        try {
            this.root = JsonParser.parseString(responseString);
            if (root != null && root.getAsJsonObject().has("ICAO")) {
                airportData = new Gson().fromJson(responseString, AirportData.class);
                List<AirportData.Runway> runways = airportData.getRunways();
                for(AirportData.Runway runway : runways) {
                    for(AirportData.Navaid navaid : runway.getNavaids()) {
                        double ilsDouble = navaid.getFrequency();
                        BigDecimal ilsValue = BigDecimal.valueOf(ilsDouble);
                        BigDecimal divisor = BigDecimal.valueOf(1000000);

                        // Perform division with high precision
                        // You can specify the scale and rounding mode as needed
                        BigDecimal decimalValue = ilsValue.divide(divisor, MathContext.DECIMAL128);
                        navaid.setFrequency(decimalValue.doubleValue());
                    }
                }
            }
            else {
                String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
                Main.getLogger().log(Level.ERROR, errorText);
                Main.loggerUtils.addLog(errorText);
                if (root.getAsJsonObject().has("message"))
                    JOptionPane.showMessageDialog(null, "Error Airport Request Failed, Message : " + this.root.getAsJsonObject().get("message"), "Airport Request", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
                airportData = null;
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            }
            EventBus.dispatchRequestStatusCodeEvent(new RequestStatusCodeEvent("airport", String.valueOf(response.code())));
        } catch (JsonSyntaxException e) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText, e);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
        }
    }

    public AirportData getAirportData() {
        return airportData;
    }

    public JsonElement getRoot() {
        return root;
    }

    public Response getResponse() {
        return response;
    }

    public String getResponseString() {
        return responseString;
    }

    public String getIcao() {
        return root.getAsJsonObject().get("ICAO").getAsString();
    }

    public List<AirportData.Runway> getRunways() {
        if (airportData == null) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return airportData.getRunways();
    }

    public List<AirportData.Navaid> getNavAids() {
        if (airportData == null) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        List<AirportData.Navaid> navaids = new ArrayList<>();
        List<AirportData.Runway> runways = getRunways();
        if (runways == null) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        for (AirportData.Runway runway : runways)
            if (runway.getNavaids() != null)
                navaids.addAll(runway.getNavaids());
        return navaids;
    }

    public List<AirportData.Runway> getAllIlsRunways() {
        if (airportData == null) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        List<AirportData.Runway> runways = new ArrayList<>();
        for (AirportData.Runway runway : getRunways()) {
            for (AirportData.Navaid navaid : runway.getNavaids())
                if (navaid.getType().contains("LOC-ILS") && navaid.getFrequency() > 0)
                    runways.add(runway);
        }
        return runways;
    }

    public List<AirportData.Runway> getAllRunways() {
        return getRunways();
    }

    public String getILSFrequencyByRunway(String runway) {
        if (airportData == null) {
        String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
        Main.getLogger().log(Level.ERROR, errorText);
        Main.loggerUtils.addLog(errorText);
        this.root = JsonParser.parseString("{\"data\":\"null\"}");
        JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
        return null;
    }
        String ilsFrequency = "";
        for (AirportData.Navaid runwayData : getNavAids()) {
            if(runwayData.getRunway().equalsIgnoreCase(runway) && runwayData.getType().equalsIgnoreCase("LOC-ILS")) {
                ilsFrequency = String.valueOf(runwayData.getFrequency());
            }
        }
        return ilsFrequency;
    }

    public double getBearing(String runway) {
        if (airportData == null) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText);
            Main.loggerUtils.addLog(errorText);
            this.root = JsonParser.parseString("{\"data\":\"null\"}");
            JOptionPane.showMessageDialog(null, "Error Airport Request Failed", "Airport Request", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        for (AirportData.Runway jsonElement : getAllIlsRunways()) {
            if(jsonElement.getIdent().equalsIgnoreCase(runway))
                return jsonElement.getBearing();
        }
        return 0;
    }


    public void downloadAsJson() throws IOException {
        File file = new File(Main.getFilePath() + Main.settings.get("download-folder"), icao + "_airport_data.json");
        FileWriter writer = new FileWriter(file);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        writer.flush();
        writer.close();
    }
}
