package me.DMan16.TelePadtation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

@SuppressWarnings("rawtypes")
public class ConfigLoader {
	private final String header;
	private boolean updateChecker;
	private int baseLimit;
	private int permLimit;
	private List<String> perms;
	
	private ArrayList<ConfigOption<?>> configOptionsList;

	public ConfigLoader() {
		configOptionsList = new ArrayList<ConfigOption<?>>();
		header = TelePadtation.getPluginName() + " config file";
		makeConfig();
	}

	private void makeConfig() {
		String[] updateCheckerComment = {"Check if a new update was released (true/false)"};
		String[] baseLimitComment = {"base-limit - Base limit for amount of TelePads per player (Integer, minimum: 0)"};
		String[] permLimitComment = {"perm-limit - Extra amount of TelePads per permission from the list below (Integer, Integer, minimum: 1)"};
		String[] permsComment = {"permissions - List of permissions that add extra amount of TelePads per permission"};
		
		FileConfiguration config = TelePadtation.getMain().getConfig();
		
		updateChecker = ((Boolean) addNewConfigOption(config,"update-checker",Boolean.valueOf(true),updateCheckerComment)).booleanValue();
		baseLimit = Math.max(((int) addNewConfigOption(config,"base-limit",Integer.valueOf(2),baseLimitComment)),0);
		permLimit = Math.max(((int) addNewConfigOption(config,"perm-limit",Integer.valueOf(1),permLimitComment)),1);
		perms = ((List<String>) addNewConfigOption(config,"permissions",Arrays.asList("",""),permsComment));
		
		for (int i = 0; i < perms.size(); i++) if (perms.get(i) == null) perms.set(i,"");
		
		writeConfig();
	}

	private <T> T addNewConfigOption(FileConfiguration config, String optionName, T defaultValue, String[] comment) {
		ConfigOption<T> option = new ConfigOption<T>(config,optionName,defaultValue,comment);

		configOptionsList.add(option);
		return (T) option.getValue();
	}

	private void writeConfig() {
		try {
			File dataFolder = TelePadtation.getMain().getDataFolder();
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}
			File saveTo = new File(TelePadtation.getMain().getDataFolder(),"config.yml");
			if (!saveTo.exists()) {
				saveTo.createNewFile();
			} else {
				saveTo.delete();
				saveTo.createNewFile();
			}
			FileWriter fw = new FileWriter(saveTo,true);
			PrintWriter pw = new PrintWriter(fw);
			if (header != null) {
				pw.print("## " + header + "\n");
			}
			for (int i = 0; i < configOptionsList.size(); i++) {
				pw.print("\n" + ((ConfigOption) configOptionsList.get(i)).toString()
						+ ((i == configOptionsList.size() - 1) || ((i < configOptionsList.size() - 1)
								&& (((ConfigOption) configOptionsList.get(i + 1)).getComment() == null)) ? "" : "\n"));
			}
			pw.flush();
			pw.close();
		} catch (IOException e) {
			TelePadtation.getLog().log(Level.SEVERE,Utils.chatColorsPlugin("Could not save config.yml! "
					+ "Please contact an admin for the following error: "));
			e.printStackTrace();
		}
	}

	public boolean getUpdateChecker() {
		return updateChecker;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public int getPermLimit() {
		return permLimit;
	}

	public List<String> getPerms() {
		List<String> perms = new ArrayList<String>();
		for (String perm : this.perms) if (perm != null && !perm.trim().isEmpty()) perms.add(perm.trim());
		return perms;
	}
}