package ch.framedev.metarapp.guis;

/*
 * ch.framedev.metarapp.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 11.05.2024 17:12
 */

import ch.framedev.metarapp.data.ListOfIcaos;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.CloseListener;
import ch.framedev.metarapp.util.Locale;
import ch.framedev.metarapp.util.TypeIndex;
import ch.framedev.metarapp.util.Variables;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static ch.framedev.metarapp.main.Main.localeUtils;

public class BetterSearchGUI {
    static JFrame frame;
    private JPanel panel;
    private JTextField textField1;
    private JCheckBox searchByICAOCheckBox;
    private JCheckBox searchByCountryCodeCheckBox;
    private JCheckBox searchByRegionCheckBox;
    private JCheckBox searchByIATACheckBox;
    private JTextArea textArea;
    private JTextField secondSearch;
    private JLabel firstSearchLabel;
    private JLabel secondSearchLabel;
    private JLabel firstSearch;
    private JLabel secondSearchLabelInfo;
    private JLabel results;
    private JButton showAllCountryCodesButton;
    private final List<JCheckBox> selected = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public BetterSearchGUI() {
        EventBus.dispatchRefreshEvent(new CallRefreshEvent(Main.from, "betterSearchGui"));
        frame.addWindowListener(new CloseListener("betterSearchGui", "main"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ch.framedev.metarapp.util.Locale locale = localeUtils.getLocale();
        if (locale == Locale.DE_DE)
            frame.setTitle("Bessere Suche");
        else
            frame.setTitle("Better Search");
        searchByICAOCheckBox.addActionListener(e -> handleSelectionChanged(searchByICAOCheckBox));
        searchByIATACheckBox.addActionListener(e -> handleSelectionChanged(searchByIATACheckBox));
        searchByRegionCheckBox.addActionListener(e -> handleSelectionChanged(searchByRegionCheckBox));
        searchByCountryCodeCheckBox.addActionListener(e -> handleSelectionChanged(searchByCountryCodeCheckBox));

        textArea.setEditable(false);

        frame.setIconImage(Variables.getLogoImage());

        ListOfIcaos listOfIcaos = new ListOfIcaos();
        final List<String>[] newUpdated = new ArrayList[]{new ArrayList<>()};
        final List<List<String>>[] newListData = new ArrayList[]{new ArrayList<>()};
        final HashMap<String, List<String>>[] newData = new HashMap[]{new HashMap<>()};
        final List<String> updated = new ArrayList<>();
        final HashMap<String, List<String>> data = new HashMap<>();

        textField1.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(listOfIcaos, updated, data, newUpdated, newData, newListData);
            }
        });
        secondSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleasedSecond(listOfIcaos, newUpdated, newListData);
            }
        });
        if((boolean) Main.settings.get("dark-mode")) {
            setDarkMode();
        }
        showAllCountryCodesButton.addActionListener(e -> new ShowAllCountryCodes());
    }

    private void setDarkMode() {
        panel.setBackground(Color.DARK_GRAY);
        searchByICAOCheckBox.setBackground(Color.DARK_GRAY);
        searchByICAOCheckBox.setForeground(Color.WHITE);
        searchByIATACheckBox.setBackground(Color.DARK_GRAY);
        searchByIATACheckBox.setForeground(Color.WHITE);
        searchByCountryCodeCheckBox.setBackground(Color.DARK_GRAY);
        searchByCountryCodeCheckBox.setForeground(Color.WHITE);
        searchByRegionCheckBox.setBackground(Color.DARK_GRAY);
        searchByRegionCheckBox.setForeground(Color.WHITE);
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
        textField1.setBackground(Color.LIGHT_GRAY);
        textField1.setForeground(Color.WHITE);
        secondSearch.setBackground(Color.LIGHT_GRAY);
        secondSearch.setForeground(Color.WHITE);
        firstSearchLabel.setForeground(Color.WHITE);
        secondSearchLabel.setForeground(Color.WHITE);
        firstSearch.setForeground(Color.WHITE);
        secondSearchLabelInfo.setForeground(Color.WHITE);
        results.setForeground(Color.WHITE);
        MetarGUI.instance.setColorButton(showAllCountryCodesButton);
    }

    public void handleSelectionChanged(JCheckBox checkBox) {
        if (checkBox.isSelected() && !selected.contains(checkBox) && selected.size() < 2) selected.add(checkBox);
        else selected.remove(checkBox);
        updateSelectionText();
    }

    public void updateSelectionText() {
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i) != null && i == 0) {
                firstSearchLabel.setText("First Search : " + selected.get(i).getText());
            } else secondSearchLabel.setText("Second Search : " + selected.get(i).getText());
        }
    }

    public void handleKeyReleased(ListOfIcaos listOfIcaos, List<String> updated, HashMap<String, List<String>> data, List<String>[] newUpdated,
                                  HashMap<String, List<String>>[] newData, List<List<String>>[] newListData) {
        updated.clear();
        data.clear();
        for (int i = 0; i < selected.size(); i++) {
            if (i == 0) {
                switch (selected.get(i).getText()) {
                    case "Search by ICAO":
                        searchByIcao(listOfIcaos, updated, data);
                        results.setText("Results : " + data.size());
                        break;
                    case "Search by IATA":
                        searchByIATA(listOfIcaos, updated, data);
                        results.setText("Results : " + data.size());
                        break;
                    case "Search by Country Code":
                        searchByCountryCode(listOfIcaos, newUpdated, newListData, newData);
                        results.setText("Results : " + newListData[0].size());
                        break;
                    case "Search by Region":
                        searchByRegion(listOfIcaos, newUpdated, newListData, newData);
                        results.setText("Results : " + newListData[0].size());
                        break;
                }
                newData[0] = data;
                newUpdated[0] = updated;
            }
        }
    }

    public void handleKeyReleasedSecond(ListOfIcaos listOfIcaos, List<String>[] newUpdated,
                                        List<List<String>>[] newListData) {
        for (int i = 0; i < selected.size(); i++) {
            if (i == 1) {
                switch (selected.get(i).getText()) {
                    case "Search by ICAO":
                        results.setText("Results : " + searchBySecondICAO(listOfIcaos, newUpdated).size());
                        break;
                    case "Search by IATA":
                        results.setText("Results : " + searchBySecondIATA(listOfIcaos, newUpdated).size());
                        break;
                    case "Search by Country Code":
                        searchBySecondCountryCode(newListData);
                        results.setText("Results : " + newListData[0].size());
                        break;
                    case "Search by Region":
                        searchBySecondRegion(newListData);
                        results.setText("Results : " + newListData[0].size());
                        break;
                }
            }
        }
    }

    private void searchByIcao(ListOfIcaos listOfIcaos, List<String> updated, HashMap<String, List<String>> data) {
        for (String icao : listOfIcaos.getIcaos()) {
            if (icao.contains(textField1.getText())) {
                updated.add(icao);
                data.put(icao, Arrays.stream(listOfIcaos.getICAO(TypeIndex.ICAO, icao)).collect(Collectors.toList()));
            }
        }
        filterDataFirst(updated, data, textField1, "ICAO");
    }

    private void searchByIATA(ListOfIcaos listOfIcaos, List<String> updated, HashMap<String, List<String>> data) {
        for (String iata : listOfIcaos.getIATAs()) {
            if (iata.contains(textField1.getText())) {
                updated.add(iata);
                data.put(iata, Arrays.stream(listOfIcaos.getICAO(TypeIndex.IATA, iata)).collect(Collectors.toList()));
            }
        }
        filterDataFirst(updated, data, textField1, "IATA");
    }

    private void filterDataFirst(List<String> updated, HashMap<String, List<String>> data, JTextField textField1, String type) {
        updated.removeIf(icao -> !icao.contains(textField1.getText()));
        List<String> removedDuplicatesIata = removeDuplicates(updated);
        Collections.sort(removedDuplicatesIata);
        final String[] textIata = {""};
        removeDuplicatesExecute(data, removedDuplicatesIata, textIata);
        textArea.setText(type + "; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + textIata[0]);
        frame.pack();
    }

    private void searchByCountryCode(ListOfIcaos listOfIcaos, List<String>[] newUpdated, List<List<String>>[] newListData, HashMap<String, List<String>>[] newData) {
        StringBuilder stringBuilder = new StringBuilder();
        final List<List<String>> countryCodesF = new ArrayList<>();
        for (List<String> countryCode : listOfIcaos.getCountryCodeAndData()) {
            if (countryCode.get(0).contains(textField1.getText())) {
                stringBuilder.append(countryCode.get(0)).append("; Data=").append(countryCode).append("\n");
                addData(newUpdated, newData, countryCodesF, countryCode);
            }
        }
        newListData[0] = countryCodesF;
        textArea.setText("Country Code; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + stringBuilder);
        frame.pack();
    }

    private void searchByRegion(ListOfIcaos listOfIcaos, List<String>[] newUpdated, List<List<String>>[] newListData, HashMap<String, List<String>>[] newData) {
        StringBuilder stringBuilderRegion = new StringBuilder();
        final List<List<String>> regionNamesF = new ArrayList<>();
        for (List<String> regionNames : listOfIcaos.getRegionNamesAndData()) {
            if (regionNames.get(1).contains(textField1.getText())) {
                stringBuilderRegion.append(regionNames.get(1)).append("; Data=").append(regionNames).append("\n");
                addData(newUpdated, newData, regionNamesF, regionNames);
            }
        }
        newListData[0] = regionNamesF;
        textArea.setText("Region Name; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + stringBuilderRegion);
        frame.pack();
    }

    private void addData(List<String>[] newUpdated, HashMap<String, List<String>>[] newData, List<List<String>> modifiedList, List<String> dataList) {
        modifiedList.add(dataList);
        if (selected.size() == 2) {
            if (selected.get(1).getText().equalsIgnoreCase(searchByICAOCheckBox.getText())) {
                newUpdated[0].add(dataList.get(3));
                newData[0].put(dataList.get(3), dataList);
            }
            if (selected.get(1).getText().equalsIgnoreCase(searchByIATACheckBox.getText())) {
                newUpdated[0].add(dataList.get(2));
                newData[0].put(dataList.get(2), dataList);
            }
        }
    }

    private HashMap<String, List<String>> searchBySecondICAO(ListOfIcaos listOfIcaos, List<String>[] newUpdated) {
        List<String> modified = new ArrayList<>(newUpdated[0]);
        HashMap<String, List<String>> modifiedData = new HashMap<>();
        for (String icao : newUpdated[0]) {
            if (icao.contains(secondSearch.getText())) {
                modified.add(icao);
                modifiedData.put(icao, Arrays.stream(listOfIcaos.getICAO(TypeIndex.ICAO, icao)).collect(Collectors.toList()));
            }
        }
        filterData(modified, modifiedData, secondSearch, "ICAO");
        return modifiedData;
    }

    private void filterData(List<String> modified, HashMap<String, List<String>> modifiedData, JTextField secondSearch, String type) {
        modified.removeIf(icao -> !icao.contains(secondSearch.getText()));
        List<String> removedDuplicates = removeDuplicates(modified);
        Collections.sort(removedDuplicates);
        final String[] text = {""};
        removeDuplicatesExecute(modifiedData, removedDuplicates, text);
        textArea.setText(type + "; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + text[0]);
        frame.pack();
    }

    private HashMap<String, List<String>> searchBySecondIATA(ListOfIcaos listOfIcaos, List<String>[] newUpdated) {
        List<String> modified = new ArrayList<>(newUpdated[0]);
        HashMap<String, List<String>> modifiedData = new HashMap<>();
        for (String iata : newUpdated[0]) {
            if (iata.contains(secondSearch.getText())) {
                modified.add(iata);
                modifiedData.put(iata, Arrays.stream(listOfIcaos.getICAO(TypeIndex.IATA, iata)).collect(Collectors.toList()));
            }
        }
        filterData(modified, modifiedData, secondSearch, "IATA");
        return modifiedData;
    }

    private void searchBySecondCountryCode(List<List<String>>[] newListData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> countryCode : newListData[0]) {
            if (countryCode.get(0).contains(secondSearch.getText())) {
                stringBuilder.append(countryCode.get(0)).append("; Data=").append(countryCode).append("\n");
            }
        }
        textArea.setText("Country Code; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + stringBuilder);
        frame.pack();
    }

    private void searchBySecondRegion(List<List<String>>[] newListData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> regionNames : newListData[0]) {
            if (regionNames.get(1).contains(secondSearch.getText())) {
                stringBuilder.append(regionNames.get(1)).append("; Data=").append(regionNames).append("\n");
            }
        }
        textArea.setText("Region Name; Data = country_code,region_name,iata,icao,airport,latitude,longitude" + "\n" + stringBuilder);
        frame.pack();
    }

    private void removeDuplicatesExecute(HashMap<String, List<String>> data, List<String> removedDuplicates, String[] text) {
        removedDuplicates.forEach(duplicate -> {
            StringBuilder stringData = new StringBuilder();
            List<String> dataList = data.get(duplicate);
            for (int x = 0; x < dataList.size(); x++) {
                stringData.append(dataList.get(x));
                if (x < dataList.size() - 1) {
                    stringData.append(",");
                }
            }
            text[0] += duplicate + "; Data = " + stringData + "\n";
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("SearchGUI");
        frame.setContentPane(new BetterSearchGUI().panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static <T> List<T> removeDuplicates(List<T> list) {
        // Create a Set to store unique elements
        Set<T> set = new HashSet<>(list);

        // Convert the Set back to a List

        return new ArrayList<>(set);
    }

    public static class CustomComparator implements Comparator<List<String>> {
        @Override
        public int compare(List<String> o1, List<String> o2) {
            String firstString_o1 = o1.get(0);
            String firstString_o2 = o2.get(0);
            return firstString_o1.compareTo(firstString_o2);
        }
    }
}
