package me.DMan16.TelePadtation.Listeners;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.Events.TelePadPostPlaceEvent;
import me.DMan16.TelePadtation.Events.TelePadPrePlaceEvent;
import me.DMan16.TelePadtation.Interfaces.Listener;
import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class TelePadListener implements Listener {
	public TelePadListener() {
		register(TelePadtationMain.getInstance());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void onPlaceEvent(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		TelePadItem.TelePadItemPlaceable<?> itemTelePad = TelePadtationMain.TelePadsManager().getPlaceable(item);
		if (itemTelePad == null) return;
		event.setCancelled(true);
		Block block = event.getBlock();
		BlockLocation location = new BlockLocation(block);
		if (!TelePad.canBlockTelePadTeleport(location)) return;
		Player player = event.getPlayer();
		long owned,limit;
		try {
			owned = TelePadtationMain.databaseConnection().owned(player);
			limit = TelePadtationMain.configManager().limit(player);
			if (owned >= limit) {
				TelePadtationMain.languageManager().limitReached(player,limit);
				return;
			}
		} catch (SQLException e) {
			event.setCancelled(true);
			e.printStackTrace();
			TelePadtationMain.languageManager().errorDatabase(player);
			return;
		}
		TelePad.TelePadPlaceable telePad = itemTelePad.createTelePad(player,location);
		if (!Utils.callEventCancellable(new TelePadPrePlaceEvent(telePad,player))) return;
		ItemStack clone = item.clone();
		clone.setAmount(1);
		if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
		else item = null;
		player.getInventory().setItem(event.getHand(),item);
		TelePadtationMain.databaseConnection().add(telePad,() -> {
			TelePadtationMain.languageManager().telePadCreated(player,telePad,owned + 1,limit);
			block.setType(TelePad.MATERIAL_BLOCK);
			Utils.callEvent(new TelePadPostPlaceEvent(telePad,player));
		},() -> {
			TelePadtationMain.languageManager().errorDatabase(player);
			player.getInventory().addItem(clone);
		});
	}
	
	@Nullable
	private static TelePad.TelePadPlaceable getTelePad(@NotNull BlockLocation location) throws SQLException {
		return TelePadtationMain.databaseConnection().getTelePad(location);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		TelePadtationMain.databaseConnection().loadWorld(event.getWorld());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void onInteractEvent(PlayerInteractEvent event) {
		if (event.getHand() == null || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) return;
		Player player = event.getPlayer();
		TelePadItem<?> itemTelePad;
		if (event.hasItem() && (itemTelePad = TelePadtationMain.TelePadsManager().getPortable(event.getItem())) != null) {
			event.setCancelled(true);
			itemTelePad.createTelePad(player,new BlockLocation(player.getWorld(),player.getLocation())).access(player,event.getHand());
		} else {
			Block block = event.getClickedBlock();
			if (block == null) return;
			try {
				TelePad.TelePadPlaceable telePad = getTelePad(new BlockLocation(block));
				if (telePad == null) return;
				event.setCancelled(true);
				telePad.access(player,event.getHand());
			} catch (SQLException e) {
				event.setCancelled(true);
				e.printStackTrace();
				TelePadtationMain.languageManager().errorDatabase(player);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			TelePad.TelePadPlaceable telePad = getTelePad(new BlockLocation(event.getBlock()));
			if (telePad == null) return;
			event.setCancelled(true);
			TelePadtationMain.configManager().fixOrOk(event.getBlock(),telePad,null,null);
		} catch (SQLException e) {
			event.setCancelled(true);
			e.printStackTrace();
			TelePadtationMain.languageManager().errorDatabase(event.getPlayer());
		}
	}
}