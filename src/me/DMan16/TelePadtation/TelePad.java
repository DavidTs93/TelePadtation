package me.DMan16.TelePadtation;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class TelePad {
	private final String ownerID;
	private final int limit;
	private int used;
	private int extra;
	private boolean global;
	static final ItemStack fuel = new ItemStack(Material.ENDER_PEARL);
	static final int fillSlots = 5;
	static final List<Integer> maxUses = Arrays.asList(4,8,16);
	
	public TelePad(String ownerID, ItemStack item) {
		String ID = null;
		int limit = 0;
		if (item != null && item.getType() == Material.END_PORTAL_FRAME) {
			PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
			for (NamespacedKey key : container.getKeys()) {
				if (key.getKey().startsWith("telepad_")) {
					try {
						limit = Integer.parseInt(key.getKey().replace("telepad_",""));
						ID = ownerID;
						break;
					} catch (Exception e) {
					}
				}
			}
		}
		this.ownerID = ID;
		this.limit = limit;
		this.used = limit;
		this.extra = 0;
		this.global = false;
	}
	
	public TelePad(String ownerID, int limit, int used, int extra, boolean global) {
		this.ownerID = limit > 0 ? ownerID : null;
		this.limit = limit;
		this.used = used;
		this.extra = extra;
		this.global = global;
	}
	
	void toggleGlobal() {
		global = !global;
	}
	
	void use(Location destination, Player player) {
		if (!canUse()) return;
		if (!global) used++;
		for (World world : Bukkit.getWorlds()) {
			if (world.getName().equals(destination.world)) {
				player.teleport(new org.bukkit.Location(world,destination.x + 0.5,destination.y + 1,destination.z + 0.5));
				break;
			}
		}
		recharge();
	}
	
	boolean canUse() {
		return (global || used < limit || extra > 0);
	}
	
	String ownerID() {
		return ownerID;
	}
	
	int limit() {
		return limit;
	}
	
	boolean global() {
		return global;
	}
	
	int used() {
		return used;
	}
	
	int extra() {
		return extra;
	}
	
	int extra(int extra) {
		if (extra < 0) extra = 0;
		int left = 0;
		if (extra > fuel.getMaxStackSize() * fillSlots) {
			left = extra - fuel.getMaxStackSize() * fillSlots;
			extra = fuel.getMaxStackSize() * fillSlots;
		}
		this.extra = extra;
		return left;
	}
	
	void recharge() {
		if (global || used < limit || extra <= 0) return;
		used = 0;
		extra--;
	}
	
	static boolean isFuel(ItemStack item) {
		return Utils.compareItems(item,fuel);
	}
	
	ItemStack toItem() {
		return Recipes.get(limit);
	}
}