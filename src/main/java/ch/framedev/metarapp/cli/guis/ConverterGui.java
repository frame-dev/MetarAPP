package ch.framedev.metarapp.cli.guis;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.cli.utils.LocaleUtils;
import ch.framedev.simplejavautils.TextUtils;
import org.apache.log4j.Level;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a GUI for converting between meters and miles.
 */
public class ConverterGui extends JFrame {

    /**
     * Utility for handling locale-specific operations.
     */
    LocaleUtils localeUtils = Main.getLocaleUtils();

    /**
     * Utility for handling text-related operations.
     */
    private final TextUtils textUtils = new TextUtils();

    /**
     * The main panel of the GUI.
     */
    private final JPanel panel;

    /**
     * Constructs a new ConverterGui object.
     *
     * @throws HeadlessException if the application is attempted to be run in a headless environment
     */
    public ConverterGui() throws HeadlessException {
        setTitle("Converter GUI");
        panel = new JPanel();
        setContentPane(panel);
        JCheckBox metersToMiles = new JCheckBox(localeUtils.getString("metersToMiles"));
        JCheckBox milesToMeters = new JCheckBox(localeUtils.getString("milesToMeters"));
        JTextField inputField = new JTextField(localeUtils.getString("converterGuiInput"));
        JButton submitButton = new JButton("Calculate");
        JTextArea outputField = new JTextArea("Output");
        submitButton.addActionListener(listener -> {
            if (metersToMiles.isSelected()) {
                try {
                    double meters = Double.parseDouble(inputField.getText());
                    outputField.setText("Miles = " + textUtils.metersToMiles(meters));
                } catch (Exception ex) {
                    Main.getLogger().log(Level.ERROR, "Error while converting meters to miles", ex);
                    outputField.setText(localeUtils.getString("errorCalculateConversion", "%Input", inputField.getText()));
                }
            } else if (milesToMeters.isSelected()) {
                try {
                    double miles = Double.parseDouble(inputField.getText());
                    outputField.setText("Meters = " + textUtils.milesToMeters(miles));
                } catch (Exception ex) {
                    Main.getLogger().log(Level.ERROR, "Error while converting miles to meters", ex);
                    outputField.setText(localeUtils.getString("errorCalculateConversion", "%Input", inputField.getText()));
                }
            } else {
                outputField.setText(localeUtils.getString("errorCalculation"));
            }
            pack();
        });
        panel.add(metersToMiles);
        panel.add(milesToMeters);
        panel.add(inputField);
        panel.add(submitButton);
        panel.add(outputField);
        pack();
        setVisible(true);
    }

    /**
     * Gets the main panel of the GUI.
     *
     * @return the main panel of the GUI
     */
    public JPanel getPanel() {
        return panel;
    }
}
