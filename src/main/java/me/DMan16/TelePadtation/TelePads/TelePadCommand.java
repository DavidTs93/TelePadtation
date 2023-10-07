package me.DMan16.TelePadtation.TelePads;

import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class TelePadCommand extends TelePad.TelePadPortable {
	public static final String KEY = "command";
	
	public TelePadCommand(@NotNull Player player) {
		super(KEY,player);
	}
	
	@NotNull
	public String skin() {
		return TelePadtationMain.configManager().headPocket();
	}
	
	@Override
	@NotNull
	protected ItemStack consumeBeforeOpen(@NotNull Player player,@NotNull EquipmentSlot hand) {
		return new ItemStack(Material.AIR);
	}
}