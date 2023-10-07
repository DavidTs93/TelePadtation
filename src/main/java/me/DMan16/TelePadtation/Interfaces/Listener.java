package me.DMan16.TelePadtation.Interfaces;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Listener class with functions to register and unregister.
 * Extend this class with your listener. Don't forget to register the listener via {@link #register}!
 */
public interface Listener extends org.bukkit.event.Listener {
	default void register(@NotNull JavaPlugin instance) {
		Bukkit.getServer().getPluginManager().registerEvents(this,instance);
	}
	
	default void unregister() {
		HandlerList.unregisterAll(this);
	}
}