package me.DMan16.TelePadtation.Interfaces;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Backable {
	void goBack(@NotNull ClickType click);
	
	@Nullable Integer slotBack();
	
	@NotNull @Contract(" -> new") ItemStack itemBack();
}