package me.DMan16.TelePadtation.Classes;

import com.google.common.base.Charsets;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractConfigManager {
	private static final @NotNull String DEFAULT_CONFIG = "config.yml";
	private static final @NotNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	
	private final JavaPlugin plugin;
	private final boolean defaultConfig;
	private final String fileName;
	private final File file;
	private FileConfiguration config;
	
	protected AbstractConfigManager(@NotNull JavaPlugin plugin,@NotNull String fileName) {
		this.plugin = plugin;
		this.defaultConfig = DEFAULT_CONFIG.equalsIgnoreCase(fileName);
		if (this.defaultConfig) fileName = DEFAULT_CONFIG;
		this.fileName = fileName;
		this.file = new File(plugin.getDataFolder(),fileName);
		this.config = null;
		reload();
	}
	
	protected AbstractConfigManager(@NotNull JavaPlugin plugin) {
		this(plugin,DEFAULT_CONFIG);
	}
	
	protected final void reloadConfig() {
		boolean existed = file.exists();
		if (defaultConfig) {
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
		} else {
			if (!existed) plugin.saveResource(fileName,false);
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			Utils.runNotNull(plugin.getResource(fileName),inputStream -> config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream,Charsets.UTF_8))));
			this.config = config;
		}
		if (!existed || plugin.getDescription().getVersion().equalsIgnoreCase(config().getString("version",null))) return;
		File folder = new File(plugin.getDataFolder(),"Backups");
		folder = new File(folder,DATE_FORMAT.format(Calendar.getInstance().getTime()));
		if (!folder.exists() && !folder.mkdirs()) throw new RuntimeException("Couldn't backup create backup directory!");
		File file = new File(folder,fileName + ".bak");
		if (file.exists()) for (long i = 1; i < Long.MAX_VALUE; i++) {
			if ((file = new File(plugin.getDataFolder(),fileName + ".bak" + i)).exists()) file = null;
			else break;
		}
		if (file == null) throw new RuntimeException("Couldn't backup \"" + fileName + "\"!");
		if (!this.file.renameTo(file)) throw new RuntimeException("Couldn't backup \"" + fileName + "\" to \"" + file.getAbsolutePath() + "\"!");
		reloadConfig();
	}
	
	@NotNull
	public final FileConfiguration config() {
		return defaultConfig ? plugin.getConfig() : config;
	}
	
	protected abstract void load();
	
	public final void reload() {
		reloadConfig();
		load();
	}
	
	@Nullable
	@Contract("_,!null,_ -> !null")
	protected String getString(@NotNull String option,@Nullable String defaultValue,boolean applyChatColors) {
		String str = config().getString(option,null);
		str = Utils.isNullOrEmpty(str) ? defaultValue : str;
		return applyChatColors ? Utils.chatColors(str) : str;
	}
	
	@Nullable
	@Contract("_,!null -> !null")
	protected String getString(@NotNull String option,@Nullable String defaultValue) {
		return getString(option,defaultValue,true);
	}
	
	@Nullable
	protected List<@NotNull String> getLines(@NotNull String option,boolean applyChatColors) {
		List<?> list = config().getList(option,null);
		return list == null ? Utils.applyNotNull(getString(option,null,applyChatColors),Collections::singletonList) : (list.isEmpty() ? null : Utils.chatColors(list.stream().map(line -> line == null ? "" : line.toString()).collect(Collectors.toList())));
	}
	
	@Nullable
	protected List<@NotNull String> getLines(@NotNull String option) {
		return getLines(option,true);
	}
	
	@NotNull
	protected List<@NotNull String> addOrSetLine(@Nullable List<@NotNull String> lines,@NotNull String line,boolean caseSensitive) {
		if (Utils.isNullOrEmpty(lines)) return Collections.singletonList(line);
		if (caseSensitive) {
			String lineL = Utils.toLowercase(line);
			if (lines.stream().anyMatch(l -> Utils.toLowercase(l).contains(lineL))) return lines;
		} else if (lines.stream().anyMatch(l -> l.contains(line))) return lines;
		lines.add(0,line);
		return lines;
	}
	
	@NotNull
	protected List<@NotNull String> addOrSetLine(@Nullable List<@NotNull String> lines,@NotNull String line) {
		return addOrSetLine(lines,line,false);
	}
	
	@Nullable
	@Contract("null -> null; !null -> !null")
	protected String join(@Nullable List<@NotNull String> lines) {
		return Utils.applyNotNull(lines,l -> String.join("\n",l));
	}
	
	@Nullable
	@SafeVarargs
	@Contract(value = "null,_ -> null; !null,_ -> new",pure = true)
	protected final String replace(@Nullable String str,@NotNull Pair<@NotNull String,@NotNull Object> @NotNull ... pairs) {
		if (!Utils.isNullOrEmpty(str)) for (Pair<String,Object> pair : pairs) str = str.replaceAll("(?i)" + pair.first(),pair.second().toString());
		return str;
	}
	
	@Nullable
	@SafeVarargs
	@Contract(value = "null,_ -> null; !null,_ -> new",pure = true)
	protected final List<String> replace(@Nullable List<String> list,@NotNull Pair<@NotNull String,@NotNull Object> @NotNull ... pairs) {
		if (list == null) return null;
		return list.stream().map(line -> replace(line,pairs)).filter(Objects::nonNull).collect(Collectors.toList());
	}
}