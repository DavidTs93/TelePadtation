package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Interfaces.Listener;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ListenerInventory implements Listener {
	public static final int LINE_SIZE = 9;
	
	private final @NotNull Inventory inventory;
	private final @NotNull UUID ownerID;
	protected final int size;
	protected boolean cancelCloseUnregister;
	private boolean registered;
	
	public ListenerInventory(@NotNull Inventory inv,@NotNull UUID ownerID) {
		this.inventory = inv;
		this.ownerID = ownerID;
		this.size = inv.getSize();
		this.registered = false;
	}
	
	protected boolean equals(@NotNull Inventory inv) {
		return inventory.equals(inv);
	}
	
	protected boolean equals(@NotNull InventoryView view) {
		return equals(view.getTopInventory());
	}
	
	protected boolean equals(@NotNull InventoryEvent event) {
		return equals(event.getView());
	}
	
	public boolean legalSlot(int slot) {
		return slot >= 0 && slot < size;
	}
	
	protected final void setItem(int slot,@Nullable ItemStack item) {
		if (legalSlot(slot)) inventory.setItem(slot,item);
	}
	
	@Nullable
	protected final InventoryView openInventory(@NotNull Player player) {
		if (player.isOnline() && !player.isDead()) return player.openInventory(inventory);
		close();
		return null;
	}
	
	protected final boolean isRegistered() {
		return registered;
	}
	
	@Nullable
	protected InventoryView open(@NotNull JavaPlugin plugin,@NotNull Player player) {
		if (!isRegistered()) {
			register(plugin);
			registered = true;
		}
		return openInventory(player);
	}
	
	protected final void close(@Nullable Boolean unregister) {
		if (unregister == null) unregister = !this.cancelCloseUnregister;
		if (unregister) {
			unregister();
			afterClose(null);
		} else this.cancelCloseUnregister = true;
		inventory.getViewers().forEach(HumanEntity::closeInventory);
	}
	
	public void close() {
		close(true);
	}
	
	@Nullable
	protected ItemStack getItem(int slot) {
		return inventory.getItem(slot);
	}
	
	protected void afterClose(@Nullable InventoryCloseEvent event) {}
	
	protected void afterLeave(@NotNull PlayerQuitEvent event) {
		afterClose(null);
	}
	
	@Override
	public void unregister() {
		if (!isRegistered()) return;
		Listener.super.unregister();
		registered = false;
	}
	
	@EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
	public void unregisterOnClose(InventoryCloseEvent event) {
		if (!equals(event)) return;
		if (!cancelCloseUnregister) unregister();
		afterClose(event);
	}
	
	@EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
	public void unregisterOnLeaveEvent(PlayerQuitEvent event) {
		if (!event.getPlayer().getUniqueId().equals(ownerID)) return;
		unregister();
		afterLeave(event);
	}
}