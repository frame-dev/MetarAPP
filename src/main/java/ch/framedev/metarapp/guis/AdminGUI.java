package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.data.DataEnum;
import ch.framedev.metarapp.data.MySQLData;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.Variables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

import static ch.framedev.metarapp.main.Main.getLogger;

public class AdminGUI extends JFrame {

    JTable table;
    JTable utilitiesTable;
    private final List<MySQLData> mySQLDataList;

    public AdminGUI() throws HeadlessException {
        setIconImage(Variables.getLogoImage());

        this.mySQLDataList = new ArrayList<>();

        setTitle("[ « Admin Panel » ]");
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setSize(640, 1100);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Main table
        table = new JTable(new MyTableModel());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Utilities table
        utilitiesTable = new JTable(new MyTableModel());
        utilitiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane utilitiesTableScrollPane = new JScrollPane(utilitiesTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Buttons
        JButton update = getUpdateSelectRowButton();
        JButton deleteUser = getDeleteUserButton();
        JButton resetPassword = getResetPasswordButton();
        JButton restart = getRestartButton();

        // Labels
        JLabel currentDirectory = new JLabel("Current Directory: " + Main.getFilePath());
        JLabel logDirectory = new JLabel("Log Directory: " + Main.loggerUtils.getLoggerFile().getParentFile().getAbsolutePath());

        // Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(currentDirectory, gbc);

        gbc.gridy++;
        add(logDirectory, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        add(scrollPane, gbc);

        gbc.gridy++;
        add(utilitiesTableScrollPane, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        add(update, gbc);

        gbc.gridy++;
        add(deleteUser, gbc);

        gbc.gridy++;
        add(resetPassword, gbc);

        gbc.gridy++;
        add(restart, gbc);

        table.setFillsViewportHeight(true);

        fillList();
        System.out.println("List filled");
        fillTable();
        initializeUtilitiesTable();
        fillUtilitiesTable();

        JButton testDownload = new JButton("Test Download");
        testDownload.addActionListener(e -> Main.download(Main.getLatestPreRelease()));

        gbc.gridy++;
        add(testDownload, gbc);


        setVisible(true);
    }

    private void initializeUtilitiesTable() {
        MyTableModel tableModel = new MyTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("UserName");
        tableModel.addColumn("Online");
        tableModel.addColumn("Version");
        tableModel.addColumn("HasUpdate");
        tableModel.addColumn("LastUpdated");
        utilitiesTable.setModel(tableModel);
    }


    private void fillUtilitiesTable() {
        showProgressBar(); // Show progress bar before loading starts

        // Create a SwingWorker to manage asynchronous data loading
        SwingWorker<Void, Vector<String>> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // Iterate through all MySQLData and process asynchronously
                for (MySQLData mySQLData : mySQLDataList) {
                    // Publish rows one at a time as they complete
                    getStringsUtilitiesAsync(mySQLData).thenAccept(this::publish).exceptionally(ex -> {
                        Main.getLogger().error(ex.getMessage(), ex);
                        return null;
                    });
                }
                return null;
            }

            @Override
            protected void process(List<Vector<String>> chunks) {
                MyTableModel tableModel = (MyTableModel) utilitiesTable.getModel();
                for (Vector<String> row : chunks) {
                    System.out.println("Processing row: " + row);
                    tableModel.addRow(row);
                }
            }

            @Override
            protected void done() {
                hideProgressBar(); // Hide progress bar after loading finishes
            }
        };

        worker.execute(); // Start the SwingWorker
    }

    private JButton getRestartButton() {
        JButton restart = new JButton("Restart");
        restart.addActionListener(listener -> Main.deleteOldVersionAndStartNew());
        return restart;
    }

    private @NotNull JButton getResetPasswordButton() {
        JButton resetPassword = new JButton("Reset Password");
        resetPassword.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = (Vector<?>) tableModel.getDataVector().elementAt(table.getSelectedRow());
            if (data != null) {
                String userName = (String) data.get(DataEnum.USERNAME.getIndex());
                MySQLData mySQLData = findUserData(userName);
                if (mySQLData != null) {
                    Main.database.resetPassword(userName, "password").thenAccept(result -> {
                        if (result) {
                            JOptionPane.showMessageDialog(this, "Password has been reset for " + userName + "!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to reset password!");
                        }
                    }).exceptionally(throwable -> {
                        getLogger().error("Failed to reset password for user: " + userName, throwable);
                        JOptionPane.showMessageDialog(this, "Failed to reset password!");
                        return null;
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "User not found!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No user selected!");
            }
        });
        return resetPassword;
    }

    private @NotNull JButton getDeleteUserButton() {
        JButton deleteUser = new JButton("Delete User");
        deleteUser.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = (Vector<?>) tableModel.getDataVector().elementAt(table.convertRowIndexToModel(table.getSelectedRow()));
            String userName = (String) data.get(DataEnum.USERNAME.getIndex());

            if (!"admin".equalsIgnoreCase(userName)) {
                MySQLData mySQLData = findUserData(userName);
                if (mySQLData != null) {
                    Main.database.deleteUser(userName);
                    mySQLDataList.remove(mySQLData);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Admin User cannot be removed!");
            }
        });
        return deleteUser;
    }

