package ch.framedev.metarapp.util;

@SuppressWarnings("unused")
public abstract class Plugin {

    public enum PluginState {
        INITIALIZED, STARTED, STOPPED,
        UPDATING, ERROR
    }

    private PluginState pluginState = PluginState.STOPPED;

    public abstract void initialize();
    public abstract void start();
    public abstract void stop();

    public abstract String getName();
    public abstract String getVersion();
    public abstract String getDescription();
    public abstract String getAuthor();
    public abstract String getWebsite();

    public void setPluginState(PluginState state) {
        this.pluginState = state;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    public abstract String getNewVersion();
    public abstract String getDownloadLink();
}
