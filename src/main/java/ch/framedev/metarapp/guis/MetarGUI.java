package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.data.ListOfIcaos;
import ch.framedev.metarapp.data.UserData;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.DisplayMetarEvent;
import ch.framedev.metarapp.events.DownloadedFileEvent;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.events.LogoutEvent;
import ch.framedev.metarapp.events.SendIcaoEvent;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.*;
import ch.framedev.metarapp.requests.AirportRequest;
import ch.framedev.metarapp.requests.MetarRequest;
import ch.framedev.simplejavautils.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static ch.framedev.metarapp.main.Main.*;
import static ch.framedev.metarapp.util.Variables.BRANCH;

public class MetarGUI {

    public static MetarRequest metarRequest;
    public static AirportRequest airportRequest;
    public static JFrame frame;
    public static MetarGUI instance;
    public static boolean logOut = false;

    public JTextField ICAOCODETextField;
    private JButton checkButton;
    private JButton showWind;
    private JButton showCloud;
    private JButton showVisibility;
    private JPanel panel;
    private JButton fullAsJsonButton;
    public JLabel flightCategory;
    private JButton showHumidity;
    private JButton downloadAsJsonButton;
    private JButton showQNH;
    private JButton showStation;
    private JButton showCondition;
    public JLabel dateCreated;
    private JButton lastSearchButton;
    public JCheckBox addToSearchCheckBox;
    private JButton searchICAOSButton;
    private JLabel versionLabel;
    private JButton updateButton;
    public JLabel updateLabel;
    private JButton settingsButton;
    private JLabel hasUpdateLabel;
    private JLabel icaoCodeLabel;
    private JButton changelogsButton;
    private JButton showWeatherMapButton;
    private JTextField runwayTextField;
    private JLabel bearingLabel;
    private JLabel ilsLabel;
    private JButton allILSRunwaysButton;
    private JCheckBox darkModeCheckBox;
    private JButton openSimBriefButton;
    private JLabel windLabel;
    private JButton adminGUIButton;
    private JButton converterButton;
    private JLabel languageSelected;
    private JLabel windImage;
    public JLabel utcTime;
    private JLabel currentUser;
    public JLabel currentIcaoLabel;

    public static UserData userData;

    public MetarGUI() {
        frame.setTitle("Metar Fetcher");

        instance = this;

        frame.setIconImage(Variables.getLogoImage());

        setTexts();

        try {
            if (BRANCH.equalsIgnoreCase("release")) {
                if (settings.getBoolean("popup-new-version")
                        && (!BUILD_NUMBER.equalsIgnoreCase(UpdateHandler.getLatestBuildNumber())) || (TESTING))
                    showPopup();
            } else {
                if (settings.getBoolean("popup-new-version")
                        && (!preRelease.equalsIgnoreCase(UpdateHandler.getLatestPreRelease())) || (TESTING)) {
                    showPrePopup();
                }
            }
        } catch (IOException e) {
            loggerUtils.addLog("Error getting latest Build Number : " + e.getMessage());
            getLogger().error("Error getting latest Build Number", e);
            if (settings.getBoolean("popup-new-version") && (!VERSION.equalsIgnoreCase(getNewVersion())) || (TESTING)) {
                showPopup();
            }
        }

        database.setOnline(LoginUtils.userNameStatic, true);
        database.setLastUsed(LoginUtils.userNameStatic, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));

        JMenuBar menuBar = getjMenuBar();
        frame.setJMenuBar(menuBar);

        if (branch.equalsIgnoreCase("release"))
            database.setVersion(LoginUtils.userNameStatic, VERSION);
        else
            database.setVersion(LoginUtils.userNameStatic, preRelease);
        database.setHasUpdate(LoginUtils.userNameStatic, hasUpdate());

        if (!(boolean) settings.get("first-time-run")) {
            JOptionPane.showMessageDialog(frame, "Take a look and press Settings for more!", "Information",
                    JOptionPane.PLAIN_MESSAGE);
            settings.set("first-time-run", true);
            settings.save();
        }

        if ((boolean) settings.get("dark-mode")) {
            panel.setBackground(Color.DARK_GRAY);
            versionLabel.setForeground(Color.WHITE);
            addToSearchCheckBox.setForeground(Color.WHITE);
            addToSearchCheckBox.setBackground(Color.DARK_GRAY);
            updateLabel.setForeground(Color.WHITE);
            dateCreated.setForeground(Color.WHITE);
            flightCategory.setForeground(Color.WHITE);
            hasUpdateLabel.setForeground(Color.WHITE);
            icaoCodeLabel.setForeground(Color.WHITE);
            bearingLabel.setForeground(Color.WHITE);
            ilsLabel.setForeground(Color.WHITE);
            runwayTextField.setBackground(Color.LIGHT_GRAY);
            ICAOCODETextField.setBackground(Color.LIGHT_GRAY);
            darkModeCheckBox.setForeground(Color.WHITE);
            darkModeCheckBox.setBackground(Color.DARK_GRAY);
            utcTime.setForeground(Color.WHITE);
            currentUser.setForeground(Color.WHITE);
            currentIcaoLabel.setForeground(Color.WHITE);

            windLabel.setForeground(Color.WHITE);
            languageSelected.setForeground(Color.WHITE);

            setColorButton(checkButton);
            setColorButton(lastSearchButton);
            setColorButton(showWind);
            setColorButton(showVisibility);
            setColorButton(showCloud);
            setColorButton(showHumidity);
            setColorButton(showQNH);
            setColorButton(showStation);
            setColorButton(showCondition);
            setColorButton(updateButton);
            setColorButton(downloadAsJsonButton);
            setColorButton(searchICAOSButton);
            setColorButton(fullAsJsonButton);
            setColorButton(settingsButton);
            setColorButton(changelogsButton);
            setColorButton(showWeatherMapButton);
            setColorButton(allILSRunwaysButton);
            setColorButton(openSimBriefButton);
            setColorButton(adminGUIButton);
            setColorButton(converterButton);
        }

