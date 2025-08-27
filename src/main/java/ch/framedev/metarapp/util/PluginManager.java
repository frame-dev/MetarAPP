package ch.framedev.metarapp.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import ch.framedev.metarapp.main.Main;

/**
 * TODO: Require Testing
 */
public class PluginManager {
    private static PluginManager instance;
    private static final String PLUGIN_DIRECTORY = Main.utils.getFilePath(Main.class) + "plugins"; // Directory where
                                                                                                   // plugins are stored
    private final Map<String, Plugin> loadedPlugins = new HashMap<>();

    private PluginManager() {
        File pluginDir = new File(PLUGIN_DIRECTORY);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
            System.out.println("Plugin directory created at " + pluginDir.getAbsolutePath());
            return;
        }
    }

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void loadPlugins() {
        File pluginDir = new File(PLUGIN_DIRECTORY);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
            System.out.println("Plugin directory created at " + pluginDir.getAbsolutePath());
            return;
        }

        File[] files = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            System.out.println("No plugin JARs found in " + pluginDir.getAbsolutePath());
            return;
        }

        System.out.println("Plugin directory: " + pluginDir.getAbsolutePath());
        System.out.println("Scanning JARs...");
        for (File jar : files) {
            System.out.println("Found: " + jar.getName());
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() },
                        this.getClass().getClassLoader());
                ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, classLoader);
                boolean found = false;
                for (Plugin plugin : loader) {
                    found = true;
                    System.out.println("Loaded plugin: " + plugin.getName());
                    loadedPlugins.put(plugin.getName(), plugin);
                    plugin.setPluginState(Plugin.PluginState.INITIALIZED);
                }
                if (!found) {
                    System.out.println("No plugins found in JAR: " + jar.getName());
                }
            } catch (Exception e) {
                System.err.println("Failed to load from: " + jar.getName());
                System.err.println("Error: " + e.getMessage() + "| Error Code: " + ErrorCode.ERROR_LOADING_PLUGIN.getErrorCode());
                e.printStackTrace();
            }
        }
    }

    public void loadPlugin(String pluginName) {
        File pluginDir = new File(PLUGIN_DIRECTORY);
        if (!pluginDir.exists()) {
            System.out.println("Plugin directory does not exist: " + pluginDir.getAbsolutePath());
            return;
        }

        File[] files = pluginDir.listFiles((dir, name) -> name.equals(pluginName + ".jar"));
        if (files == null || files.length == 0) {
            System.out.println("Plugin JAR not found: " + pluginName);
            return;
        }

        for (File jar : files) {
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() },
                        this.getClass().getClassLoader());
                ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, classLoader);
                for (Plugin plugin : loader) {
                    System.out.println("Loaded plugin: " + plugin.getName());
                    loadedPlugins.put(plugin.getName(), plugin);
                    plugin.setPluginState(Plugin.PluginState.INITIALIZED);
                }
            } catch (Exception e) {
                System.err.println("Failed to load from: " + jar.getName());
                System.err.println("Error: " + e.getMessage() + "| Error Code: " + ErrorCode.ERROR_LOADING_PLUGIN.getErrorCode());
                e.printStackTrace();
            }
        }
    }

    public void unloadPlugins() {
        System.out.println("Unloading plugins...");
        for (Plugin plugin : loadedPlugins.values()) {
            try {
                plugin.stop();
                System.out.println("Stopped plugin: " + plugin.getName());
            } catch (Exception e) {
                handlePluginError(plugin.getName(), e);
            }
        }
        loadedPlugins.clear();
    }

    public void listPlugins() {
        if (loadedPlugins.isEmpty()) {
            System.out.println("No plugins are currently loaded.");
        } else {
            System.out.println("Loaded plugins:");
            loadedPlugins.keySet().forEach(name -> System.out.println(" - " + name));
        }
    }

    public Plugin getPlugin(String pluginName) {
        return loadedPlugins.get(pluginName);
    }

    public void enablePlugins() {
        System.out.println("Enabling all plugins...");
        for (String pluginName : loadedPlugins.keySet()) {
            enablePlugin(pluginName);
        }
    }

    public void disablePlugins() {
        System.out.println("Disabling all plugins...");
        for (String pluginName : loadedPlugins.keySet()) {
            disablePlugin(pluginName);
        }
    }

    private final Map<String, Boolean> pluginEnabled = new HashMap<>();

    public void enablePlugin(String pluginName) {
        Plugin plugin = loadedPlugins.get(pluginName);
        if (plugin != null && !Boolean.TRUE.equals(pluginEnabled.get(pluginName))) {
            plugin.start();
            plugin.setPluginState(Plugin.PluginState.STARTED);
            pluginEnabled.put(pluginName, true);
            System.out.println("Enabled plugin: " + pluginName);
        }
    }

    public void disablePlugin(String pluginName) {
        Plugin plugin = loadedPlugins.get(pluginName);
        if (plugin != null && Boolean.TRUE.equals(pluginEnabled.get(pluginName))) {
            plugin.stop();
            plugin.setPluginState(Plugin.PluginState.STOPPED);
            pluginEnabled.put(pluginName, false);
            System.out.println("Disabled plugin: " + pluginName);
        }
    }

    public boolean isPluginEnabled(String pluginName) {
        return Boolean.TRUE.equals(pluginEnabled.get(pluginName));
    }

    public void updatePlugin(String pluginName) {
        // Logic to update a specific plugin
        System.out.println("Updating plugin: " + pluginName);
        Plugin plugin = loadedPlugins.get(pluginName);
        String url = plugin.getDownloadLink();
        if (url != null && !url.isEmpty()) {
            try {
                plugin.setPluginState(Plugin.PluginState.UPDATING);
                disablePlugin(pluginName);
                // Simulate downloading the plugin
                System.out.println("Downloading plugin from: " + url);

                // Main.utils.download(url, PLUGIN_DIRECTORY + File.separator + pluginName + ".jar");
                // After downloading, we can reload the plugin
                loadedPlugins.remove(pluginName);
                pluginEnabled.remove(pluginName);
                loadPlugin(pluginName);
                enablePlugin(pluginName);
                System.out.println("Plugin " + pluginName + " updated successfully.");
            } catch (Exception e) {
                handlePluginError(pluginName, e);
            }
        } else {
            System.out.println("No download link available for plugin: " + pluginName);
        }
    }

    public void reloadPlugin(String pluginName) {
        Plugin plugin = loadedPlugins.get(pluginName);
        if (plugin != null) {
            try {
                boolean isEnabled = pluginEnabled.getOrDefault(pluginName, false);
                if (isEnabled) {
                    plugin.stop();
                }
                loadedPlugins.remove(pluginName);
                pluginEnabled.remove(pluginName);
                loadPlugin(pluginName);
                if (isEnabled) {
                    plugin.start();
                }
                System.out.println("Reloaded plugin: " + pluginName);
            } catch (Exception e) {
                handlePluginError(pluginName, e);
            }
        }
    }

    public void reloadAllPlugins() {
        System.out.println("Reloading all plugins...");
        for (String pluginName : new ArrayList<>(loadedPlugins.keySet())) {
            reloadPlugin(pluginName);
        }
    }

    public void handlePluginError(String pluginName, Exception e) {
        // Logic to handle errors related to plugins
        System.err.println("Error in plugin " + pluginName + ": " + e.getMessage());
    }

    public void savePluginState() {
        for (Plugin plugin : loadedPlugins.values()) {
            // Cast to PluginWithState interface or similar
            System.out.println("Saving state for plugin: " + plugin.getName());
        }
    }

    public void restorePluginState() {
        for (Plugin plugin : loadedPlugins.values()) {
            System.out.println("Restoring state for plugin: " + plugin.getName());
        }
    }

    public void initializePlugins() {
        for (Plugin plugin : loadedPlugins.values()) {
            try {
                plugin.initialize();
                System.out.println("Initialized plugin: " + plugin.getName());
            } catch (Exception e) {
                handlePluginError(plugin.getName(), e);
            }
        }
    }

    public List<Plugin> getLoadedPlugins() {
        return new ArrayList<>(loadedPlugins.values());
    }

    public List<Plugin> getEnabledPlugins() {
        List<Plugin> enabledPlugins = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : pluginEnabled.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                Plugin plugin = loadedPlugins.get(entry.getKey());
                if (plugin != null) {
                    enabledPlugins.add(plugin);
                }
            }
        }
        return enabledPlugins;
    }

    /**
     * Checks for plugins that have updates available.
     * @return List of plugins with updates available.
     */
    public List<Plugin> getUpdatePlugins() {
        List<Plugin> updatePlugins = new ArrayList<>();
        for (Plugin plugin : loadedPlugins.values()) {
            if(!plugin.getVersion().equalsIgnoreCase(plugin.getNewVersion())) {
                updatePlugins.add(plugin);
                System.out.println("Plugin " + plugin.getName() + " has an update available: " + plugin.getNewVersion());
            }
        }
        return updatePlugins;
    }

    public boolean hasUpdate(String pluginName) {
        Plugin plugin = loadedPlugins.get(pluginName);
        if (plugin != null) {
            return !plugin.getVersion().equalsIgnoreCase(plugin.getNewVersion());
        }
        return false;
    }

}
