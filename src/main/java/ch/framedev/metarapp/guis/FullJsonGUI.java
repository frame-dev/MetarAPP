package ch.framedev.metarapp.guis;

import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.util.CloseListener;
import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.EventBus;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FullJsonGUI extends JFrame {

    private static final long serialVersionUID = 2176920101168524413L;
	private final JPanel panel;

    public FullJsonGUI() {
        EventBus.dispatchRefreshEvent(new CallRefreshEvent(Main.from, "fullJsonGui"));
        this.addWindowListener(new CloseListener("fullJsonGui", "main"));
        setTitle("FullJsonGui");
        panel = new JPanel();
        setSize(340, 650);
        panel.setSize(340, 650);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea jTextArea;
        try {
            jTextArea = new JTextArea(MetarAPPApi.getInstance().getMetarData(MetarGUI.metarRequest.getIcao()).toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not get the full MetarData from MetarRequest", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        jTextArea.setSize(320, 640);
        JScrollPane scroll = new JScrollPane (jTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        if((boolean) Main.settings.get("dark-mode")) {
            scroll.setBackground(Color.DARK_GRAY);
            panel.setBackground(Color.DARK_GRAY);
            jTextArea.setBackground(Color.DARK_GRAY);
            jTextArea.setForeground(Color.WHITE);
        }
        add(scroll);
        setVisible(true);
    }

    public FullJsonGUI(String data) {
        setTitle("FullJsonGui");
        panel = new JPanel();
        setSize(340, 650);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea jTextArea = new JTextArea(data);
        jTextArea.setSize(320, 640);
        JScrollPane scroll = new JScrollPane (jTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        if((boolean) Main.settings.get("dark-mode")) {
            scroll.setBackground(Color.DARK_GRAY);
            panel.setBackground(Color.DARK_GRAY);
            jTextArea.setBackground(Color.DARK_GRAY);
            jTextArea.setForeground(Color.WHITE);
            panel.setForeground(Color.WHITE);
        }
        add(scroll);
        setVisible(true);
    }

    public FullJsonGUI(String data, boolean open) {
        setTitle("FullJsonGui");
        panel = new JPanel();
        setSize(340, 650);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea jTextArea = new JTextArea(data);
        jTextArea.setSize(320, 640);
        JScrollPane scroll = new JScrollPane (jTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menu = getjMenu(jTextArea);
        menuBar.add(menu);

        if((boolean) Main.settings.get("dark-mode")) {
            setDarkMode(scroll, jTextArea, menuBar);
        }
        add(scroll);
        setVisible(true);
    }

    private void setDarkMode(JScrollPane scroll, JTextArea jTextArea, JMenuBar menuBar) {
        scroll.setBackground(Color.DARK_GRAY);
        panel.setBackground(Color.DARK_GRAY);
        jTextArea.setBackground(Color.DARK_GRAY);
        jTextArea.setForeground(Color.WHITE);

        menuBar.setForeground(Color.BLACK);
        menuBar.setBackground(Color.DARK_GRAY);
        for (Component component : menuBar.getComponents()) {
            component.setBackground(Color.DARK_GRAY);
            component.setForeground(Color.WHITE);
            for (Component subComponent : ((JMenu) component).getComponents()) {
                subComponent.setBackground(Color.DARK_GRAY);
                subComponent.setForeground(Color.WHITE);
            }
        }
    }

    private static @NotNull JMenu getjMenu(JTextArea jTextArea) {
        JMenu menu = new JMenu("Open");
        JMenuItem item = new JMenuItem("Open File on Default Text Editor");
        item.addActionListener(listener ->{
            try {
                File file = File.createTempFile("metarapp",".txt");
                file.deleteOnExit();
                FileWriter writer = new FileWriter(file);
                writer.write(jTextArea.getText());
                writer.flush();
                writer.close();
                Desktop.getDesktop().edit(file);
                if(!file.setWritable(false)) {
                    System.err.println("Cannot set write mode to false");
                }
            } catch (Exception ex) {
                Main.getLogger().error("An error occurred while editing the file", ex);
            }
        });
        menu.add(item);
        return menu;
    }
}
