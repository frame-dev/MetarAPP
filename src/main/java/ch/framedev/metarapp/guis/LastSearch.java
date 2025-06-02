package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.events.SendIcaoEvent;
import ch.framedev.metarapp.util.CloseListener;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.Locale;
import ch.framedev.metarapp.util.LoginUtils;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.requests.AirportRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static ch.framedev.metarapp.guis.MetarGUI.metarRequest;
import static ch.framedev.metarapp.main.Main.*;

public class LastSearch extends JFrame {

    private static final long serialVersionUID = 2230213234419643838L;
    private final JList<String> stringList;

    public LastSearch() {
        createFavouriteFile();
        EventBus.dispatchRefreshEvent(new CallRefreshEvent(Main.from, "lastSearch"));
        this.addWindowListener(new CloseListener("lastSearch", "main"));
        Locale locale = localeUtils.getLocale();
        if (locale == Locale.DE_DE)
            setTitle("Letzte Suche");
        else
            setTitle("Last Search");
        JPanel panel = new JPanel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.stringList = new JList<>(new ListModelString<>());
        loadList();
        stringList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    setIcao();
                }
            }
        });
        JTextField favouriteTextField = new JTextField();
        favouriteTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    String searchText = favouriteTextField.getText().toLowerCase();
                    if(stringList.getModel() instanceof DefaultListModel) {
                        DefaultListModel<String> model = (DefaultListModel<String>) stringList.getModel();
                        model.clear();
                        for (String icao : MetarGUI.userData.getIcaos()) {
                            if (icao.toLowerCase().contains(searchText)) {
                                saveToFavouriteFile(icao);
                                System.out.println("Saving to favourites: " + icao);
                                loadList();
                            }
                        }
                    }
                }
            }

            private void saveToFavouriteFile(String icao) {
                try(FileWriter writer = new FileWriter(new File(Main.getFilePath() + "files", "favourite_icao.txt"), true)) {
                    writer.write(icao);
                    writer.write(System.lineSeparator());
                    writer.flush();
                } catch (Exception ex) {
                    getLogger().log(Level.ERROR, "Error saving to favourites file: " + ex.getMessage(), ex);
                }
            }
        });
        panel.add(stringList);

        JButton deleteButton = getButton();
        panel.add(deleteButton);

        panel.add(favouriteTextField);

        if ((boolean) Main.settings.get("dark-mode")) {
            setColorButton(deleteButton);
            panel.setBackground(Color.DARK_GRAY);
            stringList.setBackground(Color.GRAY);
            stringList.setForeground(Color.WHITE);
        }

        pack();
        setVisible(true);
    }

    private void setIcao() {
        String selected = stringList.getSelectedValue();
        if(selected.contains(" (FAV)")) {
            selected = selected.replace(" (FAV)", "");
        }
        MetarGUI metarGUI = MetarGUI.instance;
        // Double-click detected
        metarGUI.ICAOCODETextField.setText(selected);
        metarGUI.ICAOCODETextField.setText(
                EventBus.dispatchSendIcaoEvent(new SendIcaoEvent(metarGUI.ICAOCODETextField.getText().toUpperCase())));
        try {
            if (MetarAPPApi.getInstance().isMetarAPIOnline())
                metarRequest.setICAO(metarGUI.ICAOCODETextField.getText());
            else {
                JOptionPane.showMessageDialog(null,
                        "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                getLogger().log(Level.ERROR,
                        "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "Metar API is not available"));
            }
        } catch (Exception ex) {
            getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError(), ex);
            JOptionPane.showMessageDialog(null,
                    localeUtils.getString("noAirportFound", "%ICAO%", metarGUI.ICAOCODETextField.getText()) + " "
                            + ErrorCode.ERROR_INVALID_REQUEST.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + metarGUI.ICAOCODETextField.getText()));
        }
        if (metarRequest.getResults() == 1) {
            metarGUI.flightCategory
                    .setText(localeUtils.getString("labelFlightCategory") + " : " + metarRequest.getFlightCategory());
            metarGUI.dateCreated.setText("Created at : " + metarRequest.getDateTime());
            metarGUI.utcTime.setText("UTC Time : " + Instant.now().toString());
            if (hasUpdate()) {
                metarGUI.updateLabel.setText("yes");
            } else {
                metarGUI.updateLabel.setText("no");
            }
            MetarGUI.frame.pack();
            metarGUI.currentIcaoLabel.setText("Current ICAO : " + metarGUI.ICAOCODETextField.getText().toUpperCase());
            if (metarGUI.addToSearchCheckBox.isSelected())
                metarGUI.addToJson(metarGUI.ICAOCODETextField.getText());
            try {
                if (MetarAPPApi.getInstance().isAirportAPIOnline()) {
                    MetarGUI.airportRequest = new AirportRequest(metarGUI.ICAOCODETextField.getText());
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Airport"));
                    getLogger().log(Level.ERROR,
                            "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Airport"));
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "Airport API is not available"));
                }
            } catch (Exception ex) {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError(),
                        ex);
                JOptionPane.showMessageDialog(null,
                        localeUtils.getString("noAirportFound", "%ICAO%", metarGUI.ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + metarGUI.ICAOCODETextField.getText()));
            }
        } else {
            getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
            JOptionPane.showMessageDialog(null,
                    localeUtils.getString("noAirportFound", "%ICAO%", metarGUI.ICAOCODETextField.getText()) + " "
                            + ErrorCode.ERROR_INVALID_REQUEST.getError());
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + metarGUI.ICAOCODETextField.getText()));
        }
    }

    @NotNull
    private JButton getButton() {
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(listener -> {
            if (!stringList.isSelectionEmpty()) {
                if (stringList.getSelectedValuesList().size() == 1) {
                    MetarGUI.userData.getIcaos().remove(stringList.getSelectedValue());
                    MetarGUI.userData.save();
                    delete(stringList.getSelectedValuesList());
                } else {
                    for (String selected : stringList.getSelectedValuesList()) {
                        MetarGUI.userData.getIcaos().remove(selected);
                        MetarGUI.userData.save();
                    }
                    delete(stringList.getSelectedValuesList());
                }
            }
            DefaultListModel<String> model = (DefaultListModel<String>) stringList.getModel();
            model.clear();
            loadList();
            pack();
        });
        return deleteButton;
    }

    public void setColorButton(JButton button) {
        LoginFrame.setColorButton(button);
    }

    private void delete(List<String> icaosList) {
        if (new File(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json").exists()) {
            try {
                List<String> icaos;
                FileReader fileReader = new FileReader(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                java.lang.reflect.Type type = new TypeToken<List<String>>() {
                }.getType();
                icaos = new Gson().fromJson(fileReader, type);
                DefaultListModel<String> model = (DefaultListModel<String>) stringList.getModel();
                model.clear();
                icaos.removeAll(icaosList);
                for (String icaoString : icaos) {
                    model.addElement(icaoString);
                }
                FileWriter writer = new FileWriter(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(icaos));
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                loggerUtils.addLog("Could not delete icaoList : " + ex.getMessage());
                getLogger().error("Could not delete icaoList", ex);
            }
        }
    }

    public void loadList() {
        ((DefaultListModel<String>)stringList.getModel()).clear();
        if ((boolean) Main.settings.get("offline-mode") || Main.DEVELOPMENT) {
            try {
                List<String> icaos;
                FileReader fileReader = new FileReader(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                java.lang.reflect.Type type = new TypeToken<List<String>>() {
                }.getType();
                icaos = new Gson().fromJson(fileReader, type);
                DefaultListModel<String> model = (DefaultListModel<String>) stringList.getModel();
                for (String icao : icaos) {
                    model.addElement(MetarAPPApi.getInstance().getFavouriteIcaos().contains(icao.toUpperCase()) ? icao.toUpperCase() + " (FAV)" : icao.toUpperCase());
                }
            } catch (Exception ex) {
                loggerUtils.addLog("Could not load icaoList : " + ex.getMessage());
                getLogger().error("Could not load icaoList", ex);
            }
        } else {
            DefaultListModel<String> model = (DefaultListModel<String>) stringList.getModel();
            List<String> icaos = MetarGUI.userData != null ? MetarGUI.userData.getIcaos() : Collections.emptyList();

            if (icaos != null)
                for (String icao : icaos) {
                    model.addElement(MetarAPPApi.getInstance().getFavouriteIcaos().contains(icao.toUpperCase()) ? icao.toUpperCase() + " (FAV)" : icao.toUpperCase());
                }
        }
    }

    private void createFavouriteFile() {
        File file = new File(Main.getFilePath() + "files", "favourite_icao.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception ex) {
                getLogger().log(Level.ERROR, "Error creating favourite ICAO file: " + ex.getMessage(), ex);
            }
        }
    }

    public static class ListModelString<T> extends DefaultListModel<T> {

        private static final long serialVersionUID = -6132946897889318299L;

    }
}
