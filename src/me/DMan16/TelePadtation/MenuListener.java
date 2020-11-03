package me.DMan16.TelePadtation;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	void onCloseInventory(InventoryCloseEvent event) {
		if (TelePadtation.MenuManager.containsKey(event.getPlayer())) {
			if (event.getInventory().equals(event.getPlayer().getOpenInventory().getTopInventory())) {
				Menu menu = TelePadtation.MenuManager.get(event.getPlayer());
				TelePad telePad = TelePadtation.TelePadsManager.get(menu.location);
				if (telePad != null) {
					if (menu.isFill()) {
						int charge = 0;
						for (int i = 0; i < TelePad.fillSlots; i++) {
							ItemStack item = menu.getInv(menu.loc(2,1) + i);
							if (TelePad.isFuel(item)) charge += item.getAmount();
						}
						telePad.extra(charge);
					}
					telePad.recharge();
				} else if (TelePadtation.TelePadsManager.TelePadsSingleUse.containsKey(event.getPlayer().getUniqueId().toString())) {
					if (TelePadtation.TelePadsManager.TelePadsSingleUse.get(event.getPlayer().getUniqueId().toString()).used() > 0 &&
							event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						if (Utils.compareItems(event.getPlayer().getInventory().getItemInMainHand(),Recipes.recipePocketTelePad().getResult())) {
							int amount = event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
							if (amount == 0) event.getPlayer().getInventory().setItemInMainHand(null);
							else event.getPlayer().getInventory().getItemInMainHand().setAmount(amount);
						}
						else if (Utils.compareItems(event.getPlayer().getInventory().getItemInOffHand(),Recipes.recipePocketTelePad().getResult())) {
							int amount = event.getPlayer().getInventory().getItemInOffHand().getAmount() - 1;
							if (amount == 0) event.getPlayer().getInventory().setItemInOffHand(null);
							else event.getPlayer().getInventory().getItemInOffHand().setAmount(amount);
						}
					}
					TelePadtation.TelePadsManager.TelePadsSingleUse.remove(event.getPlayer().getUniqueId().toString());
				}
				TelePadtation.MenuManager.remove(event.getPlayer());
			}
		}
	}

	@EventHandler
	void onInventoryDrag(InventoryDragEvent event) {
		if (event.getCursor() == null) return;
		if (TelePadtation.MenuManager.containsKey(event.getWhoClicked())) {
			event.getRawSlots().forEach(slot -> {
				if (slot >= TelePadtation.MenuManager.get(event.getWhoClicked()).size()) {
					return;
				}
			});
			if (TelePadtation.MenuManager.get(event.getWhoClicked()).isFill()) {
				if (!TelePad.isFuel(event.getCursor())) event.setCancelled(true);
			} else event.setCancelled(true);
		}
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (TelePadtation.MenuManager.containsKey(event.getWhoClicked())) {
			boolean cancel = true;
			Menu menu = TelePadtation.MenuManager.get(event.getWhoClicked());
			Button button = null;
			if (menu.isFill() && event.getRawSlot() >= menu.loc(2,1) && event.getRawSlot() < (menu.loc(2,1) + TelePad.fillSlots)) {
				if (TelePad.isFuel(event.getCursor()) || event.getCursor() == null || event.getCursor().getType().isAir() || event.isShiftClick()) cancel = false;
			} else if (event.getRawSlot() >= menu.size()) {
				if (event.isShiftClick()) cancel = !TelePad.isFuel(event.getCurrentItem());
				else cancel = false;
			} else button = menu.get(event.getRawSlot());
			event.setCancelled(cancel);
			if (cancel && event.getClick() != ClickType.RIGHT && event.getClick() != ClickType.LEFT) return;
			if (button != null) button.run(menu);
		}
	}
}