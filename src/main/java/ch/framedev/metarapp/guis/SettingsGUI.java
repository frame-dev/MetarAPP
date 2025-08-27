package ch.framedev.metarapp.guis;

/*
 * ch.framedev.metarapp.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 04.05.2024 21:21
 */

import ch.framedev.metarapp.database.Database;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.DatabaseChangeEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.*;
import ch.framedev.simplejavautils.SystemUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.log4j.Level;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ch.framedev.metarapp.main.Main.*;

public class SettingsGUI {

    public static JFrame frame;
    private JPanel panel;
    private JCheckBox darkModeCheckBox;
    private JCheckBox popupNewVersionCheckBox;
    private JCheckBox autoLoginCheckBox;
    private JCheckBox autoUpdateRestartCheckBox;
    private JComboBox<String> comboBox1;
    private JButton showAllChangelogsButton;
    private JTextArea infoArea;
    private JLabel languageLabel;
    private JLabel selectedLanguage;
    private JLabel currentUserLabel;
    private JComboBox<String> databaseCombo;
    private JLabel databaseLabel;
    private JCheckBox useNewSearchGUICheckBox;
    private JLabel buildNumber;
    private JButton openLatestLogButton;
    private JComboBox<String> selectBranch;
    private JLabel currentBranch;
    private JLabel platformLabel;
    private JLabel databaseChange;

    public SettingsGUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        EventBus.dispatchRefreshEvent(new CallRefreshEvent(Main.from, "settingsGui"));
        frame.setIconImage(Variables.getLogoImage());

        setup();

