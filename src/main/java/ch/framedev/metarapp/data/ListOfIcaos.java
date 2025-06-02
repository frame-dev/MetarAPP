package ch.framedev.metarapp.data;

import ch.framedev.csvutils.CsvUtils;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.TypeIndex;
import com.opencsv.exceptions.CsvException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ch.framedev.metarapp.main.Main.getFilePath;

@SuppressWarnings("ALL")
public class ListOfIcaos {

    public @NotNull List<String[]> listIcaos;

    /**
     * Constructs a new instance of ListOfIcaos.
     * This class is responsible for loading and managing a list of ICAO codes and their corresponding data.
     * The data is loaded from a CSV file located at "files/iata-icao.csv".
     * The CSV file should contain the following columns: "country_code", "region_name", "iata", "icao", "airport", "latitude", "longitude".
     * The loaded data is stored in the listIcaos field.
     *
     * @throws RuntimeException If an error occurs while reading the CSV file or if any required columns are missing.
     */
    public ListOfIcaos() {
        this.listIcaos = new ArrayList<>();

        try {
            String[] rows = {"country_code", "region_name", "iata", "icao", "airport", "latitude", "longitude"};
            this.listIcaos = new CsvUtils().getDataFromCSVFile(new File(getFilePath() + "files/iata-icao.csv"),
                    rows);
        } catch (IOException | CsvException e) {
            Main.getLogger().error("Error loading iata-icao.csv : " + ErrorCode.ERROR_LOAD.getError(), e);
            Main.loggerUtils.addLog("Error loading iata-icao.csv : " + ErrorCode.ERROR_LOAD.getError());
            throw new RuntimeException(e);
        }
    }

    public List<String> getIcaos() {
        List<String> icaos = new ArrayList<>();
        for (String[] icao : listIcaos) {
            icaos.add(icao[TypeIndex.ICAO.getIndex()]);
        }
        return icaos;
    }

    public List<String> getIATAs() {
        List<String> iatas = new ArrayList<>();
        for (String[] iata : listIcaos) {
            iatas.add(iata[TypeIndex.IATA.getIndex()]);
        }
        return iatas;
    }

    public List<String> getCountryCodes() {
        List<String> countryCodes = new ArrayList<>();
        for (String[] countryCode : listIcaos) {
            countryCodes.add(countryCode[TypeIndex.COUNTRY_CODE.getIndex()]);
        }
        return countryCodes;
    }

    public List<String> getRegionNames() {
        List<String> regionNames = new ArrayList<>();
        for (String[] regionName : listIcaos) {
            regionNames.add(regionName[TypeIndex.REGION_NAME.getIndex()]);
        }
        return regionNames;
    }

    public String[] getICAO(TypeIndex typeIndex, String params) {
        for (String[] icao : listIcaos) {
            if (icao[typeIndex.getIndex()].equalsIgnoreCase(params))
                return icao;
        }
        return null;
    }

    public List<String[]> getByCountryCode(TypeIndex index, String params) {
        List<String[]> strings = new ArrayList<>();
        for (String[] icao : listIcaos) {
            if (icao[index.getIndex()].equalsIgnoreCase(params)) {
                strings.add(icao);
            }
        }
        return strings;
    }

    public List<String[]> getByRegionName(String params) {
        List<String[]> strings = new ArrayList<>();
        for (String[] icao : listIcaos) {
            if (icao[TypeIndex.REGION_NAME.getIndex()].equalsIgnoreCase(params)) {
                strings.add(icao);
            }
        }
        return strings;
    }

    public List<String> getCountryCodeStringList() {
        List<String> countryCodes = new ArrayList<>();
        for (String[] icao : listIcaos) {
            if (!countryCodes.contains(icao[TypeIndex.COUNTRY_CODE.getIndex()]))
                countryCodes.add(icao[TypeIndex.COUNTRY_CODE.getIndex()]);
        }
        return countryCodes;
    }

    public List<List<String>> getCountryCodeAndData() {
        List<List<String>> countryCodes = new ArrayList<>();
        for (String[] icao : listIcaos) {
            countryCodes.add(new ArrayList<>(Arrays.stream(icao).collect(Collectors.toList())));
        }
        return countryCodes;
    }

    public List<List<String>> getRegionNamesAndData() {
        List<List<String>> regionCode = new ArrayList<>();
        for (String[] icao : listIcaos) {
            regionCode.add(new ArrayList<>(Arrays.stream(icao).collect(Collectors.toList())));
        }
        return regionCode;
    }
}
