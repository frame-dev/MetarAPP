package ch.framedev.metarapp.cli.utils;

import ch.framedev.csvutils.CsvUtils;
import ch.framedev.metarapp.cli.Main;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ch.framedev.metarapp.cli.Main.getFilePath;

public class LoadCountryCode {
    
    private List<String[]> countryCodeList;
    
    public LoadCountryCode() {
        this.countryCodeList = new ArrayList<>();
        if(new File(getFilePath() + Variables.DIRECTORY_FOLDER, "CountryCodes.csv").exists()) {
            String[] rows = {"Name","Code"};
            try {
                this.countryCodeList = new CsvUtils().getDataFromCSVFile(new File(getFilePath() + Variables.DIRECTORY_FOLDER, "CountryCodes.csv"), rows);
            } catch (IOException | CsvException e) {
                Main.getLogger().error("Could not load CountryCodes", e);
                throw new RuntimeException(e);
            }
        }
    }

    public List<String[]> getCountryCodeList() {
        return countryCodeList;
    }
}