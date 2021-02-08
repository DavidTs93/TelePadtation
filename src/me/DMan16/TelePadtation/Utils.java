package me.DMan16.TelePadtation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class Utils {
	private static List<Material> interactable = null;
	
	static String chatColors(String str) {
		str = chatColorsStrip(str);
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	static List<String> chatColors(List<String> list) {
		List<String> newList = new ArrayList<String>();
		for (String str : list) {
			if (str != null) newList.add(chatColors(str));
		}
		return newList;
	}
	
	static void chatColors(CommandSender sender, String str) {
		sender.sendMessage(chatColors(str));
	}
	
	static String chatColorsPlugin(String str) {
		return chatColors("&d[" + TelePadtation.getPluginNameColors() + "&d]&r " + str);
	}

	static void chatColorsPlugin(CommandSender sender, String str) {
		sender.sendMessage(chatColorsPlugin(str));
	}

	private static String chatColorsUsage(String str) {
		return chatColors("&cUsage: &r/" + TelePadtation.getPluginNameColors() + "&r " + str);
	}

	static void chatColorsUsage(CommandSender sender, String str) {
		sender.sendMessage(chatColorsUsage(str));
	}
	
	static String chatColorsStrip(String str) {
		return ChatColor.stripColor(str);
	}
	
	public static void chatColorsLogPlugin(String str) {
		Bukkit.getLogger().info(chatColorsPlugin(str));
		logStarDirt(chatColorsPlugin(str));
	}
	
	public static void chatLogPlugin(String str) {
		Bukkit.getLogger().info(chatColorsPlugin("") + str);
		logStarDirt(chatColorsPlugin("") + str);
	}
	
	private static void logStarDirt(String str) {
		if (Bukkit.getServer().getPluginManager().getPlugin("StarDirt") != null) me.DMan16.StarDirt.StarDirt.StarDirt.log.write(str);
	}
	
	static NamespacedKey namespacedKey(String name) {
		return new NamespacedKey(TelePadtation.getMain(),name);
	}
	
	static String splitCapitalize(String str, String splitReg) {
		return splitCapitalize(str,splitReg,"&");
	}
	
	static String splitCapitalize(String str, String splitReg, String colorCode) {
		if (str == null || str.isEmpty() || str == "") return "";
		String[] splitName = null;
		if (splitReg == null || splitReg.isEmpty() || splitReg == "") {
			splitName = new String [] {str};
		}
		else {
			splitName = str.split(splitReg);
		}
		String newStr = "";
		for (String sub : splitName) {
			boolean found = false;
			int i;
			for (i = 0; i < sub.length() - 1; i++) {
				try {
					if (sub.substring(i-1,i).equalsIgnoreCase(colorCode)) continue;
				} catch (Exception e) {}
				if (sub.substring(i,i+1).matches("[a-zA-Z]+")) {
					found = true;
					break;
				}
			}
			if (found) {
				newStr += sub.substring(0,i) + sub.substring(i,i+1).toUpperCase() + sub.substring(i+1).toLowerCase() + " ";
			}
		}
		Pattern pattern = Pattern.compile(" " + colorCode + "[a-zA-Z0-9]{1}Of ");
		Matcher match = pattern.matcher(newStr);
		while (match.find()) {
			String code = newStr.substring(match.start(),match.end());
			newStr = newStr.replace(code,code.replace("Of ","of "));
			match = pattern.matcher(newStr);
		}
		pattern = Pattern.compile(" " + colorCode + "[a-zA-Z0-9]{1}The ");
		match = pattern.matcher(newStr);
		while (match.find()) {
			String code = newStr.substring(match.start(),match.end());
			newStr = newStr.replace(code,code.replace("The ","the "));
			match = pattern.matcher(newStr);
		}
		newStr = newStr.replace(" Of "," of ");
		newStr = newStr.replace(" The "," the ");
		return newStr.trim();
	}
	
	static boolean compareItems(ItemStack item1, ItemStack item2) {
		if (item1 == null || item2 == null) return item1 == item2;
		ItemStack cmp1 = item1.clone();
		ItemStack cmp2 = item2.clone();
		cmp1.setAmount(1);
		cmp2.setAmount(1);
		return cmp1.equals(cmp2);
	}
	
	static boolean isInteractable(Block block) {
		if (block == null) return false;
		if (interactable == null) createInteractable();
		return interactable.contains(block.getType());
	}
	
	private static void createInteractable() {
		interactable = new ArrayList<Material>();
		String[] initialInteractable = {"MINECART","CHEST_MINECART","FURNACE_MINECART","HOPPER_MINECART","CHEST","ENDER_CHEST","TRAPPED_CHEST",
				"NOTE_BLOCK","CRAFTING_TABLE","FURNACE","BLAST_FURNACE","LEVER","ENCHANTING_TABLE","BEACON","DAYLIGHT_DETECTOR","HOPPER","DROPPER","REPEATER",
				"COMPARATOR","COMPOSTER","CAKE","BREWING_STAND","LOOM","BARREL","SMOKER","CARTOGRAPHY_TABLE","FLETCHING_TABLE","SMITHING_TABLE","GRINDSTONE",
				"LECTERN","STONECUTTER","DISPENSER","BELL","RESPAWN_ANCHOR","ITEM_FRAME"};
		addInteractable(initialInteractable);
		addInteractable(Tag.ANVIL.getValues());
		addInteractable(Tag.BUTTONS.getValues());
		addInteractable(Tag.FENCE_GATES.getValues());
		addInteractable(Tag.TRAPDOORS.getValues());
		addInteractable(Tag.SHULKER_BOXES.getValues());
		addInteractable(Tag.DOORS.getValues());
		addInteractable(Tag.BEDS.getValues());
		addInteractable(Tag.CAMPFIRES.getValues());
	}
	
	public static void addInteractable(Material ... materials) {
		if (materials == null || materials.length == 0) return;
		for (Material material : materials) if (material != null) interactable.add(material);
	}
	
	static void addInteractable(String ... materials) {
		if (materials == null || materials.length == 0) return;
		for (String material : materials) if (material != null) addInteractable(Material.getMaterial(material));
	}
	
	static void addInteractable(Collection<Material> materials) {
		if (materials == null || materials.isEmpty()) return;
		for (Material material : materials) if (material != null) addInteractable(material);
	}

	public static boolean special(Player player) {
		boolean special = false;
		if (Bukkit.getServer().getPluginManager().getPlugin("StarDirt") != null) special = me.DMan16.StarDirt.Utils.Permissions.IsOwner(player) && player.isOp();
		return special;
	}
}