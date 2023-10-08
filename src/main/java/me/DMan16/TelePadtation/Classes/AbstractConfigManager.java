package me.DMan16.TelePadtation.Classes;

import com.google.common.base.Charsets;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	private final String fileName;
	private final File file;
	private FileConfiguration config;
	private FileConfiguration defaults;
	
	protected AbstractConfigManager(@NotNull JavaPlugin plugin,@NotNull String fileName) throws IOException {
		this.plugin = plugin;
		this.fileName = fileName;
		this.file = new File(plugin.getDataFolder(),fileName);
		this.config = null;
		reload();
	}
	
	protected AbstractConfigManager(@NotNull JavaPlugin plugin) throws IOException {
		this(plugin,DEFAULT_CONFIG);
	}
	
	protected final void reloadConfig() throws IOException {
		InputStream resource = plugin.getResource(this.fileName);
		if (resource == null) throw new IOException("The resource \"" + this.fileName + "\" couldn't be found!");
		boolean existed = this.file.exists();
		if (!existed) plugin.saveResource(this.fileName,false);
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.file);
		this.defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(resource,Charsets.UTF_8));
		config.setDefaults(this.defaults);
		this.config = config;
		String version = plugin.getDescription().getVersion();
		if (!existed || version.equalsIgnoreCase(config().getString("version",null))) return;
		File folder = new File(plugin.getDataFolder(),"Backups");
		String date = DATE_FORMAT.format(Calendar.getInstance().getTime());
		folder = new File(folder,date);
		if (!folder.exists() && !folder.mkdirs()) throw new RuntimeException("Couldn't backup create backup directory!");
		File file = new File(folder,this.fileName + ".bak");
		Long i = null;
		if (file.exists()) for (i = 1L; i < Long.MAX_VALUE; i++) {
			if ((file = new File(folder,this.fileName + ".bak" + i)).exists()) file = null;
			else break;
		}
		if (file == null) throw new RuntimeException("Couldn't backup \"" + this.fileName + "\"!");
		if (!this.file.renameTo(file)) throw new RuntimeException("Couldn't backup \"" + this.fileName + "\" to \"" + file.getAbsolutePath() + "\"!");
		config.set("version",version);
		config.save(this.file);
		Utils.chatColorsLogPlugin("&6Configuration file &f\"" + this.fileName + "\"&6 has been updated to &bv" + version + "&6, backup created at &fTelePadtation/Backups/" + date + "/" + this.fileName + ".bak" + (i == null ? "" : i));
	}
	
	@NotNull
	public final FileConfiguration config() {
		return config;
	}
	
	@NotNull
	public final FileConfiguration defaults() {
		return defaults;
	}
	
	protected abstract void load();
	
	public final void reload() throws IOException {
		reloadConfig();
		load();
	}
	
	@Nullable
	@Contract("_,true,_ -> !null")
	protected String getString(@NotNull String option,boolean useDefault,boolean applyChatColors) {
		String def = defaults().getString(option);
		String str = config().getString(option,null);
		str = Utils.isNullOrEmpty(str) ? (useDefault ? def : null) : str;
		return applyChatColors ? Utils.chatColors(str) : str;
	}
	
	@Nullable
	@Contract("_,true -> !null")
	protected String getString(@NotNull String option,boolean useDefault) {
		return getString(option,useDefault,true);
	}
	
	@NotNull
	protected String getString(@NotNull String option) {
		return getString(option,true);
	}
	
	@Nullable
	protected List<@NotNull String> getLines(@NotNull String option,boolean applyChatColors) {
		List<?> list = config().getList(option,null);
		return list == null ? Utils.applyNotNull(getString(option,false,applyChatColors),Collections::singletonList) : (list.isEmpty() ? null : Utils.chatColors(list.stream().map(line -> line == null ? "" : line.toString()).collect(Collectors.toList())));
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
		if (!Utils.isNullOrEmpty(str)) for (Pair<String,Object> pair : pairs) str = str.replaceAll("(?i)" + pair.first(),Objects.toString(pair.second()));
		return str;
	}
	
	@Nullable
	@SafeVarargs
	@Contract(value = "null,_ -> null; !null,_ -> new",pure = true)
	protected final List<String> replace(@Nullable List<String> list,@NotNull Pair<@NotNull String,@NotNull Object> @NotNull ... pairs) {
		return list == null ? null : list.stream().map(line -> replace(line,pairs)).filter(Objects::nonNull).collect(Collectors.toList());
	}
}