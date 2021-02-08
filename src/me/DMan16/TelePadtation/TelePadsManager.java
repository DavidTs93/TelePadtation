package me.DMan16.TelePadtation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TelePadsManager {
	private HashMap<Location,TelePad> TelePads;
	private HashMap<String,List<Location>> TelePadsPlayer;
	public List<Location> TelePadsGlobal;
	private HashMap<Location,String> TelePadsGlobalNames;
	public HashMap<String,TelePad> TelePadsSingleUse;
	private final String pluginDir = "plugins/" + TelePadtation.getPluginName();
	private final Path dir = Paths.get(pluginDir);
	
	TelePadsManager() throws IOException {
		TelePads = new HashMap<Location,TelePad>();
		TelePadsPlayer = new HashMap<String,List<Location>>();
		TelePadsGlobal = new ArrayList<Location>();
		TelePadsSingleUse = new HashMap<String,TelePad>();
		TelePadsGlobalNames = new HashMap<Location,String>();
		if (!Files.exists(dir, new LinkOption[0])) {
			Files.createDirectories(dir, new FileAttribute[0]);
		} else {
			final File dir = new File(pluginDir);
			JSONParser jsonParser = new JSONParser();
			for (final File path : dir.listFiles()) {
				World world = null;
				for (World world1 : Bukkit.getWorlds()) if (world1.getName().equals(path.getName())) world = world1;
				if (world == null) continue;
				if (path.isDirectory()) {
					List<File> remove = new ArrayList<File>();
					for (final File file : path.listFiles()) {
						try (InputStreamReader reader = new InputStreamReader(new FileInputStream(pluginDir + "/" + path.getName() + "/" + file.getName()),"UTF-8")) {
							List<Location> playerTelePads = new ArrayList<Location>();
							Object obj = jsonParser.parse(reader);
							JSONArray arr = (JSONArray) obj;
							for (Object padInfo : arr) {
								try {
									int x = Integer.parseInt(((JSONObject) padInfo).get("x").toString());
									int y = Integer.parseInt(((JSONObject) padInfo).get("y").toString());
									int z = Integer.parseInt(((JSONObject) padInfo).get("z").toString());
									int limit = Integer.parseInt(((JSONObject) padInfo).get("limit").toString());
									int used = Integer.parseInt(((JSONObject) padInfo).get("used").toString());
									int extra = Integer.parseInt(((JSONObject) padInfo).get("extra").toString());
									boolean global = Boolean.parseBoolean(((JSONObject) padInfo).get("global").toString());
									Location location = new Location(path.getName(),x,y,z);
									TelePads.put(location, new TelePad(file.getName().replace(".json",""),limit,used,extra,global));
									playerTelePads.add(location);
									if (global) {
										String name = null;
										try {
											name = ((JSONObject) padInfo).get("name").toString();
											if (Utils.chatColorsStrip(Utils.chatColors(name.trim().replace(" ",""))).isEmpty()) name = null;
										} catch (Exception e) {}
										TelePadsGlobal.add(location);
										TelePadsGlobalNames.put(location,name);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (playerTelePads.isEmpty()) remove.add(file);
							else TelePadsPlayer.put(file.getName().replace(".json",""),playerTelePads);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					for (File file : remove) file.delete();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void write(World world) {
		String worldName = world.getName();
		Path path = dir.resolve(worldName);
		try {
			if (!Files.exists(path, new LinkOption[0])) {
				Files.createDirectories(path, new FileAttribute[0]);
			}
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			JsonParser jp = new JsonParser();
			List<Location> remove = new ArrayList<Location>();
			for (Entry<String,List<Location>> playerTelePads : TelePadsPlayer.entrySet()) {
				try {
					JSONArray arr = new JSONArray();
					for (Location location : playerTelePads.getValue()) {
						if (location.world.equals(worldName)) {
							try {
								if (world.getBlockAt(location.x,location.y,location.z).getType() == Material.END_PORTAL_FRAME) {
									TelePad telePad = TelePads.get(location);
									JSONObject entry = new JSONObject();
									entry.put("x",location.x);
									entry.put("y",location.y);
									entry.put("z",location.z);
									entry.put("limit",telePad.limit());
									entry.put("used",telePad.used());
									entry.put("extra",telePad.extra());
									entry.put("global",telePad.global());
									if (TelePadsGlobalNames.containsKey(location)) {
										String name = TelePadsGlobalNames.get(location);
										entry.put("name",name == null ? "" : name);
									}
									arr.add(entry);
								} else {
									remove.add(location);
								}
							} catch (Exception e) {}
						}
					}
					Path playerPath = path.resolve(playerTelePads.getKey() + ".json");
					Files.deleteIfExists(playerPath);
					if (!arr.isEmpty()) {
						Files.createFile(playerPath, new FileAttribute[0]);
						JsonElement je = jp.parse(arr.toJSONString());
						String prettyJsonString = gson.toJson(je);
						PrintWriter pw = new PrintWriter(pluginDir + "/" + worldName + "/" + playerTelePads.getKey() + ".json");
						pw.write(prettyJsonString);
						pw.flush();
						pw.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			remove.forEach(loc -> remove(loc));
		} catch (Exception e) {
			Utils.chatColorsLogPlugin("&cError saving &bTele&6Pad&bs &cin world &e" + world.getName() + "&c!");
		}
		if (Bukkit.getWorlds().get(Bukkit.getWorlds().size() - 1).getName().equals(worldName))
			Utils.chatColorsLogPlugin("&aAll &bTele&6Pad&bs &ahave been saved!");
	}
	
	void remove(org.bukkit.Location location) {
		remove(new Location(location));
	}
	
	void remove(Location location) {
		TelePad telePad = get(location);
		if (telePad == null) return;
		TelePadsPlayer.get(telePad.ownerID()).remove(location);
		TelePads.remove(location);
		if (TelePadsGlobal.contains(location)) TelePadsGlobal.remove(location);
		World world = null;
		for (World world1 : Bukkit.getWorlds()) if (world1.getName().equals(location.world)) world = world1;
		if (world == null) return;
		Block block = world.getBlockAt(location.x,location.y,location.z);
		if (block.getType() == Material.END_PORTAL_FRAME) block.setType(Material.AIR);
	}
	
	void add(UUID playerID, Location location, TelePad telePad) {
		if (TelePadsPlayer.containsKey(playerID.toString())) {
			if (!TelePadsPlayer.get(playerID.toString()).contains(location)) TelePadsPlayer.get(playerID.toString()).add(location);
			else return;
		} else {
			List<Location> playerTelePads = new ArrayList<Location>();
			playerTelePads.add(location);
			TelePadsPlayer.put(playerID.toString(),playerTelePads);
		}
		TelePads.put(location,telePad);
		if (!TelePadsGlobal.contains(location) && telePad.global()) TelePadsGlobal.add(location);
	}
	
	void add(UUID playerID, org.bukkit.Location location, TelePad telePad) {
		add(playerID, new Location(location),telePad);
	}
	
	TelePad get(Location location) {
		if (location == null) return null;
		if (TelePads.containsKey(location)) return TelePads.get(location);
		return null;
	}
	
	TelePad get(org.bukkit.Location location) {
		if (location == null) return null;
		return get(new Location(location));
	}
	
	List<Location> get(UUID playerID) {
		return get(playerID.toString());
	}
	
	List<Location> get(String playerID) {
		if (TelePadsPlayer.containsKey(playerID)) return TelePadsPlayer.get(playerID);
		return null;
	}
	
	Location get(TelePad telePad) {
		for (Location loc : TelePadsPlayer.get(telePad.ownerID())) if (TelePads.get(loc).equals(telePad)) return loc;
		return null;
	}
	
	List<Location> getPrivate(String playerID) {
		List<Location> privateTelePads = new ArrayList<Location>();
		if (TelePadsPlayer.containsKey(playerID)) for (Location telePad : get(playerID)) if (!TelePadsGlobal.contains(telePad)) privateTelePads.add(telePad);
		return privateTelePads;
	}
	
	List<Location> getAll() {
		List<Location> allTelePads = new ArrayList<Location>();
		for (Entry<String,List<Location>> telePads : TelePadsPlayer.entrySet()) for (Location telePad : telePads.getValue()) allTelePads.add(telePad);
		return allTelePads;
	}
	
	String getGlobalName(Location location) {
		if (TelePadsGlobalNames.containsKey(location)) return TelePadsGlobalNames.get(location);
		return null;
	}
	
	public String toString() {
		String str = "";
		for (Entry<Location,TelePad> telePad : TelePads.entrySet()) {
			Location loc = telePad.getKey();
			TelePad pad = telePad.getValue();
			str += loc.world + " " + "(" + loc.x + "," + loc.y + "," + loc.z + ") - " + pad.ownerID() + ", limit: " + pad.limit() + "\n";
		}
		return str;
	}
	
	public void fix() {
		List<Location> remove = new ArrayList<Location>();
		for (Location loc : TelePads.keySet()) {
			World world = null;
			for (World world1 : Bukkit.getWorlds()) if (world1.getName().equals(loc.world)) world = world1;
			if (world == null) remove.add(loc);
			else {
				Block block = world.getBlockAt(loc.x,loc.y,loc.z);
				if (block != null && block.getType() != Material.END_PORTAL_FRAME) {
					Location location = new Location(block.getLocation());
					if ((get(location.add(0,-1,0)) != null && !remove.contains(location.add(0,-1,0))) ||
							(get(location.add(0,-2,0)) != null && !remove.contains(location.add(0,-2,0)))) remove.add(loc);
					else block.setType(Material.END_PORTAL_FRAME);
				}
			}
		}
		for (Location loc : remove) remove(loc);
	}
}