        // Set the background and foreground colors based on the dark mode setting
        setGUIDarkMode();
        frame.addWindowListener(new CloseListener("settingsGui", "main"));
    }

    private void setup() {
        currentUserLabel.setText("Current User : " + LoginUtils.userNameStatic);

        showAllChangelogsButton.setText(localeUtils.getString("showAllChangeLogs"));
        popupNewVersionCheckBox.setText(localeUtils.getString("popupNewVersion"));
        autoUpdateRestartCheckBox.setText(localeUtils.getString("autoUpdateRestart"));

        if (localeUtils.getConfiguration().containsKey("defaultSearch")) {
            useNewSearchGUICheckBox.setText(localeUtils.getString("defaultSearch"));
        }

        updateInfoArea();
        darkModeCheckBox.setSelected((Boolean) settings.get("dark-mode"));
        darkModeCheckBox.addActionListener(e -> darkModeAction());
        popupNewVersionCheckBox.setSelected((Boolean) settings.get("popup-new-version"));
        popupNewVersionCheckBox.addActionListener(e -> {
            settings.set("popup-new-version", popupNewVersionCheckBox.isSelected());
            settings.save();
        });
        autoLoginCheckBox.setSelected((Boolean) settings.get("auto-login"));
        autoLoginCheckBox.addActionListener(e -> {
            settings.set("auto-login", autoLoginCheckBox.isSelected());
            settings.save();
        });
        autoUpdateRestartCheckBox.setSelected((Boolean) settings.get("auto-restart-after-update"));
        autoUpdateRestartCheckBox.addActionListener(e -> {
            settings.set("auto-restart-after-update", autoUpdateRestartCheckBox.isSelected());
            settings.save();
        });
        Arrays.stream(Locale.values()).map(Locale::getLocaleString).forEachOrdered(comboBox1::addItem);
        comboBox1.removeItem(Locale.FALLBACK.getLocaleString());
        comboBox1.setSelectedItem(settings.get("language"));
        comboBox1.addActionListener(e -> languageChanger());
        showAllChangelogsButton.addActionListener(e -> new FullJsonGUI(showAllChangelogs(), true));

        if (localeUtils.getLocale() != null)
            selectedLanguage.setText(localeUtils.getString("selectedLanguage", "%LOCALE%", localeUtils.getLocale().getLocaleString()));
        else
            selectedLanguage.setText("Locale not found : " + settings.get("language"));

        databaseCombo.addItem("mysql");
        databaseCombo.addItem("sqlite");
        databaseCombo.addItem("mongodb");
        databaseCombo.addItem("mysql-use-own");
        databaseCombo.setSelectedItem(settings.get("database"));
        databaseLabel.setText("Current Database : " + databaseCombo.getSelectedItem());
        databaseCombo.addActionListener(e -> {
            String databaseType = EventBus.dispatchDatabaseChangeEvent(new DatabaseChangeEvent((String) databaseCombo.getSelectedItem()));
            settings.set("database", databaseType);
            if (Objects.requireNonNull(databaseType).equalsIgnoreCase("mysql-use-own")) {
                settings.set("mysql.own-mysql-database", true);
            } else {
                settings.set("mysql.own-mysql-database", false);
            }
            settings.save();
            databaseLabel.setText("Current Database : " + databaseType);
            database = new Database();
        });

        useNewSearchGUICheckBox.setSelected(settings.getBoolean("defaultSearch"));
        useNewSearchGUICheckBox.addActionListener(e -> {
            settings.set("defaultSearch", useNewSearchGUICheckBox.isSelected());
            settings.save();
        });


        if (branch.equalsIgnoreCase("release")) {
            buildNumber.setText("Build Number : " + BUILD_NUMBER);
        } else {
            buildNumber.setText("Build Number : " + preRelease);
        }

        openLatestLogButton.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(loggerUtils.getLoggerFile());
                } catch (IOException ex) {
                    getLogger().error("Could not open Log File : " + ex.getMessage(), ex);
                    loggerUtils.addLog("Could not open Log File : " + ex.getMessage());
                }
            }
        });

        String currentPlatformString = localeUtils.getString("currentPlatform", "%PLATFORM%", "%PLATFORM%");
        SystemUtils.OSType osType = new SystemUtils().getOSType();
        if (osType == SystemUtils.OSType.WINDOWS) {
            currentPlatformString = currentPlatformString.replace("%PLATFORM%", "Windows");
        } else if (osType == SystemUtils.OSType.MACOS) {
            currentPlatformString = currentPlatformString.replace("%PLATFORM%", "Mac OS");
        } else if (osType == SystemUtils.OSType.LINUX) {
            currentPlatformString = currentPlatformString.replace("%PLATFORM%", "Linux");
        } else {
            currentPlatformString = currentPlatformString.replace("%PLATFORM%", "Unknown");
        }
        platformLabel.setText(currentPlatformString);

        List<String> messages = localeUtils.getStringList("databaseChange");
        if (messages != null && !messages.isEmpty()) {
            String databaseChangeText = "<html>" + String.join("<br>", messages) + "</html>";
            databaseChange.setText(databaseChangeText);
        } else {
            databaseChange.setText("<html>No database changes available.</html>");
        }

        // Branch change
        setupBranch();
    }

    private void setupBranch() {
        // Branch change
        if (TESTING)
            selectBranch.setSelectedItem("pre-release");
        else
            selectBranch.setSelectedItem(settings.getString("branch"));
        selectBranch.addActionListener(e -> {
            if (!TESTING)
                changeBranch();
            else {
                JOptionPane.showMessageDialog(null, "Branch change is not possible in TESTING mode!", "Error", JOptionPane.ERROR_MESSAGE);
                selectBranch.setSelectedItem("pre-release");
            }
        });
    }

    private void setGUIDarkMode() {
        if ((boolean) settings.get("dark-mode")) {
            frame.setBackground(Color.DARK_GRAY);
            darkModeCheckBox.setBackground(Color.LIGHT_GRAY);
            darkModeCheckBox.setForeground(Color.WHITE);
            MetarGUI.instance.setColorButton(showAllChangelogsButton);
            panel.setBackground(Color.DARK_GRAY);
            popupNewVersionCheckBox.setForeground(Color.WHITE);
            popupNewVersionCheckBox.setBackground(Color.LIGHT_GRAY);
            autoLoginCheckBox.setForeground(Color.WHITE);
            autoLoginCheckBox.setBackground(Color.LIGHT_GRAY);
            autoUpdateRestartCheckBox.setForeground(Color.WHITE);
            autoUpdateRestartCheckBox.setBackground(Color.LIGHT_GRAY);
            languageLabel.setBackground(Color.DARK_GRAY);
            languageLabel.setForeground(Color.WHITE);
            selectedLanguage.setBackground(Color.DARK_GRAY);
            selectedLanguage.setForeground(Color.WHITE);
            currentUserLabel.setForeground(Color.WHITE);
            databaseLabel.setBackground(Color.DARK_GRAY);
            databaseLabel.setForeground(Color.WHITE);
            useNewSearchGUICheckBox.setForeground(Color.WHITE);
            useNewSearchGUICheckBox.setBackground(Color.LIGHT_GRAY);
            buildNumber.setForeground(Color.WHITE);
            MetarGUI.instance.setColorButton(openLatestLogButton);
            currentBranch.setBackground(Color.DARK_GRAY);
            currentBranch.setForeground(Color.WHITE);
            platformLabel.setBackground(Color.DARK_GRAY);
            platformLabel.setForeground(Color.WHITE);
            databaseChange.setBackground(Color.DARK_GRAY);
            databaseChange.setForeground(Color.WHITE);
        }
    }

    /**
     * This function handles the branch change process.
     * It checks if the selected branch is different from the current one and prompts the user for a restart.
     * If the selected branch is "release", it downloads the latest version.
     * If the selected branch is "pre-release", it downloads the latest pre-release version.
     * After the download, it updates the branch setting, logs the change, and disables the changelog display.
     */
    private void changeBranch() {
        String branch = (String) selectBranch.getSelectedItem();
        if (branch != null && !branch.equalsIgnoreCase(Main.branch)) {
            JOptionPane.showMessageDialog(null, "for a branch change a restart is required\n New " + CaseUtils.toCamelCase(branch, true) + " will be downloaded", "Require Restart", JOptionPane.INFORMATION_MESSAGE);
            if (branch.equalsIgnoreCase("release"))
                download(getNewVersion());
            else
                download(getLatestPreRelease());
            Main.branch = branch;
            Main.branch = (String) selectBranch.getSelectedItem();
            settings.set("branch", selectBranch.getSelectedItem());
            settings.save();
            loggerUtils.addLog("Branch changed from : " + branch + " to : " + Main.branch + "!");
            settings.set("show-changelogs", false);
            settings.save();
        }
    }

    private void languageChanger() {
        settings.set("language", comboBox1.getSelectedItem());
        settings.save();
        try {
            localeUtils = new LocaleUtils(Locale.fromLocaleString((String) comboBox1.getSelectedItem()));
            MetarGUI.instance.setTexts();
        } catch (LocaleNotFoundException ex) {
            getLogger().log(Level.ERROR, ex.getMessage());
            localeUtils = new LocaleUtils();
            if (Desktop.isDesktopSupported())
                JOptionPane.showMessageDialog(null, "Locale not found using Fallback Language!");
        }
        selectedLanguage.setText(localeUtils.getString("selectedLanguage", "%LOCALE%", localeUtils.getLocale().getLocaleString()));
        updateInfoArea();
        MetarGUI.frame.pack();
    }

    /**
     * This function handles the action when the dark mode checkbox is selected or deselected.
     * It updates the "dark-mode" setting in the application's settings, saves the changes,
     * and then applies the selected dark mode or light mode to the GUI components.
     *
     */
    private void darkModeAction() {
        settings.set("dark-mode", darkModeCheckBox.isSelected());
        settings.save();
        if (darkModeCheckBox.isSelected()) {
            MetarGUI.instance.setDarkMode();
            setDarkLightMode();
        } else {
            MetarGUI.instance.setLightMode();
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("SettingsGUI");
        frame.setContentPane(new SettingsGUI().panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void setDarkLightMode() {
        if ((boolean) settings.get("dark-mode")) {
            frame.setBackground(Color.DARK_GRAY);
            darkModeCheckBox.setBackground(Color.LIGHT_GRAY);
            darkModeCheckBox.setForeground(Color.WHITE);
            MetarGUI.instance.setColorButton(showAllChangelogsButton);
            panel.setBackground(Color.DARK_GRAY);
            popupNewVersionCheckBox.setForeground(Color.WHITE);
            popupNewVersionCheckBox.setBackground(Color.LIGHT_GRAY);
            autoLoginCheckBox.setForeground(Color.WHITE);
            autoLoginCheckBox.setBackground(Color.LIGHT_GRAY);
            autoUpdateRestartCheckBox.setForeground(Color.WHITE);
            autoUpdateRestartCheckBox.setBackground(Color.LIGHT_GRAY);
            languageLabel.setBackground(Color.DARK_GRAY);
            languageLabel.setForeground(Color.WHITE);
            currentUserLabel.setForeground(Color.WHITE);
            currentBranch.setForeground(Color.WHITE);
            MetarGUI.instance.setColorButton(openLatestLogButton);
            platformLabel.setBackground(Color.DARK_GRAY);
            platformLabel.setForeground(Color.WHITE);
            databaseChange.setBackground(Color.DARK_GRAY);
            databaseChange.setForeground(Color.WHITE);
        }
    }

    public void updateInfoArea() {
        List<String> messages = localeUtils.getStringList("settingsInfos");
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : messages) {
            stringBuilder.append(message).append("\n");
        }
        infoArea.setText(stringBuilder.toString());
        infoArea.setFont(new Font("Arial", Font.PLAIN, 18));
    }
}
