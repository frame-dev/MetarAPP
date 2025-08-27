package ch.framedev.metarapp.guis;



/*
 * ch.framedev.metarapp.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 27.11.2024 21:24
 */

import ch.framedev.metarapp.data.Remember;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.events.LoginEvent;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.ErrorCode;
import ch.framedev.metarapp.util.LoginUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import static ch.framedev.metarapp.main.Main.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class LoginFrame {

    private static JFrame frame;
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JCheckBox rememberUnsafeCheckBox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton changePasswordButton;
    private JLabel passwordLabel;
    private JLabel usernameLabel;
    private JPanel panel;

    public LoginFrame() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                 Main.from = "login";
            }
        });
        rememberUnsafeCheckBox.setSelected((Boolean) settings.get("remember"));

        getButton();

        rememberUnsafeCheckBox.addActionListener(listener -> {
            Main.settings.set("remember", rememberUnsafeCheckBox.isSelected());
            Main.settings.save();
        });

        if (rememberUnsafeCheckBox.isSelected()) {
            if (new Remember().exists()) {
                usernameTextField.setText(new Remember().getUserName());
                passwordPasswordField.setText(new Remember().getPassword());
            } else {
                usernameTextField.setText("Username?");
                passwordPasswordField.setText("Password?");
            }
        }

        registerButton.addActionListener(listener -> createUser(usernameTextField, passwordPasswordField));

        // Login is Required for the Program
        loginButton.addActionListener(listener -> login(usernameTextField, passwordPasswordField));

        if ((boolean) Main.settings.get("dark-mode")) {
            usernameTextField.setBackground(Color.LIGHT_GRAY);
            panel.setBackground(Color.DARK_GRAY);
            passwordPasswordField.setBackground(Color.LIGHT_GRAY);
            setColorButton(registerButton);
            setColorButton(loginButton);
            rememberUnsafeCheckBox.setForeground(Color.WHITE);
            rememberUnsafeCheckBox.setBackground(Color.LIGHT_GRAY);
            setColorButton(changePasswordButton);
            passwordLabel.setForeground(Color.WHITE);
            usernameLabel.setForeground(Color.WHITE);
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loggerUtils.sendLogsFromIP();
            }
        });
    }

    public void createUser(@NotNull JTextField jTextField, @NotNull JPasswordField jPasswordField) {
        LoginUtils loginHandler = new LoginUtils(jTextField.getText(), String.valueOf(jPasswordField.getPassword()));
        if (jTextField.getText().contains(" ")) {
            JOptionPane.showConfirmDialog(null, "Username cannot contain Spaces!\nPlease try again without Spaces", "Error", JOptionPane.DEFAULT_OPTION);
            return;
        }
        if (loginHandler.create()) {
            JOptionPane.showConfirmDialog(null, "Successfully Registered", "Success", JOptionPane.DEFAULT_OPTION);
        } else {
            JOptionPane.showConfirmDialog(null, "Something went wrong please try again", "Error", JOptionPane.DEFAULT_OPTION);
        }
    }

    public void login(@NotNull JTextField usernameField, @NotNull JPasswordField passwordField) {
        // Validate inputs
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(null, "Username or Password cannot be empty", "Login", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform login asynchronously
        LoginUtils loginHandler = new LoginUtils(username, String.valueOf(password));
        loginHandler.isRight().thenAccept(isAuthenticated -> {
            if (isAuthenticated) {
                MetarGUI.loadUserData();
                onLoginSuccess(username, password);
            } else {
                onLoginFailure();
            }
        }).exceptionally(throwable -> {
            onLoginError(throwable);
            return null;
        });
    }

    private void onLoginSuccess(String username, char[] password) {
        try {
            // Launch MetarGUI
            try {
                Main.from = "login";
                EventBus.dispatchLoginEvent(new LoginEvent(username, (boolean) Main.settings.get("remember"), true));
                new Thread(() -> {
                    try {
                        Thread.sleep(1000); // Wait for 1 second before launching MetarGUI
                        MetarGUI.main(args);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        Main.getLogger().error(e.getMessage(), e);
                    }
                }).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            frame.setVisible(false);

            // Save credentials if "remember" is enabled
            if ((boolean) Main.settings.get("remember")) {
                new Remember(username, String.valueOf(password)).save();
            }

            LoginUtils.active = true;
            MetarGUI.logOut = false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "The API is not Online!", "Login", JOptionPane.ERROR_MESSAGE);
            loggerUtils.addLog("The API is not Online! " + ErrorCode.ERROR_API_DOWN.getError() + " : " + e.getMessage());
            EventBus.dispatchLoginEvent(new LoginEvent(username, (boolean) Main.settings.get("remember"), false));
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_API_DOWN, e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            // Clear password array for security
            Arrays.fill(password, '\0');
        }
    }

    private void onLoginFailure() {
        JOptionPane.showMessageDialog(null, "Username or Password incorrect", "Login", JOptionPane.WARNING_MESSAGE);
        loggerUtils.addLog("Login failed: Incorrect Username or Password");
        EventBus.dispatchLoginEvent(new LoginEvent(usernameTextField.getText(), (boolean) Main.settings.get("remember"), false));
    }

    private void onLoginError(Throwable throwable) {
        JOptionPane.showMessageDialog(null, "An error occurred while logging in. Please try again.", "Login", JOptionPane.ERROR_MESSAGE);
        loggerUtils.addLog("Login error: " + ErrorCode.ERROR_JSON_LOAD.getError() + " : " + throwable.getMessage());
        getLogger().error("Login error", throwable);
        EventBus.dispatchLoginEvent(new LoginEvent(usernameTextField.getText(), (boolean) Main.settings.get("remember"), false));
    }

    public static void main(String[] args) {
        if (!autoLogin()) {
            frame = new JFrame("LoginFrame");
            frame.setContentPane(new LoginFrame().panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }

    private void getButton() {
        changePasswordButton.addActionListener(listener -> changePasswordAction());
    }

    private void changePasswordAction() {
        JOptionPane.showMessageDialog(null, "If the Password was reset by Admin the Default Password is 'password'!", "Password Reset", JOptionPane.PLAIN_MESSAGE);
        JFrame changePW = new JFrame("Change Password");
        JPanel panelCh = new JPanel();
        changePW.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        changePW.setContentPane(panelCh);
        JLabel userNameLabel = new JLabel("Username");
        JTextField userName = new JTextField("Username?");
        JLabel oldPasswordLabel = new JLabel("Old Password");
        JPasswordField oldPassword = new JPasswordField("Old Password");
        JLabel newPasswordLabel = new JLabel("New Password");
        JPasswordField newPassword = new JPasswordField("New Password");
        JButton changeButton = getChangeButton(userName, oldPassword, newPassword);

        panelCh.add(userNameLabel);
        panelCh.add(userName);
        panelCh.add(oldPasswordLabel);
        panelCh.add(oldPassword);
        panelCh.add(newPasswordLabel);
        panelCh.add(newPassword);
        panelCh.add(changeButton);
        changePW.pack();

        if ((boolean) Main.settings.get("dark-mode")) {
            setDarkMode(userNameLabel, userName, panelCh, oldPasswordLabel, oldPassword, newPasswordLabel, newPassword, changeButton);
        }

        changePW.setVisible(true);
    }

    private @NotNull JButton getChangeButton(JTextField userName, JPasswordField oldPassword, JPasswordField newPassword) {
        JButton changeButton = new JButton("Change Password");

        changeButton.addActionListener(list -> changePassword(userName, oldPassword, newPassword));
        return changeButton;
    }

    private void setDarkMode(@NotNull JLabel userNameLabel, @NotNull JTextField userName, @NotNull JPanel panelCh, @NotNull JLabel oldPasswordLabel, @NotNull JPasswordField oldPassword, @NotNull JLabel newPasswordLabel, @NotNull JPasswordField newPassword, JButton changeButton) {
        userNameLabel.setForeground(Color.WHITE);
        userName.setBackground(Color.LIGHT_GRAY);
        panelCh.setBackground(Color.DARK_GRAY);
        oldPasswordLabel.setForeground(Color.WHITE);
        oldPassword.setBackground(Color.LIGHT_GRAY);
        newPasswordLabel.setForeground(Color.WHITE);
        newPassword.setBackground(Color.LIGHT_GRAY);
        setColorButton(changeButton);
    }

    public static void setColorButton(@NotNull JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(Color.GRAY);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    private static boolean autoLogin() {
        if ((boolean) Main.settings.get("auto-login") && !MetarGUI.logOut) {
            if ((boolean) Main.settings.get("remember")) {
                if (new Remember().exists()) {
                    database.isUserRight(new Remember().getUserName(), new Remember().getPassword()).thenAccept(aBoolean -> {
                        if (aBoolean) {
                            LoginUtils.userNameStatic = new Remember().getUserName();
                            MetarGUI.loadUserData();
                            try {
                                MetarGUI.main(Main.args);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            if (frame != null)
                                frame.setVisible(false);
                        }
                    }).exceptionally(throwable -> {
                        loggerUtils.addLog("Error : " + throwable.getMessage());
                        getLogger().error("Error : " + throwable.getMessage(), throwable);
                        return null;
                    });
                    LoginUtils.active = true;
                    return true;
                }
            }
        }
        return false;
    }

    public void changePassword(@NotNull JTextField userName, @NotNull JPasswordField oldPassword, JPasswordField newPassword) {
        new LoginUtils(userName.getText(), String.valueOf(oldPassword.getPassword())).isRight().thenAccept(aBoolean -> {
            if (aBoolean) {
                if (new LoginUtils().changePassword(userName.getText(), String.valueOf(oldPassword.getPassword()), String.valueOf(newPassword.getPassword())))
                    JOptionPane.showConfirmDialog(null, "Successfully");
            }
        }).exceptionally(throwable -> {
            JOptionPane.showMessageDialog(null, "Username or Old Password incorrect");
            loggerUtils.addLog("Username or Old Password incorrect : " + ErrorCode.ERROR_JSON_LOAD.getError() + " : " + throwable.getMessage());
            getLogger().error("Username or Old Password incorrect", throwable);
            EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_JSON_LOAD, throwable.getMessage()));
            return null;
        });
    }
}
