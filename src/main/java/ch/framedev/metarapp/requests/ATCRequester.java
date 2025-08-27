package ch.framedev.metarapp.requests;

import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.EncryptionUtil;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.ErrorMessages;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
public class ATCRequester {

    String icao;
    Response response;

    String responseString;

    JsonElement root;
    String key;
    {
        try {
            key = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("atcRequest"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ATCRequester(@NotNull String icao) throws IOException {
        this.icao = icao;
        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Request original = chain.request();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", key); // <-- this is the important line

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }).build();
        Request request = new Request.Builder()
                .url("https://airportdb.io/api/v1/airport/"+icao+"?apiToken=" + key)
                .get().addHeader("application", "json")
                .build();
        this.response = client.newCall(request).execute();
        responseString = response.body().string();
        try {
            this.root = JsonParser.parseString(responseString);
        } catch (JsonSyntaxException e) {
            String errorText = ErrorMessages.getErrorJsonParse(ErrorCode.ERROR_JSON_PARSE.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_PARSE, errorText));
            Main.getLogger().log(Level.ERROR, errorText, e);
            Main.loggerUtils.addLog(errorText);
        }
    }

    public void printData() {
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(root);
        System.out.println(jsonString);
    }
}
