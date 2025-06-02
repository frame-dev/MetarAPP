package ch.framedev.metarapp.cli.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ch.framedev.metarapp.cli.Main;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LastSearch {

    public List<String> lastSearch;
    private final String userName;

    public LastSearch(String userName) {
        this.userName = userName;
        lastSearch = new ArrayList<>();
        load();
    }

    private void load() {
        File file = new File(Main.getFilePath() + "files/" + userName + "_icaos.json");
        if (file.exists()) {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            try {
                lastSearch = new Gson().fromJson(new FileReader(file), type);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save() {
        File file = new File(Main.getFilePath() + "files/" + userName + "_icaos.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(lastSearch));
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(String last) {
        if (lastSearch == null) lastSearch = new ArrayList<>();
        if (!lastSearch.contains(last.toUpperCase()))
            lastSearch.add(last.toUpperCase());
    }
}
