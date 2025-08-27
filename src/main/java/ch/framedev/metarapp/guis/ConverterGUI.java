package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.main.Main;
import ch.framedev.simplejavautils.Length;
import ch.framedev.simplejavautils.LengthConverter;
import org.apache.log4j.Level;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Represents a GUI for converting between meters and miles.
 */
public class ConverterGUI extends JFrame {

    /**
     * Constructs a new ConverterGui object.
     *
     * @throws HeadlessException if the application is attempted to be run in a headless environment
     */
    public ConverterGUI() throws HeadlessException {
        // Set the title of the GUI window
        setTitle("Converter GUI");
        // Set the default close operation of the GUI window
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Create a new panel to hold the components
        JPanel panel = new JPanel();
        // Set the content pane of the GUI window to the panel
        setContentPane(panel);

        JButton infoButton = new JButton("Info");
        infoButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "You can also use Decimal numbers like 2.6"));

        // Create checkboxes for selecting the conversion direction
        JCheckBox metersToMiles = new JCheckBox("Meters to Miles");
        metersToMiles.setToolTipText("You can also use Decimal numbers like 2.6");
        JCheckBox milesToMeters = new JCheckBox("Miles to Meters");
        milesToMeters.setToolTipText("You can also use Decimal numbers like 2.6");
        // Create a text field for user input
        JTextField inputField = new JTextField("Your Input as Decimal");
        // Create a button for initiating the conversion calculation
        JButton submitButton = new JButton("Calculate");
        // Create a text area for displaying the output
        JTextArea outputField = new JTextArea("Output");

        // Add an action listener to the submit button for handling the conversion logic
        submitButton.addActionListener(listener -> submitConvert(inputField, outputField, metersToMiles, milesToMeters));

        // Add the components to the panel
        panel.add(infoButton);
        panel.add(metersToMiles);
        panel.add(milesToMeters);
        panel.add(inputField);
        panel.add(submitButton);
        panel.add(outputField);

        // Set the background and foreground colors based on the dark mode setting
        if((boolean) Main.settings.get("dark-mode")) {
            setDarkMode(panel, metersToMiles, milesToMeters, outputField, inputField, submitButton);
        }

        // Adjust the size of the GUI window based on its components
        pack();
        // Set the GUI window to be visible
        setVisible(true);
    }

    private void submitConvert(JTextField inputField, JTextArea outputField, JCheckBox metersToMiles, JCheckBox milesToMeters) {
        // Check if the input field is empty or contains the default text
        if (inputField.getText().equalsIgnoreCase("Your Input as Decimal") || inputField.getText().equalsIgnoreCase("") || inputField.getText().isEmpty()) {
            outputField.setText("No Input given");
            pack();
            return;
        }
        // Check if the "Meters to Miles" checkbox is selected
        if (metersToMiles.isSelected()) {
            try {
                double meters;
                // Check if the input contains a comma (for handling decimal input)
                if (inputField.getText().contains(",")) {
                    double kiloMeters = Double.parseDouble(inputField.getText().replace(",", "."));
                    meters = new LengthConverter(Length.KILOMETER, kiloMeters).convertTo(Length.MILE);
                } else if(inputField.getText().contains(".")) {
                    double kiloMeters = Double.parseDouble(inputField.getText());
                    meters = new LengthConverter(Length.KILOMETER, kiloMeters).convertTo(Length.MILE);
                } else {
                    meters = Double.parseDouble(inputField.getText());
                }
                // Display the converted value in miles
                outputField.setText("Miles = " + new LengthConverter(Length.METER, meters).convertTo(Length.MILE));
                pack();
                return;
            } catch (Exception ex) {
                // Log and display an error message if the conversion fails
                Main.getLogger().log(Level.ERROR, "Error while converting meters to miles", ex);
                outputField.setText("Something went wrong while converting meters to miles. Your Input : " + inputField.getText());
                pack();
                return;
            }
        } else if (milesToMeters.isSelected()) {
            // Check if the "Miles to Meters" checkbox is selected
            try {
                double miles;
                if (inputField.getText().contains(","))
                    miles = Double.parseDouble(inputField.getText().replace(",", "."));
                else if(inputField.getText().contains("."))
                    miles = Double.parseDouble(inputField.getText());
                else
                    miles = Double.parseDouble(inputField.getText());
                // Display the converted value in meters
                outputField.setText("Meters = " + new LengthConverter(Length.MILE, miles).convertTo(Length.METER));
                pack();
                return;
            } catch (Exception ex) {
                // Log and display an error message if the conversion fails
                Main.getLogger().log(Level.ERROR, "Error while converting miles to meters", ex);
                outputField.setText("Something went wrong while converting miles to meters. Your Input : " + inputField.getText());
                pack();
                return;
            }
        }
        // Display a message if no conversion direction is selected
        outputField.setText("Select either Meters to Miles or Miles to Meters");
        pack();
    }

    private void setDarkMode(JPanel panel, JCheckBox metersToMiles, JCheckBox milesToMeters, JTextArea outputField, JTextField inputField, JButton submitButton) {
        this.setBackground(Color.DARK_GRAY);
        panel.setBackground(Color.DARK_GRAY);
        metersToMiles.setBackground(Color.DARK_GRAY);
        metersToMiles.setForeground(Color.WHITE);
        milesToMeters.setBackground(Color.DARK_GRAY);
        milesToMeters.setForeground(Color.WHITE);
        outputField.setBackground(Color.DARK_GRAY);
        outputField.setForeground(Color.WHITE);
        outputField.setBorder(new BevelBorder(BevelBorder.LOWERED));
        inputField.setBackground(Color.DARK_GRAY);
        inputField.setBorder(new BevelBorder(BevelBorder.LOWERED));
        inputField.setForeground(Color.WHITE);
        setColorButton(submitButton);
    }

    /**
     * Sets the color of the specified button to a predefined style.
     *
     * @param button the button to set the color for
     */
    public void setColorButton(JButton button) {
        LoginFrame.setColorButton(button);
    }
}
