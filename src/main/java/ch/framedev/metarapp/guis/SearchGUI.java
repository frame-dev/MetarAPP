package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.util.TypeIndex;
import ch.framedev.metarapp.data.ListOfIcaos;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.Variables;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @deprecated use {@link BetterSearchGUI}
 */
@Deprecated
public class SearchGUI extends JFrame {

    private static final long serialVersionUID = 1L;
	private final JComboBox<String> comboBox;

    @Deprecated
    public SearchGUI() {
        setIconImage(Variables.getLogoImage());
        JPanel scrollPane = new JPanel();
        setTitle("Search");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(scrollPane);
        JLabel countryCodeLabel = new JLabel("Country Code");
        // JTextField countryCode = new JTextField("Country Code");
        comboBox = new JComboBox<>();
        populateComboBox();
        JCheckBox useCC = new JCheckBox("Use Country Code");
        JLabel regionNameLabel = new JLabel("Region Name");
        JTextField regionName = new JTextField("Region Name");
        JButton searchButton = new JButton("Search Button");
        JTextArea textArea = new JTextArea();
        JButton showCountries = new JButton("Show Country's and there Codes(CC)");
        JScrollPane scrollPanel = new JScrollPane(textArea);
        JButton betterSearch = new JButton("Better Search");
        textArea.setEditable(false);
        scrollPanel.setPreferredSize(new Dimension(580, 300));
        ListOfIcaos listOfIcaos = new ListOfIcaos();

        showCountries.addActionListener(listener -> {
            new ShowAllCountryCodes();
        });


        searchButton.addActionListener(listener -> {
            searchForData(useCC, listOfIcaos, regionName, textArea);
        });

        betterSearch.addActionListener( listener -> {
            BetterSearchGUI.main(Main.args);
            setVisible(false);
        });
        scrollPane.add(countryCodeLabel);
        scrollPane.add(showCountries);
        //scrollPane.add(countryCode);
        scrollPane.add(comboBox);
        scrollPane.add(useCC);
        scrollPane.add(regionNameLabel);
        scrollPane.add(regionName);
        scrollPane.add(searchButton);
        scrollPane.add(betterSearch);
        scrollPane.add(scrollPanel);

        if ((boolean) Main.settings.get("dark-mode")) {
            setDarkMode(scrollPane, scrollPanel, countryCodeLabel, showCountries, useCC, regionNameLabel, searchButton, regionName, textArea, betterSearch);
        }

        pack();
        setVisible(true);
    }

    private void setDarkMode(JPanel scrollPane, JScrollPane scrollPanel, JLabel countryCodeLabel, JButton showCountries, JCheckBox useCC, JLabel regionNameLabel, JButton searchButton, JTextField regionName, JTextArea textArea, JButton betterSearch) {
        scrollPane.setBackground(Color.DARK_GRAY);
        scrollPanel.setBackground(Color.GRAY);
        countryCodeLabel.setForeground(Color.WHITE);
        setColorButton(showCountries);
        useCC.setForeground(Color.WHITE);
        useCC.setBackground(Color.DARK_GRAY);
        regionNameLabel.setForeground(Color.WHITE);
        setColorButton(searchButton);
        regionName.setBackground(Color.LIGHT_GRAY);
        scrollPanel.setBackground(Color.LIGHT_GRAY);
        textArea.setBackground(Color.LIGHT_GRAY);
        setColorButton(betterSearch);
    }

    private void searchForData(JCheckBox useCC, ListOfIcaos listOfIcaos, JTextField regionName, JTextArea textArea) {
        List<String[]> list;
        if (useCC.isSelected()) {
            list = listOfIcaos.getByCountryCode(TypeIndex.COUNTRY_CODE, (String) comboBox.getSelectedItem());
            if (!regionName.getText().isEmpty() && !regionName.getText().equalsIgnoreCase("Region Name")) {
                list.removeIf(args -> !args[1].equalsIgnoreCase(regionName.getText()));
            }
            StringBuilder builder = new StringBuilder();
            builder.append("country_code,region_name,iata,icao,airport,latitude,longitude").append("\n");
            for (String[] args : list) {
                builder.append(Arrays.toString(args)).append("\n");
            }
            textArea.setText(builder.toString());
        } else if (!regionName.getText().isEmpty() && !regionName.getText().equalsIgnoreCase("Region Name")) {
            list = listOfIcaos.getByRegionName(regionName.getText());
            StringBuilder builder = new StringBuilder();
            builder.append("country_code,region_name,iata,icao,airport,latitude,longitude").append("\n");
            for (String[] args : list) {
                builder.append(Arrays.toString(args)).append("\n");
            }
            textArea.setText(builder.toString());
        }
        pack();
    }

    public void setColorButton(JButton button) {
        LoginFrame.setColorButton(button);
    }

    public void populateComboBox() {
        ListOfIcaos listOfIcaos = new ListOfIcaos();
        for (String countryCode : listOfIcaos.getCountryCodeStringList()) {
            comboBox.addItem(countryCode);
        }
    }
}
