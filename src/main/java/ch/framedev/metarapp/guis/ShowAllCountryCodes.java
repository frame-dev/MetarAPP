package ch.framedev.metarapp.guis;

import ch.framedev.csvutils.CsvUtils;
import ch.framedev.metarapp.data.ListOfIcaos;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.CloseListener;

import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ShowAllCountryCodes extends JFrame {

    private static final long serialVersionUID = 801451041416647750L;

    public ShowAllCountryCodes() {
        EventBus.dispatchRefreshEvent(new CallRefreshEvent("search", "country-codes"));
        this.addWindowListener(new CloseListener("country-codes", "main"));
        setTitle("Country Codes");
        setSize(400, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);

        JTextField filterField = new JTextField("Filter?");
        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setSelectionColor(Color.YELLOW);

        JScrollPane scroll = new JScrollPane(jTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel.add(filterField, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        // Apply dark mode settings
        if ((boolean) Main.settings.get("dark-mode")) {
            jTextArea.setBackground(Color.DARK_GRAY);
            jTextArea.setForeground(Color.WHITE);
            filterField.setBackground(Color.DARK_GRAY);
            filterField.setForeground(Color.WHITE);
        }

        // Load data from CSV file
        // and filter it based on the ListOfIcaos
        try {
            List<String[]> list = new CsvUtils().getDataFromCSVFile(
                    new File(Main.getFilePath() + "files/CountryCodes.csv"),
                    new String[]{"Name", "Code"});

            ListOfIcaos listOfIcaos = new ListOfIcaos();
            list.removeIf(data -> !listOfIcaos.getCountryCodeStringList().contains(data[1]));

            updateTextArea(jTextArea, list);

            // Filter logic
            filterField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterList();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterList();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterList();
                }

                private void filterList() {
                    String text = filterField.getText().toLowerCase();
                    List<String[]> filteredList = new ArrayList<>();
                    for (String[] data : list) {
                        if (data[0].toLowerCase().contains(text) || data[1].toLowerCase().contains(text)) {
                            filteredList.add(data);
                        }
                    }
                    updateTextArea(jTextArea, filteredList);
                }
            });

        } catch (IOException | CsvException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

    /**
     * Updates the text area with the provided data list.
     *
     * @param textArea  The JTextArea to update.
     * @param dataList  The list of data to display.
     */
    private void updateTextArea(JTextArea textArea, List<String[]> dataList) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("Name , Code");
        for (String[] data : dataList) {
            joiner.add(data[0] + " , " + data[1]);
        }
        textArea.setText(joiner.toString());
    }
}
