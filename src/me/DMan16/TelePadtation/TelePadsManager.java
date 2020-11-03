package me.DMan16.TelePadtation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
						try (FileReader reader = new FileReader(file)) {
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
									Block block = world.getBlockAt(x,y,z);
									if (block != null && block.getType() == Material.END_PORTAL_FRAME) {
										Location location = new Location(path.getName(),x,y,z);
										TelePads.put(location, new TelePad(file.getName(),limit,used,extra,global));
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
									}
								} catch (Exception e) {}
							}
							if (playerTelePads.isEmpty()) remove.add(file);
							else TelePadsPlayer.put(file.getName(),playerTelePads);
						} catch (Exception e) {}
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
					path = path.resolve(playerTelePads.getKey().toString());
					Files.deleteIfExists(path);
					if (!arr.isEmpty()) {
						Files.createFile(path, new FileAttribute[0]);
						JsonElement je = jp.parse(arr.toJSONString());
						String prettyJsonString = gson.toJson(je);
						PrintWriter pw = new PrintWriter(pluginDir + "/" + worldName + "/" + playerTelePads.getKey());
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
			e.printStackTrace();
		}
	}
	
	void remove(org.bukkit.Location location) {
		remove(new Location(location));
	}
	
	void remove(Location location) {
		TelePad telePad = get(location);
		if (telePad == null) return;
		TelePadsPlayer.get(telePad.ownerID()).remove(location);
		if (TelePadsPlayer.get(telePad.ownerID()).isEmpty()) TelePadsPlayer.remove(telePad.ownerID());
		TelePads.remove(location);
		if (TelePadsGlobal.contains(location)) TelePadsGlobal.remove(location);
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
		if (TelePads.containsKey(location)) return TelePads.get(location);
		return null;
	}
	
	TelePad get(org.bukkit.Location location) {
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
	
	String getGlobalName(Location location) {
		if (TelePadsGlobalNames.containsKey(location)) return TelePadsGlobalNames.get(location);
		return null;
	}
}