        darkModeCheckBox.setSelected((Boolean) settings.get("dark-mode"));

        // initialize the MetarRequest
        metarRequest = new MetarRequest();
        if (branch.equalsIgnoreCase("release"))
            versionLabel.setText("Version : " + BUILD_NUMBER);
        else
            versionLabel.setText("Version : " + preRelease);

        adminGUIButton.setVisible(LoginUtils.userNameStatic.equalsIgnoreCase("Admin"));

        setup();

        currentUser.setText(String.format("Current User : %s", LoginUtils.userNameStatic));

        EventBus.dispatchRefreshEvent(new CallRefreshEvent(Main.from, "main"));
        Main.from = "main";
    }

    public static void loadUserData() {
        String userName = LoginUtils.userNameStatic;

        CompletableFuture<Integer> usedFuture = database.getUsed(userName)
                .thenApply(value -> {
                    return value != null ? value : 0;
                })
                .exceptionally(ex -> {
                    logError("Failed to get 'used' value for user: " + userName, ex);
                    return 0; // Default value
                });

        CompletableFuture<Integer> mapOpenedFuture = database.getMapOpened(userName)
                .thenApply(value -> value != null ? value : 0)
                .exceptionally(ex -> {
                    logError("Failed to get 'mapOpened' value for user: " + userName, ex);
                    return 0; // Default value
                });

        CompletableFuture<Integer> filesDownloadedFuture = database.getFilesDownloaded(userName)
                .thenApply(value -> value != null ? value : 0)
                .exceptionally(ex -> {
                    logError("Failed to get 'filesDownloaded' value for user: " + userName, ex);
                    return 0; // Default value
                });

        CompletableFuture<List<?>> icaosFuture = database.getIcaos(userName)
                .thenApply(value -> value != null && !value.isEmpty() ? value : Collections.emptyList())
                .exceptionally(ex -> {
                    logError("Failed to get 'ICAOs' for user: " + userName, ex);
                    return Collections.emptyList(); // Default value
                });

        CompletableFuture<String> lastUsedFuture = database.getLastUsed(userName)
                .thenApply(value -> value != null ? value : "Never")
                .exceptionally(ex -> {
                    logError("Failed to get 'lastUsed' for user: " + userName, ex);
                    return "Never"; // Default value
                });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                usedFuture, mapOpenedFuture, filesDownloadedFuture, icaosFuture, lastUsedFuture);

        allFutures.thenRun(() -> {
            try {
                int used = usedFuture.join();
                int mapOpened = mapOpenedFuture.join();
                int filesDownloaded = filesDownloadedFuture.join();
                @SuppressWarnings("unchecked")
                List<String> icaos = (List<String>) icaosFuture.join();
                String lastUsed = lastUsedFuture.join();

                userData = new UserData(
                        userName,
                        used,
                        mapOpened,
                        filesDownloaded,
                        icaos,
                        lastUsed);

                database.setVersion(userName, VERSION);
                database.setHasUpdate(userName, hasUpdate());
                userData.setUsed(userData.getUsed() + 1);
                userData.save();

                logInfo("User data initialized and saved for: " + userName);
            } catch (CompletionException e) {
                loggerUtils.addLog("Error constructing user data: " + e.getCause().getMessage());
                getLogger().error("Error constructing user data", e.getCause());
            }
        }).exceptionally(throwable -> {
            loggerUtils.addLog("Error loading user data: " + throwable.getMessage());
            getLogger().error("Error loading user data", throwable);
            return null;
        });
    }

    public static void logInfo(String message) {
        loggerUtils.addLog("INFO: " + message);
        getLogger().info(message);
    }

    public static void logError(String message, Throwable throwable) {
        loggerUtils.addLog("ERROR: " + message + " THROWED: " + throwable.getMessage());
        getLogger().error(message, throwable);
    }

    private static void showPopup() {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16); // Adjust font size as needed
        UIManager.put("OptionPane.messageFont", font);
        String boxText = new TextUtils().generateBox("There is a new version available!", "[" + getNewVersion() + "]");
        JOptionPane.showMessageDialog(frame, boxText, "Update available", JOptionPane.PLAIN_MESSAGE);
        frame.requestFocus();
        loggerUtils.addLog("There is a new version available! [" + getNewVersion() + "]");
    }

    private static void showPrePopup() {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16); // Adjust font size as needed
        UIManager.put("OptionPane.messageFont", font);
        String boxText = new TextUtils().generateBox("There is a new Pre-Release version available!",
                "[" + getLatestPreRelease() + "]");
        JOptionPane.showMessageDialog(frame, boxText, "Update available", JOptionPane.PLAIN_MESSAGE);
        frame.requestFocus();
        loggerUtils.addLog("There is a new Pre-Release version available! [" + getLatestPreRelease() + "]");
    }

    public void doClick() {
        ICAOCODETextField.setText(
                    EventBus.dispatchSendIcaoEvent(new SendIcaoEvent(ICAOCODETextField.getText().toUpperCase())));
            if (!new ListOfIcaos().getIcaos().contains(ICAOCODETextField.getText().toUpperCase())) {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
            try {
                if (MetarAPPApi.getInstance().isMetarAPIOnline())
                    metarRequest.setICAO(ICAOCODETextField.getText());
                else {
                    getLogger().log(Level.ERROR,
                            "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                    loggerUtils.addLog("Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Metar"));
                    return;
                }
            } catch (Exception ex) {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError(),
                        ex);
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
            if (metarRequest.getResults() == 1) {
                flightCategory.setText(
                        localeUtils.getString("labelFlightCategory") + " : " + metarRequest.getFlightCategory());
                dateCreated.setText("Created at : " + metarRequest.getDateTime());
                utcTime.setText("UTC Time : " + Instant.now().toString());
                if (hasUpdate()) {
                    updateLabel.setText("yes");
                } else {
                    updateLabel.setText("no");
                }
                currentIcaoLabel.setText("Current ICAO : " + ICAOCODETextField.getText().toUpperCase());
                frame.pack();
                if (addToSearchCheckBox.isSelected())
                    addToJson(ICAOCODETextField.getText().toUpperCase());
                try {
                    if (MetarAPPApi.getInstance().isAirportAPIOnline())
                        airportRequest = new AirportRequest(ICAOCODETextField.getText());
                    else {
                        getLogger().log(Level.ERROR, "Error API Down " + ErrorCode.ERROR_API_DOWN.getError("Airport"));
                        JOptionPane.showMessageDialog(null,
                                "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Metar"));
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                    + ErrorCode.ERROR_INVALID_REQUEST.getError());
                    loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText())
                            + " " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                    return;
                }

            } else {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
    }

    @SuppressWarnings("deprecation")
    public void setup() {
        languageSelected
                .setText(localeUtils.getString("selectedLanguage", "%LOCALE%", localeUtils.getString("language")));
        if (hasUpdate()) {
            updateLabel.setText("yes");
        } else {
            updateLabel.setText("no");
        }
        showWind.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showConfirmDialog(frame, metarRequest.getWindPretty(), "Winds at " + metarRequest.getIcao(),
                        JOptionPane.DEFAULT_OPTION);
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });

        adminGUIButton.addActionListener(event -> SwingUtilities.invokeLater(AdminGUI::new));

        ICAOCODETextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ICAOCODETextField.setText(EventBus
                            .dispatchSendIcaoEvent(new SendIcaoEvent(ICAOCODETextField.getText().toUpperCase())));
                    if (!new ListOfIcaos().getIcaos().contains(ICAOCODETextField.getText().toUpperCase())) {
                        getLogger().log(Level.ERROR,
                                "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        JOptionPane.showMessageDialog(frame,
                                localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        loggerUtils
                                .addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText())
                                        + " " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                        return;
                    }
                    try {
                        if (MetarAPPApi.getInstance().isMetarAPIOnline())
                            metarRequest.setICAO(ICAOCODETextField.getText());
                        else {
                            getLogger().log(Level.ERROR,
                                    "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                            loggerUtils.addLog("Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError());
                            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Metar"));
                            return;
                        }
                    } catch (Exception ex) {
                        getLogger().log(Level.ERROR,
                                "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError(), ex);
                        JOptionPane.showMessageDialog(frame,
                                localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        loggerUtils
                                .addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText())
                                        + " " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                        return;
                    }
                    if (metarRequest.getResults() == 1) {
                        flightCategory.setText(localeUtils.getString("labelFlightCategory") + " : "
                                + metarRequest.getFlightCategory());
                        dateCreated.setText("Created at : " + metarRequest.getDateTime());
                        utcTime.setText("UTC Time : " + Instant.now().toString());
                        if (hasUpdate()) {
                            updateLabel.setText("yes");
                        } else {
                            updateLabel.setText("no");
                        }
                        currentIcaoLabel.setText("Current ICAO : " + ICAOCODETextField.getText().toUpperCase());
                        frame.pack();
                        if (addToSearchCheckBox.isSelected())
                            addToJson(ICAOCODETextField.getText().toUpperCase());
                        try {
                            if (MetarAPPApi.getInstance().isAirportAPIOnline())
                                airportRequest = new AirportRequest(ICAOCODETextField.getText());
                            else {
                                getLogger().log(Level.ERROR,
                                        "Error API Down " + ErrorCode.ERROR_API_DOWN.getError("Airport"));
                                JOptionPane.showMessageDialog(null,
                                        "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                                loggerUtils.addLog("Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Airport"));
                                return;
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame,
                                    localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                            + ErrorCode.ERROR_INVALID_REQUEST.getError());
                            loggerUtils.addLog(
                                    localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                            + ErrorCode.ERROR_INVALID_REQUEST.getError());
                            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                            return;
                        }
                    } else {
                        getLogger().log(Level.ERROR,
                                "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        JOptionPane.showMessageDialog(frame,
                                localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        loggerUtils
                                .addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText())
                                        + " " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                        return;
                    }
                }
            }
        });

        checkButton.addActionListener(event -> {
            ICAOCODETextField.setText(
                    EventBus.dispatchSendIcaoEvent(new SendIcaoEvent(ICAOCODETextField.getText().toUpperCase())));
            if (!new ListOfIcaos().getIcaos().contains(ICAOCODETextField.getText().toUpperCase())) {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
            try {
                if (MetarAPPApi.getInstance().isMetarAPIOnline())
                    metarRequest.setICAO(ICAOCODETextField.getText());
                else {
                    getLogger().log(Level.ERROR,
                            "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                    loggerUtils.addLog("Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Metar"));
                    return;
                }
            } catch (Exception ex) {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError(),
                        ex);
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
            if (metarRequest.getResults() == 1) {
                flightCategory.setText(
                        localeUtils.getString("labelFlightCategory") + " : " + metarRequest.getFlightCategory());
                dateCreated.setText("Created at : " + metarRequest.getDateTime());
                utcTime.setText("UTC Time : " + Instant.now().toString());
                if (hasUpdate()) {
                    updateLabel.setText("yes");
                } else {
                    updateLabel.setText("no");
                }
                currentIcaoLabel.setText("Current ICAO : " + ICAOCODETextField.getText().toUpperCase());
                frame.pack();
                if (addToSearchCheckBox.isSelected())
                    addToJson(ICAOCODETextField.getText().toUpperCase());
                try {
                    if (MetarAPPApi.getInstance().isAirportAPIOnline())
                        airportRequest = new AirportRequest(ICAOCODETextField.getText());
                    else {
                        getLogger().log(Level.ERROR, "Error API Down " + ErrorCode.ERROR_API_DOWN.getError("Airport"));
                        JOptionPane.showMessageDialog(null,
                                "Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                        loggerUtils.addLog("Error API is not Available : " + ErrorCode.ERROR_API_DOWN.getError("Metar"));
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, "API is not available: Airport"));
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                    + ErrorCode.ERROR_INVALID_REQUEST.getError());
                    loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText())
                            + " " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                    return;
                }

            } else {
                getLogger().log(Level.ERROR, "Error no Airport Found " + ErrorCode.ERROR_INVALID_REQUEST.getError());
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                                + ErrorCode.ERROR_INVALID_REQUEST.getError());
                loggerUtils.addLog(localeUtils.getString("noAirportFound", "%ICAO%", ICAOCODETextField.getText()) + " "
                        + ErrorCode.ERROR_INVALID_REQUEST.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_INVALID_REQUEST, "No airport found for ICAO: " + ICAOCODETextField.getText()));
                return;
            }
        });
        showCloud.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame, metarRequest.getCloudsPretty(),
                        "Clouds at " + metarRequest.getIcao(), JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError()); 
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        fullAsJsonButton.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                new FullJsonGUI();
                try {
                    EventBus.dispatchDisplayMetarEvent(new DisplayMetarEvent(ICAOCODETextField.getText(),
                            MetarAPPApi.getInstance().getMetarData(MetarGUI.metarRequest.getIcao()).toString()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        showVisibility.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame, metarRequest.getVisibilityPretty());
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        showHumidity.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame, "Humidity in Percent : " + metarRequest.getHumidityPercent());
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        downloadAsJsonButton.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                DownloadUtils downloadUtils = new DownloadUtils();
                File file = downloadUtils.downloadMetarAirportDataToZip(metarRequest.getMetarData(),
                        airportRequest.getAirportData());
                JOptionPane.showMessageDialog(frame, "File downloaded!");
                userData.setFilesDownloaded(userData.getFilesDownloaded() + 1);
                userData.save();
                EventBus.dispatchDownloadedFileEvent(new DownloadedFileEvent(file.getAbsolutePath()));
                loggerUtils.addLog("File downloaded : " + file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        showQNH.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame, "hg : " + metarRequest.getQNH().getHg());
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        showStation.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame,
                        new GsonBuilder().setPrettyPrinting().create().toJson(metarRequest.getStation()));
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        showCondition.addActionListener(event -> {
            if (metarRequest.getResults() == 1) {
                JOptionPane.showMessageDialog(frame, metarRequest.getPrettyConditions());
            } else {
                JOptionPane.showMessageDialog(frame,
                        localeUtils.getString("noResults") + " " + ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                getLogger().log(Level.ERROR, ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                loggerUtils.addLog(ErrorCode.ERROR_AIRPORT_NOT_FOUND.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_AIRPORT_NOT_FOUND, "No results found for ICAO: " + ICAOCODETextField.getText()));
            }
        });
        lastSearchButton.addActionListener(e -> new LastSearch());
        searchICAOSButton.addActionListener(e -> {
            if (settings.getBoolean(Setting.USE_NEW_SEARCH.getKey())) {
                BetterSearchGUI.main(args);
            } else
                // noinspection deprecation
                new SearchGUI();
        });
        updateButton.addActionListener(event -> {
            if ((boolean) settings.get("auto-restart-after-update")) {
                if (hasUpdate()) {
                    settings.set("first-time-run", "true");
                    settings.set("show-changelogs", "false");
                    settings.save();
                }
                deleteOldVersionAndStartNew();
            }
            update().thenAccept(aBoolean -> {
                if (aBoolean) {
                    JOptionPane.showConfirmDialog(frame, "Successfully Updated", "Update", JOptionPane.DEFAULT_OPTION);
                    if (!new File(getFilePath() + "files", "changelogs.json").delete()) {
                        System.out.println("changelogs.json not found or cannot be deleted "
                                + ErrorCode.ERROR_FILE_NOT_FOUND.getError());
                        loggerUtils.addLog("changelogs.json not found or cannot be deleted "
                                + ErrorCode.ERROR_FILE_NOT_FOUND.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_FILE_NOT_FOUND, "changelogs.json not found or cannot be deleted"));
                    }
                    database.setLastUpdated(LoginUtils.userNameStatic,
                            new SimpleDateFormat("dd,MM,yyyy'T'HH:mm:ss").format(new Date(System.currentTimeMillis())));
                    database.setHasUpdate(LoginUtils.userNameStatic, false);
                    // Close APP after Update
                    loggerUtils.addLog("Updated Successfully!");
                    System.exit(1);
                } else {
                    String errorText = ErrorMessages.getErrorUpdate(ErrorCode.ERROR_UPDATE.getError());
                    JOptionPane.showConfirmDialog(frame, errorText, "Update Error", JOptionPane.DEFAULT_OPTION);
                    System.err.println(errorText);
                    loggerUtils.addLog(errorText);
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_UPDATE, "Update failed"));
                }
            });
        });
        settingsButton.addActionListener(e -> SettingsGUI.main(args));
        changelogsButton.addActionListener(e -> showChangelogs());
        showWeatherMapButton.addActionListener(event -> {
            if (LoginUtils.active) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://framedev.ch/files/metarapp/weather.html"));
                    userData.setMapOpened(userData.getMapOpened() + 1);
                    userData.save();
                } catch (Exception ex) {
                    getLogger().error("Failed to open link (WeatherMap) : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link (WeatherMap) : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open link (WeatherMap)"));
                }
            } else {
                JOptionPane.showMessageDialog(null, "You need to be Logged in to use this Feature.", "Message",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        runwayTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (airportRequest == null) {
                        loggerUtils.addLog(ErrorCode.ERROR_NULL_OBJECT.getError() + " : airportRequest is null");
                        JOptionPane.showMessageDialog(null,
                                ErrorCode.ERROR_NULL_OBJECT.getError() + " : airportRequest is null");
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_NULL_OBJECT, "airportRequest is null"));
                        return;
                    }
                    if (!runwayTextField.getText().isEmpty()) {
                        if (LoginUtils.active) {
                            bearingLabel
                                    .setText("Heading : " + airportRequest.getBearing(runwayTextField.getText()) + "°");
                            ilsLabel.setText(
                                    "ILS : " + airportRequest.getILSFrequencyByRunway(runwayTextField.getText()));
                        } else {
                            JOptionPane.showMessageDialog(null, "You need to be Logged in to use this Feature.",
                                    "Message", JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (airportRequest == null) {
                        loggerUtils.addLog(ErrorCode.ERROR_NULL_OBJECT.getError() + " : airportRequest is null");
                        JOptionPane.showMessageDialog(null,
                                ErrorCode.ERROR_NULL_OBJECT.getError() + " : airportRequest is null");
                        return;
                    }
                    if (!runwayTextField.getText().isEmpty()) {
                        if (LoginUtils.active) {
                            bearingLabel.setText("Heading : " + airportRequest.getBearing(runwayTextField.getText()));
                            ilsLabel.setText(
                                    "ILS : " + airportRequest.getILSFrequencyByRunway(runwayTextField.getText()));// Assuming
                                                                                                                  // this
                                                                                                                  // is
                                                                                                                  // within
                                                                                                                  // a
                                                                                                                  // Swing
                                                                                                                  // application
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    // Calculate degrees
                                    int degrees = Variables.calculateDegrees(metarRequest.getWind().getDegrees(),
                                            (int) airportRequest.getBearing(runwayTextField.getText()));
                                    if (degrees < 0) {
                                        degrees = degrees + 360;
                                    }

                                    // Get the weather image
                                    Image weatherImage = Variables.getWeatherImage();

                                    // Rotate the image
                                    BufferedImage rotatedImage = Variables
                                            .rotate(Variables.toBufferedImage(weatherImage), degrees);
                                    rotatedImage = Variables.resize(rotatedImage, 32, 32);

                                    // Update the JLabel with the rotated image
                                    ImageIcon icon = new ImageIcon(rotatedImage);
                                    windImage.setIcon(icon);

                                    // Set text for wind label
                                    windLabel.setText("Wind || kts : " + metarRequest.getWind().getSpeed_kts() + "kt"
                                            + " , Degrees : " + metarRequest.getWind().getDegrees());

                                    // Ensure windImage is added to its container and container is revalidated
                                    // if windImage is a JLabel within a JPanel, for example:
                                    windImage.getParent().revalidate();
                                    windImage.getParent().repaint(); // Optionally repaint

                                } catch (Exception ex) {
                                    // Handle any exceptions
                                    getLogger().error(ex.getMessage(), ex);
                                    JOptionPane.showMessageDialog(null,
                                            "Error : " + ErrorCode.ERROR_LOAD + " : " + ex.getMessage());
                                    loggerUtils.addLog(ErrorCode.ERROR_LOAD + " : " + ex.getMessage());
                                }
                                frame.pack();
                            });
                            // windLabel.setText(Direction.getDirectionSymbol(metarRequest.getWind().getDegrees()
                            // - (int) airportRequest.getBearing(runwayTextField.getText())) + " " +
                            // metarRequest.getWind().getSpeed_kts() + "kt");
                            // windLabel.setText("Wind : " +
                            // metarAPI.getWind().get("speed_kts").getAsString() + "kt from " +
                            // metarAPI.getWind().get("degrees").getAsString() + "°");
                        } else {
                            JOptionPane.showMessageDialog(null, "You need to be Logged in to use this Feature.",
                                    "Message", JOptionPane.PLAIN_MESSAGE);
                        }
                        frame.pack();
                    }
                }
            }
        });
        allILSRunwaysButton.addActionListener(event -> {
            if (LoginUtils.active) {
                new FullJsonGUI("ILS Frequency need to be trimmed to 109.25 as Example" + "\n"
                        + new GsonBuilder().setPrettyPrinting().create().toJson(airportRequest.getAllRunways()));
            } else {
                JOptionPane.showMessageDialog(null, "You need to be Logged in to use this Feature.", "Message",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        darkModeCheckBox.addActionListener(event -> {
            if (darkModeCheckBox.isSelected()) {
                settings.set("dark-mode", true);
                settings.save();
                setDarkMode();
            } else {
                settings.set("dark-mode", false);
                settings.save();
                setLightMode();
            }
        });
        openSimBriefButton.addActionListener(event -> {
            if (LoginUtils.active) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://dispatch.simbrief.com/home"));
                } catch (Exception ex) {
                    getLogger().error("Failed to open link (Simbrief) : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link (Simbrief) : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open link (Simbrief)"));
                }
            } else {
                JOptionPane.showMessageDialog(null, "You need to be Logged in to use this Feature.", "Message",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });

        converterButton.addActionListener(event -> new ConverterGUI());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                database.setOnline(LoginUtils.userNameStatic, false);
                loggerUtils.sendLogsFromIP();
                loggerUtils.moveToLogFile();
                DownloadUtils downloadUtils = new DownloadUtils();
                downloadUtils.zipLogFiles();
                database.setLastUsed(LoginUtils.userNameStatic,
                        new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
                System.out.println("Closing");
                System.exit(0);
            }
        });
    }

    private static final JMenuItem settingsItem = new JMenuItem("Settings");

    @NotNull
    private static JMenuBar getjMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu info = new JMenu("Info");
        JMenu account = new JMenu("Account Info");

        JMenuItem infoText = new JMenuItem("Info");
        String version;
        if (branch.equalsIgnoreCase("release"))
            version = BUILD_NUMBER;
        else
            version = preRelease;
        infoText.addActionListener(listener -> {
            MessageWithLink messageWithLink = new MessageWithLink(
                    "This Program is created by Framedev (<br><a href=\"https://framedev.ch\">https://framedev.ch</a>) \n"
                            +
                            "Version : " + version);
            JOptionPane.showMessageDialog(null, messageWithLink, "Info", JOptionPane.PLAIN_MESSAGE);
        });

        JMenuItem help = new JMenuItem("Help");
        help.addActionListener(Listener -> JOptionPane.showMessageDialog(null, new MessageWithLink(
                " If you need Help <a href=\"https://framedev.ch/files/metarapp/help.html\">click here</a>")));
        JMenuItem license = new JMenuItem("License");
        license.addActionListener(listener -> JOptionPane.showMessageDialog(null,
                new MessageWithLink(localeUtils.getString("infoMessage"))));

        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(listener -> {
            String message = EventBus.dispatchLogoutEvent(new LogoutEvent(LoginUtils.userNameStatic,
                    "You have been logged out successfully." + LoginUtils.userNameStatic));
            frame.setVisible(false);
            LoginUtils.userNameStatic = "";
            LoginUtils.active = false;
            MetarGUI.logOut = true;
            LoginFrame.main(args);
            JOptionPane.showMessageDialog(null,
                    message, "Logout", JOptionPane.INFORMATION_MESSAGE);
        });

        info.add(infoText);
        info.add(help);
        info.add(license);

        account.add(logout);

        JMenu toolsMenu = new JMenu("Tools");
        settingsItem.addActionListener(e -> SettingsGUI.main(args));

        JMenu documentsMenu = getDocumentsMenu();
        toolsMenu.add(settingsItem);

        JMenu plugins = new JMenu("Plugins");
        for (Plugin plugin : PluginManager.getInstance().getLoadedPlugins()) {
            JMenu pluginMenu = new JMenu(plugin.getName() + " - is enabled: "
                    + PluginManager.getInstance().isPluginEnabled(plugin.getName()));
            JMenuItem pluginItem = new JMenuItem("Plugin Info");
            pluginItem.addActionListener(e -> printPluginInfo(plugin));
            JMenuItem pluginStart = new JMenuItem("Enable / Disable");
            pluginStart.addActionListener(e -> {
                if (PluginManager.getInstance().isPluginEnabled(plugin.getName())) {
                    PluginManager.getInstance().disablePlugin(plugin.getName());
                    pluginStart.setText("Enable");
                    pluginMenu.setText(plugin.getName() + " - is enabled: false");
                } else {
                    PluginManager.getInstance().enablePlugin(plugin.getName());
                    pluginStart.setText("Disable");
                    pluginMenu.setText(plugin.getName() + " - is enabled: true");
                }
            });
            JMenuItem pluginWebsite = new JMenuItem("Website");
            pluginWebsite.addActionListener(e -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(plugin.getWebsite()));
                    } catch (IOException | URISyntaxException ex) {
                        getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                        loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                        EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open plugin website link"));
                    }
                }
            });
            pluginMenu.add(pluginItem);
            pluginMenu.add(pluginStart);
            pluginMenu.add(pluginWebsite);
            pluginMenu.add(new JSeparator());
            plugins.add(pluginMenu);
        }

        menuBar.add(account);
        menuBar.add(info);
        menuBar.add(toolsMenu);
        menuBar.add(documentsMenu);
        menuBar.add(plugins);
        return menuBar;
    }

    private static void printPluginInfo(Plugin plugin) {
        JOptionPane.showMessageDialog(null, plugin.getDescription(), plugin.getName() + "-" + plugin.getVersion(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static @NotNull JMenu getDocumentsMenu() {
        JMenu documentsMenu = new JMenu("Documents");
        JMenuItem errorExplanationMenuItem = getErrorExplanationMenuItem();
        JMenu apiExplanationMenu = new JMenu("API Explanation");
        JMenuItem apiExplanationGithub = getApiExplanationGithub();
        JMenuItem apiExplanationEditor = getApiExplanationSystemEditor();
        apiExplanationMenu.add(apiExplanationGithub);
        apiExplanationMenu.add(apiExplanationEditor);
        documentsMenu.add(errorExplanationMenuItem);
        documentsMenu.add(apiExplanationMenu);
        JMenu pluginMenu = new JMenu("Plugin Documentation");
        JMenuItem pluginExample = new JMenuItem("Plugin Example (Text Editor)");
        pluginExample.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("file://"
                            + new File(Variables.DOCUMENTS_DIRECTORY, "plugin_example.md").getAbsolutePath()));
                } catch (IOException | URISyntaxException ex) {
                    getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open plugin example link"));
                }
            }
        });
        pluginMenu.add(pluginExample);
        JMenuItem pluginGithub = new JMenuItem("Plugin Example (Github Preferred)");
        pluginGithub.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(
                            new URI("https://github.com/frame-dev/MetarAPP/blob/master/documents/plugin_example.md"));
                } catch (IOException | URISyntaxException ex) {
                    getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open plugin example link on GitHub"));
                }
            }
        });
        pluginMenu.add(new JMenuItem("---"));
        pluginMenu.add(pluginGithub);
        documentsMenu.add(pluginMenu);
        return documentsMenu;
    }

    private static @NotNull JMenuItem getApiExplanationSystemEditor() {
        JMenuItem apiExplanationEditor = new JMenuItem("System Editor");
        apiExplanationEditor.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    File pdfFile = new File(Variables.DOCUMENTS_DIRECTORY, "api_explanation.md");
                    Desktop.getDesktop().open(pdfFile);
                } catch (IOException ex) {
                    getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open link (API Explanation)"));
                }
            }
        });
        return apiExplanationEditor;
    }

    private static @NotNull JMenuItem getApiExplanationGithub() {
        JMenuItem apiExplanationGithub = new JMenuItem("Github [Preferred]");
        apiExplanationGithub.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(
                            new URI("https://github.com/frame-dev/MetarAPP/blob/master/documents/api_explanation.md"));
                } catch (IOException | URISyntaxException ex) {
                    getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open link (API Explanation)"));
                }
            }
        });
        return apiExplanationGithub;
    }

    private static @NotNull JMenuItem getErrorExplanationMenuItem() {
        JMenuItem errorExplanationMenuItem = new JMenuItem("Error Explanation");
        errorExplanationMenuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(Variables.DOCUMENTS_DIRECTORY, "error_explanation.txt"));
                } catch (IOException ex) {
                    getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                    loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                    EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open error explanation link"));
                }
            }
        });
        return errorExplanationMenuItem;
    }

    public void addToJson(String icaoCode) {
        try {
            if (!new File(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json").exists()) {
                if (!new File(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json").createNewFile())
                    System.err.println(new File(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json")
                            + " already exists or could not be created!");

                List<String> icaos = new ArrayList<>();
                icaos.add(icaoCode.toUpperCase());
                FileWriter writer = new FileWriter(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(icaos));
                writer.flush();
                writer.close();
            } else {
                List<String> icaos;
                FileReader fileReader = new FileReader(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                Type type = new TypeToken<List<String>>() {
                }.getType();
                icaos = new Gson().fromJson(fileReader, type);
                if (icaos != null) {
                    if (!icaos.contains(icaoCode.toUpperCase()))
                        icaos.add(icaoCode.toUpperCase());
                }
                FileWriter writer = new FileWriter(getFilePath() + "files/" + LoginUtils.userNameStatic + ".json");
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(icaos));
                writer.flush();
                writer.close();
            }
        } catch (Exception ex) {
            getLogger().log(Level.ERROR, "Error while adding to Json " + ErrorCode.ERROR_JSON_SAVE.getError(), ex);
            loggerUtils.addLog(
                    "Error while adding to Json : " + ex.getMessage() + "/" + ErrorCode.ERROR_JSON_SAVE.getError());
                    
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_SAVE, "Error while adding to Json: " + ex.getMessage()));
        }

        if (userData.getIcaos() == null) {
            List<String> icaos = new ArrayList<>();
            icaos.add(icaoCode.toUpperCase());
            userData.setIcaos(icaos);
            userData.save();
        } else if (!userData.getIcaos().contains(icaoCode.toUpperCase())) {
            if (userData.getIcaos().isEmpty())
                userData.setIcaos(new ArrayList<>());
            userData.getIcaos().add(icaoCode.toUpperCase());
            userData.save();
        }
    }

    public void setColorButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(Color.GRAY);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    public void setColorButtonLight(JButton button) {
        button.setForeground(null);
        button.setBackground(null);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(true);
    }

    public void setDarkMode() {
        if ((boolean) settings.get("dark-mode")) {
            panel.setBackground(Color.DARK_GRAY);
            versionLabel.setForeground(Color.WHITE);
            addToSearchCheckBox.setForeground(Color.WHITE);
            addToSearchCheckBox.setBackground(Color.DARK_GRAY);
            updateLabel.setForeground(Color.WHITE);
            dateCreated.setForeground(Color.WHITE);
            flightCategory.setForeground(Color.WHITE);
            hasUpdateLabel.setForeground(Color.WHITE);
            icaoCodeLabel.setForeground(Color.WHITE);
            bearingLabel.setForeground(Color.WHITE);
            ilsLabel.setForeground(Color.WHITE);
            runwayTextField.setBackground(Color.LIGHT_GRAY);
            ICAOCODETextField.setBackground(Color.LIGHT_GRAY);
            darkModeCheckBox.setForeground(Color.WHITE);
            darkModeCheckBox.setBackground(Color.DARK_GRAY);
            windLabel.setForeground(Color.WHITE);
            utcTime.setForeground(Color.WHITE);
            languageSelected.setForeground(Color.WHITE);
            currentUser.setForeground(Color.WHITE);
            currentIcaoLabel.setForeground(Color.WHITE);

            setColorButton(checkButton);
            setColorButton(lastSearchButton);
            setColorButton(showWind);
            setColorButton(showVisibility);
            setColorButton(showCloud);
            setColorButton(showHumidity);
            setColorButton(showQNH);
            setColorButton(showStation);
            setColorButton(showCondition);
            setColorButton(updateButton);
            setColorButton(downloadAsJsonButton);
            setColorButton(searchICAOSButton);
            setColorButton(fullAsJsonButton);
            setColorButton(settingsButton);
            setColorButton(changelogsButton);
            setColorButton(showWeatherMapButton);
            setColorButton(allILSRunwaysButton);
            setColorButton(openSimBriefButton);
            setColorButton(adminGUIButton);
            setColorButton(converterButton);
        }
    }

    public void setLightMode() {
        if (!(boolean) settings.get("dark-mode")) {
            panel.setBackground(null);
            versionLabel.setForeground(null);
            addToSearchCheckBox.setForeground(null);
            addToSearchCheckBox.setBackground(null);
            updateLabel.setForeground(null);
            dateCreated.setForeground(null);
            flightCategory.setForeground(null);
            hasUpdateLabel.setForeground(null);
            icaoCodeLabel.setForeground(null);
            bearingLabel.setForeground(null);
            ilsLabel.setForeground(null);
            runwayTextField.setBackground(null);
            ICAOCODETextField.setBackground(null);
            darkModeCheckBox.setForeground(null);
            darkModeCheckBox.setBackground(null);
            windLabel.setForeground(null);
            languageSelected.setForeground(null);
            utcTime.setForeground(null);
            currentUser.setForeground(null);
            currentIcaoLabel.setForeground(null);

            setColorButtonLight(checkButton);
            setColorButtonLight(lastSearchButton);
            setColorButtonLight(showWind);
            setColorButtonLight(showVisibility);
            setColorButtonLight(showCloud);
            setColorButtonLight(showHumidity);
            setColorButtonLight(showQNH);
            setColorButtonLight(showStation);
            setColorButtonLight(showCondition);
            setColorButtonLight(updateButton);
            setColorButtonLight(downloadAsJsonButton);
            setColorButtonLight(searchICAOSButton);
            setColorButtonLight(fullAsJsonButton);
            setColorButtonLight(settingsButton);
            setColorButtonLight(changelogsButton);
            setColorButtonLight(showWeatherMapButton);
            setColorButtonLight(allILSRunwaysButton);
            setColorButtonLight(openSimBriefButton);
            setColorButtonLight(adminGUIButton);
            setColorButtonLight(converterButton);
        }
    }

    public static void main(String[] args) throws Exception {
        frame = new JFrame("MetarGUI");
        frame.setContentPane(new MetarGUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static class MessageWithLink extends JEditorPane {
        private static final long serialVersionUID = 1L;

        public MessageWithLink(String htmlBody) {
            super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
            addHyperlinkListener(e -> {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    // Process the click event on the link (for example with
                    // java.awt.Desktop.getDesktop().browse())

                    Object[] buttons = new Object[] { "No", "Yes open it" };
                    int clicked = JOptionPane.showOptionDialog(null, "Open Link", "Open Link",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, null);
                    if (clicked == 1) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            getLogger().error("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError(), ex);
                            loggerUtils.addLog("Failed to open link : " + ErrorCode.ERROR_OPEN_LINK.getError());
                            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_OPEN_LINK, "Failed to open link"));
                        }
                    }
                }
            });
            setEditable(false);
            setBorder(null);
        }

        static StringBuffer getStyle() {
            // for copying style
            JLabel label = new JLabel();
            Font font = label.getFont();
            Color color = label.getBackground();

            // create some css from the label's font
            StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
            style.append("font-weight:").append(font.isBold() ? "bold" : "normal").append(";");
            style.append("font-size:").append(font.getSize()).append("pt;");
            style.append("background-color: rgb(").append(color.getRed()).append(",").append(color.getGreen())
                    .append(",").append(color.getBlue()).append(");");
            return style;
        }
    }

    public void setTexts() {
        showWind.setText(localeUtils.getString("showWind"));
        showCloud.setText(localeUtils.getString("showCloud"));
        showCondition.setText(localeUtils.getString("showCondition"));
        showHumidity.setText(localeUtils.getString("showHumidity"));
        showVisibility.setText(localeUtils.getString("showVisibility"));
        showQNH.setText(localeUtils.getString("showQNH"));
        showStation.setText(localeUtils.getString("showStation"));
        showWeatherMapButton.setText(localeUtils.getString("showWeatherMap"));
        fullAsJsonButton.setText(localeUtils.getString("fullAsJson"));
        checkButton.setText(localeUtils.getString("check"));
        lastSearchButton.setText(localeUtils.getString("lastSearch"));
        searchICAOSButton.setText(localeUtils.getString("searchICAOS"));
        changelogsButton.setText(localeUtils.getString("changelogs"));
        openSimBriefButton.setText(localeUtils.getString("openSimBrief"));
        downloadAsJsonButton.setText(localeUtils.getString("downloadAsJson"));
        settingsButton.setText(localeUtils.getString("settingsButton"));
        languageSelected
                .setText(localeUtils.getString("selectedLanguage", "%LOCALE%", localeUtils.getString("language")));
        settingsItem.setText(localeUtils.getString("settingsButton"));
        flightCategory.setText(localeUtils.getString("labelFlightCategory"));
    }

    @SuppressWarnings("unused")
    private void createUIComponents() {
        windImage = new JLabel();
    }

}
