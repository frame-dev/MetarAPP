package ch.framedev.metarapp.cli.data;

import ch.framedev.csvutils.CsvUtils;
import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.cli.utils.TypeIndex;

import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class ListOfIcaos {

    public List<String[]> listIcaos;

    public ListOfIcaos() {
        this.listIcaos = new ArrayList<>();

        try {
            this.listIcaos = new CsvUtils().getDataFromCSVFile(new File(Main.getFilePath() + "files/iata-icao.csv"), new String[]{"country_code", "region_name", "iata", "icao", "airport", "latitude", "longitude"});
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
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

    public List<String[]> getByRegionName(TypeIndex index, String params) {
        List<String[]> strings = new ArrayList<>();
        for (String[] icao : listIcaos) {
            if (icao[index.getIndex()].equalsIgnoreCase(params)) {
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
}
