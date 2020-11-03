package me.DMan16.TelePadtation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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
	
	static void chatColorsLog(String str) {
		TelePadtation.getLog().info(chatColors(str));
	}
	
	static void chatColorsLogPlugin(String str) {
		TelePadtation.getLog().info(chatColorsPlugin(str));
	}
	
	static NamespacedKey namespacedKey(String name) {
		return new NamespacedKey(TelePadtation.getMain(),name);
	}
	
	static String splitCapitalize(String str, String splitReg) {
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
					if (sub.substring(i-1,i).equalsIgnoreCase("&")) continue;
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
		newStr.replace(" Of ", " of ");
		newStr.replace(" The ", " the ");
		return newStr.trim();
	}
	
	static JString JString(String str) {
		return new JString(str);
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
		List<Material> initialInteractable = Arrays.asList(Material.MINECART,Material.CHEST_MINECART,Material.FURNACE_MINECART,Material.HOPPER_MINECART,
				Material.CHEST,Material.ENDER_CHEST,Material.TRAPPED_CHEST,Material.NOTE_BLOCK,Material.CRAFTING_TABLE,Material.FURNACE,Material.BLAST_FURNACE,
				Material.LEVER,Material.ENCHANTING_TABLE,Material.BEACON,Material.DAYLIGHT_DETECTOR,Material.HOPPER,Material.DROPPER,Material.REPEATER,
				Material.COMPARATOR,Material.COMPOSTER,Material.CAKE,Material.BREWING_STAND,Material.LOOM,Material.BARREL,Material.SMOKER,
				Material.CARTOGRAPHY_TABLE,Material.FLETCHING_TABLE,Material.SMITHING_TABLE,Material.GRINDSTONE,Material.LECTERN,Material.STONECUTTER,
				Material.DISPENSER,Material.BELL,Material.RESPAWN_ANCHOR,Material.ITEM_FRAME);
		interactable.addAll(initialInteractable);
		interactable.addAll(new ArrayList<Material>(Tag.ANVIL.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.BUTTONS.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.FENCE_GATES.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.TRAPDOORS.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.DOORS.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.BEDS.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.SHULKER_BOXES.getValues()));
		interactable.addAll(new ArrayList<Material>(Tag.CAMPFIRES.getValues()));
	}
	
	@SuppressWarnings("unused")
	private static class JString implements java.io.Serializable{
		private static final long serialVersionUID = 1L;
		String value;
		JString(String value) {
			super();
			this.value = value;
		}
		
		String getValue() {
			return value;
		}
		
		void setValue(String value) {
			this.value = value;
		}
		
		@Override
		public String toString(){
			return this.value;
		}
	}
}