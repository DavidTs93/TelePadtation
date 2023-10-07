package me.DMan16.TelePadtation;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import me.DMan16.TelePadtation.Listeners.RecipesListener;
import me.DMan16.TelePadtation.Listeners.TelePadListener;
import me.DMan16.TelePadtation.Listeners.TelePadtationCommandListener;
import me.DMan16.TelePadtation.Managers.ConfigManager;
import me.DMan16.TelePadtation.Managers.Database.DatabaseConnection;
import me.DMan16.TelePadtation.Managers.Database.LocalDatabase;
import me.DMan16.TelePadtation.Managers.Database.SQLDatabase;
import me.DMan16.TelePadtation.Managers.LanguageManager;
import me.DMan16.TelePadtation.Managers.RecipesManager;
import me.DMan16.TelePadtation.Managers.TelePadsManager;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Scanner;

public class TelePadtationMain extends JavaPlugin {
	public static final String PLUGIN_NAME = "TelePadtation";
	public static final String PLUGIN_NAME_COLORS = "&bTele&6Pad&btation";
	private static final String VERSION_LATEST = "&aRunning latest version!";
	private static final String VERSION_NEWER = "&aNew version available - v&b";
	private static final int METRICS_ID = 19783;
	private static final int SPIGOTMC_ID = 85390;
	private static TelePadtationMain instance;
	
	private TaskChainFactory taskChainFactory;
	private ConfigManager configManager;
	private LanguageManager languageManager;
	private DatabaseConnection databaseConnection;
	private TelePadsManager TelePadsManager;
	private RecipesManager recipesManager;
	
	public void onEnable() {
		if (instance != null) throw new RuntimeException(PLUGIN_NAME + " is already initialized!");
		instance = this;
		if (Utils.VERSION_MAIN < 1 || Utils.VERSION < 16) {
			Utils.chatColorsLogPlugin("&cUnsupported version - please use version 1.16+!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		try {
			if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
				Utils.chatColorsLogPlugin("&cCouldn't create data folder");
				throw new Exception();
			}
			try {
				taskChainFactory = BukkitTaskChainFactory.create(this);
				configManager = new ConfigManager();
				languageManager = new LanguageManager();
				String host = getConfig().getString("database.host",null);
				String table = getConfig().getString("database.table",null);
				if (Utils.isNullOrEmpty(table)) table = "TelePadtation";
				int maxPoolSize = getConfig().getInt("database.SQL.maximum-pool-size",0);
				if (maxPoolSize < 1) maxPoolSize = 20;
				long connectionTimeout = getConfig().getLong("database.SQL.connection-timeout",0);
				if (connectionTimeout < 250) connectionTimeout = 5000;
				DatabaseConnection databaseConnection;
				try {
					String database = getConfig().getString("database.SQL.database",null);
					if (Utils.isNullOrEmpty(host) || host.equalsIgnoreCase("local") || Utils.isNullOrEmpty(database)) throw new SQLException();
					String username = getConfig().getString("database.SQL.username",null);
					if (Utils.isNullOrEmpty(username)) username = null;
					String password = getConfig().getString("database.SQL.password",null);
					if (Utils.isNullOrEmpty(password)) password = null;
					databaseConnection = new SQLDatabase(host,table,maxPoolSize,connectionTimeout,database,getConfig().getInt("database.SQL.port",3306),username,password);
					Utils.chatColorsLogPlugin("&aConnected to&f SQL&a database!");
				} catch (SQLException e) {
					databaseConnection = new LocalDatabase(table,maxPoolSize,connectionTimeout);
					Utils.chatColorsLogPlugin("&aConnected to&f local&a database!");
				}
				this.databaseConnection = databaseConnection;
				if (getConfig().getBoolean("metrics",true)) {
					new Metrics(instance,METRICS_ID);
					Utils.chatColorsLogPlugin("&aHooked to &fMetrics&a provider!");
				}
				if (getConfig().getBoolean("update-checker",true)) try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOTMC_ID).openStream(); Scanner scanner = new Scanner(inputStream)) {
					if (scanner.hasNext()) {
						String version = scanner.next(),current = getDescription().getVersion();
						try {
							String[] versionSplit = version.split("\\."),currentSplit = version.split("\\.");
							int max = Math.min(versionSplit.length,currentSplit.length);
							Boolean newer = null;
							for (int i = 0; i < max; i++) {
								int v = Integer.parseInt(versionSplit[i]),c = Integer.parseInt(currentSplit[i]);
								if (v != c) {
									newer = v > c;
									break;
								}
							}
							if (newer == null) newer = versionSplit.length > currentSplit.length;
							if (newer) Utils.chatColorsLogPlugin(VERSION_NEWER + version);
							else Utils.chatColorsLogPlugin(VERSION_LATEST);
						} catch (Exception e) {
							if (current.equalsIgnoreCase(version)) Utils.chatColorsLogPlugin(VERSION_LATEST);
							else Utils.chatColorsLogPlugin(VERSION_NEWER + version);
						}
					} else Utils.chatColorsLogPlugin(VERSION_LATEST);
				} catch (Exception e) {
					Utils.chatColorsLogPlugin("&cError getting version update!");
				}
				TelePadsManager = new TelePadsManager();
				recipesManager = new RecipesManager();
				new TelePadListener();
				new RecipesListener();
				new TelePadtationCommandListener();
				new BukkitRunnable() {
					public void run() {
						if (Bukkit.getWorlds().isEmpty()) return;
						cancel();
						Bukkit.getWorlds().forEach(databaseConnection()::loadWorld);
					}
				}.runTaskTimer(this,20 * 5,20 * 5);
			} catch (IOException e) {
				Utils.chatColorsLogPlugin("&cError accessing files!");
				throw e;
			} catch (SQLException e) {
				Utils.chatColorsLogPlugin("&cError connecting to database! Error: &e" + e.getMessage());
				throw e;
			}
		} catch (Exception e) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		Utils.chatColorsLogPlugin("&aLoaded successfully! Running version: &b" + Utils.VERSION_STR);
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		databaseConnection().close();
		recipesManager().removeRecipes();
		Utils.chatColorsLogPlugin("&aDisabled successfully!");
	}
	
	@NotNull
	public static TelePadtationMain getInstance() {
		return instance;
	}
	
	@NotNull
	public static TaskChainFactory taskChainFactory() {
		return instance.taskChainFactory;
	}
	
	@NotNull
	public static ConfigManager configManager() {
		return instance.configManager;
	}
	
	@NotNull
	public static LanguageManager languageManager() {
		return instance.languageManager;
	}
	
	@NotNull
	public static DatabaseConnection databaseConnection() {
		return instance.databaseConnection;
	}
	
	@NotNull
	public static TelePadsManager TelePadsManager() {
		return instance.TelePadsManager;
	}
	
	@NotNull
	public static RecipesManager recipesManager() {
		return instance.recipesManager;
	}
}