package ch.framedev.metarapp.util;

import ch.framedev.metarapp.main.Main;

@SuppressWarnings("unused")
public abstract class Plugin {

    public enum PluginState {
        INITIALIZED, STARTED, STOPPED,
        UPDATING, ERROR
    }

    private PluginState pluginState = PluginState.STOPPED;

    /**
     * Initializes the plugin and performs any necessary setup.
     */
    public abstract void initialize();
    /**
     * Starts the plugin and performs any necessary setup.
     */
    public abstract void start();
    /**
     * Stops the plugin and performs any necessary cleanup.
     */
    public abstract void stop();

    /**
     * Gets the name of the plugin.
     * @return The name as a String.
     */
    public abstract String getName();
    /**
     * Gets the version of the plugin.
     * @return The version as a String.
     */
    public abstract String getVersion();
    /**
     * Gets the description of the plugin.
     * @return The description as a String.
     */
    public abstract String getDescription();
    /**
     * Gets the author of the plugin.
     * @return The author's name as a String.
     */
    public abstract String getAuthor();

    /**
     * Gets the website URL of the plugin.
     * @return The website URL as a String.
     */
    public abstract String getWebsite();

    /**
     * Not for external use.
     * Sets the current state of the plugin.
     * @param state The new PluginState to set.
     */
    public void setPluginState(PluginState state) {
        this.pluginState = state;
    }

    /**
     * Gets the current state of the plugin.
     * @return The current PluginState.
     */
    public PluginState getPluginState() {
        return pluginState;
    }

    /**
     * Gets the new version of the plugin if an update is available.
     * @return The new version string.
     */
    public abstract String getNewVersion();

    /**
     * Gets the download link for the new version of the plugin.
     * @return The download link as a String.
     */
    public abstract String getDownloadLink();

    /**
     * Gets the type of application the plugin is running in.
     * @return The AppType enum value.
     */
    public Main.AppType getAppType() {
        return Main.appType;
    }

    /**
     * Checks if the plugin is supported in the current application type.
     * @return true if supported, false otherwise.
     */
    public boolean isPluginSupported() {
        return true;
    }
}
