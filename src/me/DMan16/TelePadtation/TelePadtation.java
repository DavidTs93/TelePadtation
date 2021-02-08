package me.DMan16.TelePadtation;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TelePadtation extends JavaPlugin {
	private static TelePadtation main;
	private static Logger log = Bukkit.getLogger();
	private static String pluginName = "TelePadtation";
	private static String pluginNameColors = "&bTele&6Pad&btation";
	private final int spigotID = 85390;
	private static ConfigLoader config;
	static HashMap<Player,Menu> MenuManager;
	static TelePadsManager TelePadsManager;
	
	public void onEnable() {
		main = this;
		String versionMC = Bukkit.getServer().getVersion().split("\\(MC:")[1].split("\\)")[0].trim().split(" ")[0].trim();
		if (Integer.parseInt(versionMC.split("\\.")[0]) < 1 || Integer.parseInt(versionMC.split("\\.")[1]) < 16) {
		//if (Double.parseDouble(versionMC.split("\\.",2)[0] + "." + versionMC.split("\\.",2)[1].replace(".","")) < 1.16) {
			Utils.chatColorsLogPlugin("&cunsupported version! Please use version 1.16+.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		try {
			TelePadsManager = new TelePadsManager();
		} catch (IOException e) {
			Utils.chatColorsLogPlugin("&cerror accessing files!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		TelePadsManager.fix();
		config = new ConfigLoader();
		if (config.getUpdateChecker()) {
			new UpdateChecker(spigotID).getVersion(version -> {
				if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
					Utils.chatColorsLogPlugin("Running latest version!");
				} else {
					Utils.chatColorsLogPlugin("New version avilable - v" + version);
				}
			});
		}
		MenuManager = new HashMap<Player,Menu>();
		registerListeners();
		
		Utils.chatColorsLogPlugin("&aLoaded&f, running on version: " + versionMC + ".");
	}
	
	public void onDisable() {
		Bukkit.getServer().getWorlds().forEach(world -> TelePadsManager.write(world));
	}

	private void registerListeners() {
		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(new TelePadListener(),this);
		manager.registerEvents(new MenuListener(),this);
		manager.registerEvents(new Recipes(),this);
	}

	static TelePadtation getMain() {
		return main;
	}

	static Logger getLog() {
		return log;
	}

	static String getPluginName() {
		return pluginName;
	}

	static String getPluginNameColors() {
		return pluginNameColors;
	}

	public static ConfigLoader getConfigLoader() {
		return config;
	}
	
	public static boolean contains(org.bukkit.Location location) {
		return TelePadsManager.get(location) != null;
	}
}