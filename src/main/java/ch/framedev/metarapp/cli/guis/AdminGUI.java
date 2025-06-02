package ch.framedev.metarapp.cli.guis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.metarapp.cli.data.DataIndex;
import ch.framedev.metarapp.cli.data.MySQLData;
import ch.framedev.metarapp.cli.utils.UpdateService;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class AdminGUI extends JFrame {

    JTable table;
    private final ArrayList<MySQLData> mySQLDataArrayList;
    private final int userNameIndex = DataIndex.USERNAME.getIndex();

    public AdminGUI() throws HeadlessException {
        this.mySQLDataArrayList = new ArrayList<>();
        fillList();
        System.out.println(mySQLDataArrayList);
        setTitle("[ Admin Panel ]");
        JPanel panel = new JPanel();
        setSize(640, 640);
        panel.setSize(640, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        table = new JTable(new MyTableModel());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setSize(600, 320);
        fillTable();

        JButton update = getUpdateSelectRowButton();

        JButton deleteUser = getDeleteUserButton();

        JButton resetPassword = getResetPasswordButton();
        JButton restart = getRestartButton();

        JLabel currentDirectory = new JLabel(Main.getFilePath());
        panel.add(currentDirectory);
        panel.add(scrollPane);
        panel.add(update);
        panel.add(deleteUser);
        panel.add(resetPassword);
        panel.add(restart);
        add(panel);
        setVisible(true);
    }

    private JButton getRestartButton() {
        JButton restart = new JButton("Restart");
        restart.addActionListener(listener -> {
            if(Main.TESTING)
                Main.VERSION = "1.0.0";
            UpdateService.downloadLatestVersionAndStart();
        });
        return restart;
    }

    private @NotNull JButton getResetPasswordButton() {
        JButton resetPassword = new JButton("Reset Password");
        resetPassword.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = tableModel.getDataVector().elementAt(table.convertRowIndexToModel(table.getSelectedRow()));
            if(data != null) {
                Main.database.resetPassword((String) data.get(userNameIndex), "password");
                JOptionPane.showMessageDialog(null, "Password has been reset!");
            }
        });
        return resetPassword;
    }

    private @NotNull JButton getDeleteUserButton() {
        JButton deleteUser = new JButton("Delete User");
        deleteUser.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = tableModel.getDataVector().elementAt(table.convertRowIndexToModel(table.getSelectedRow()));
            if(!((String)data.get(userNameIndex)).equalsIgnoreCase("admin")) {
                Main.database.deleteUser((String) data.get(userNameIndex));
                mySQLDataArrayList.clear();
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(null, "Admin User cannot be Removed!");
            }
        });
        return deleteUser;
    }

    private @NotNull JButton getUpdateSelectRowButton() {
        JButton update = new JButton("Update Selected Row");
        update.addActionListener(listener -> {
            MyTableModel tableModel = (MyTableModel) table.getModel();
            Vector<?> data = tableModel.getDataVector().elementAt(table.convertRowIndexToModel(table.getSelectedRow()));
            Main.database.setUsed((String) data.get(userNameIndex), Integer.parseInt((String) data.get(DataIndex.USED.getIndex())));
            Main.database.setMapOpened((String) data.get(userNameIndex), Integer.parseInt((String) data.get(DataIndex.MAP_OPENED.getIndex())));
            Main.database.setFilesDownloaded((String) data.get(userNameIndex), Integer.parseInt((String) data.get(DataIndex.FILES_DOWNLOADED.getIndex())));
            Main.database.setIcaos((String) data.get(userNameIndex),  new Gson().fromJson((String) data.get(DataIndex.ICAOS.getIndex()), new TypeToken<List<String>>(){}.getType()));
        });
        return update;
    }

    public void fillList() {
        for (String user : Main.database.getAllUserNames()) {
            mySQLDataArrayList.add(new MySQLData(user));
        }
    }

    public void fillTable() {
        MyTableModel tableModel = (MyTableModel) table.getModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("UserName");
        tableModel.addColumn("Password");
        tableModel.addColumn("Used");
        tableModel.addColumn("MapOpened");
        tableModel.addColumn("FilesDownloaded");
        tableModel.addColumn("Icaos");
        tableModel.addColumn("LastUsed");
        for (MySQLData mySQLData : mySQLDataArrayList) {
            if (mySQLData != null) {
                Vector<String> strings = getStrings(mySQLData);
                tableModel.addRow(strings);
            }
        }
        table.setModel(tableModel);
    }

    private static @NotNull Vector<String> getStrings(MySQLData mySQLData) {
        Vector<String> strings = new Vector<>();
        strings.add(String.valueOf(mySQLData.getId()));
        strings.add(mySQLData.getUserName());
        strings.add(Arrays.toString(mySQLData.getPassword()));
        strings.add(String.valueOf(mySQLData.getUsed()));
        strings.add(String.valueOf(mySQLData.getMapOpened()));
        strings.add(String.valueOf(mySQLData.getFilesDownloaded()));
        strings.add(mySQLData.getIcaos());
        strings.add(mySQLData.getLastUsed());
        return strings;
    }

    static class MyTableModel extends DefaultTableModel {
        public MyTableModel() {
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    }
}
