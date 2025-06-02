package ch.framedev.metarapp.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import ch.framedev.metarapp.events.CallRefreshEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;

public class CloseListener extends WindowAdapter {

    private String from;
    private String to;

    public CloseListener(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        Main.from = "main";
        EventBus.dispatchRefreshEvent(new CallRefreshEvent(from, to));
    }
}