    private @NotNull JButton getUpdateSelectRowButton() {
        JButton update = new JButton("Update Selected Row");
        update.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = (Vector<?>) tableModel.getDataVector().elementAt(table.convertRowIndexToModel(table.getSelectedRow()));
            String userName = (String) data.get(DataEnum.USERNAME.getIndex());
            java.lang.reflect.Type type = new TypeToken<List<String>>() {
            }.getType();

            MySQLData mySQLData = findUserData(userName);
            if (mySQLData != null) {
                mySQLData.setUsed(Integer.parseInt((String) data.get(DataEnum.USED.getIndex())));
                mySQLData.setMapOpened(Integer.parseInt((String) data.get(DataEnum.MAP_OPENED.getIndex())));
                mySQLData.setFilesDownloaded(Integer.parseInt((String) data.get(DataEnum.FILES_DOWNLOADED.getIndex())));
                mySQLData.setIcaos(new Gson().fromJson((String) data.get(DataEnum.ICAOS.getIndex()), type));
                mySQLData.save();
            }
        });
        return update;
    }

    private void fillList() {
        // Fetch all user data at once
        List<MySQLData> users = Main.database.getAllUserData(); // Implement a batch-fetch method
        mySQLDataList.addAll(users);
    }

    private void fillTable() {
        MyTableModel tableModel = (MyTableModel) table.getModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("UserName");
        tableModel.addColumn("Password");
        tableModel.addColumn("Used");
        tableModel.addColumn("MapOpened");
        tableModel.addColumn("FilesDownloaded");
        tableModel.addColumn("Icaos");
        tableModel.addColumn("LastUsed");

        // Populate the table asynchronously
        populateTableAsync();
    }

    private void populateTableAsync() {
        SwingWorker<Void, Vector<String>> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<CompletableFuture<MySQLData>> initializationFutures = new ArrayList<>();
                for (MySQLData mySQLData : mySQLDataList) {
                    initializationFutures.add(mySQLData.initializeUserAsync(mySQLData.getUserName()));
                }

                CompletableFuture.allOf(initializationFutures.toArray(new CompletableFuture[0])).join(); // Wait for all to complete

                for (MySQLData mySQLData : mySQLDataList) {
                    Vector<String> strings = new Vector<>();
                    strings.add(String.valueOf(mySQLData.getId()));
                    strings.add(mySQLData.getUserName());
                    strings.add(Arrays.toString(mySQLData.getPassword()));
                    strings.add(String.valueOf(mySQLData.getUsed()));
                    strings.add(String.valueOf(mySQLData.getMapOpened()));
                    strings.add(String.valueOf(mySQLData.getFilesDownloaded()));
                    strings.add(new Gson().toJson(mySQLData.getIcaos() != null ? mySQLData.getIcaos() : "Not Set"));
                    strings.add(mySQLData.getLastUsed());

                    publish(strings);
                }
                return null;
            }

            @Override
            protected void process(List<Vector<String>> chunks) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (Vector<String> rowData : chunks) {
                    model.addRow(rowData);
                }
            }

            @Override
            protected void done() {
                hideProgressBar();
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        showProgressBar();
        worker.execute();
    }

    @SuppressWarnings("unused")
    private static @NotNull Vector<String> getStringsUtilities(MySQLData mySQLData) {
        Vector<String> strings = new Vector<>();
        strings.add(String.valueOf(mySQLData.getId()));
        strings.add(mySQLData.getUserName());
        strings.add(String.valueOf(Main.database.isOnline(mySQLData.getUserName()).join()));
        strings.add(Main.database.getVersion(mySQLData.getUserName()).join());
        strings.add(String.valueOf(Main.database.hasUpdate(mySQLData.getUserName()).join()));
        strings.add(Main.database.getLastUpdated(mySQLData.getUserName()).join());
        return strings;
    }

    private MySQLData findUserData(String userName) {
        return mySQLDataList.stream()
                .filter(data -> data.getUserName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    private void refreshTable() {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
        fillTable();
    }

    static class MyTableModel extends DefaultTableModel {
        @Override
        public Class<?> getColumnClass(int column) {
            return String.class;
        }
    }

    private JProgressBar progressBar;

    private void showProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Loading data...");
            progressBar.setStringPainted(true);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER; // Span across entire row
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            add(progressBar, gbc);
            revalidate(); // Recompute layout
            repaint();    // Redraw
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            remove(progressBar);
            progressBar = null;
            revalidate(); // Recompute layout
            repaint();    // Redraw
        }
    }

    private CompletableFuture<Vector<String>> getStringsUtilitiesAsync(MySQLData mySQLData) {
        Vector<String> strings = new Vector<>();
        strings.add(String.valueOf(mySQLData.getId()));
        strings.add(mySQLData.getUserName());

        // Fetch data concurrently
        CompletableFuture<Boolean> onlineFuture = Main.database.isOnline(mySQLData.getUserName())
                .exceptionally(ex -> {
                    Main.getLogger().error(ex.getMessage(), ex);
                    return false; // Default value in case of failure
                });

        CompletableFuture<String> versionFuture = Main.database.getVersion(mySQLData.getUserName())
                .exceptionally(ex -> {
                    Main.getLogger().error(ex.getMessage(), ex);
                    return "Unknown"; // Default value in case of failure
                });

        CompletableFuture<Boolean> hasUpdateFuture = Main.database.hasUpdate(mySQLData.getUserName())
                .exceptionally(ex -> {
                    Main.getLogger().error(ex.getMessage(), ex);
                    return false; // Default value in case of failure
                });

        CompletableFuture<String> lastUpdatedFuture = Main.database.getLastUpdated(mySQLData.getUserName())
                .exceptionally(ex -> {
                    Main.getLogger().error(ex.getMessage(), ex);
                    return "N/A"; // Default value in case of failure
                });

        // Combine futures
        return CompletableFuture.allOf(onlineFuture, versionFuture, hasUpdateFuture, lastUpdatedFuture)
                .thenApply(v -> {
                    try {
                        strings.add(String.valueOf(onlineFuture.get()));
                        strings.add(versionFuture.get());
                        strings.add(String.valueOf(hasUpdateFuture.get()));
                        strings.add(lastUpdatedFuture.get());
                        System.out.println(strings);
                    } catch (Exception e) {
                        Main.getLogger().error(e.getMessage(), e);
                    }
                    return strings;
                });
    }
}
