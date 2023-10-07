package me.DMan16.TelePadtation.Listeners;

import me.DMan16.TelePadtation.Interfaces.Listener;
import me.DMan16.TelePadtation.TelePadItems.TelePadItemAdvanced;
import me.DMan16.TelePadtation.TelePadItems.TelePadItemBasic;
import me.DMan16.TelePadtation.TelePadItems.TelePadItemPocket;
import me.DMan16.TelePadtation.TelePadItems.TelePadItemStandard;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class RecipesListener implements Listener {
	private static final @NotNull NamespacedKey KEY_COMPASS = NamespacedKey.minecraft("compass");
	private static final @NotNull HashMap<@NotNull NamespacedKey,@NotNull List<@NotNull NamespacedKey>> CRAFTS = new HashMap<>();
	
	static {
		CRAFTS.put(KEY_COMPASS,Arrays.asList(TelePadItemBasic.KEY_NAMESPACED,TelePadItemPocket.KEY_NAMESPACED));
		CRAFTS.put(TelePadItemBasic.KEY_NAMESPACED,Collections.singletonList(TelePadItemStandard.KEY_NAMESPACED));
		CRAFTS.put(TelePadItemStandard.KEY_NAMESPACED,Collections.singletonList(TelePadItemAdvanced.KEY_NAMESPACED));
	}
	
	public RecipesListener() {
		register(TelePadtationMain.getInstance());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasDiscoveredRecipe(KEY_COMPASS)) event.getPlayer().discoverRecipe(TelePadItemBasic.KEY_NAMESPACED);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerCraft(CraftItemEvent event) {
		if (event.getRecipe() instanceof ShapedRecipe) Utils.runNotNull(CRAFTS.get(((ShapedRecipe) event.getRecipe()).getKey()),add -> add.forEach(((Player) event.getWhoClicked())::discoverRecipe));
	}
}