package me.DMan16.TelePadtation;

import java.util.function.Consumer;

import org.bukkit.inventory.ItemStack;

import com.mojang.datafixers.util.Pair;

public class Button {
	private ItemStack item;
	private Consumer<Pair<Menu,Location>> method;
	final Location info;
	
	Button(ItemStack item, Consumer<Pair<Menu,Location>> method, Location info) {
		this.item = item == null ? null : item.clone();
		this.method = method;
		this.info = info;
	}
	
	Button(ItemStack item, Location info) {
		this(item,null,info);
	}
	
	void run(Menu menu) {
		if (method == null) return;
		method.accept(new Pair<Menu,Location>(menu,info));
	}
	
	ItemStack item() {
		return (item == null ? null : item.clone());
	}
}