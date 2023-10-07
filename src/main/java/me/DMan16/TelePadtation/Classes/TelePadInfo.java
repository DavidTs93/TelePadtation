package me.DMan16.TelePadtation.Classes;

import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TelePadInfo<I extends TelePadItem<T>,T extends TelePad> {
	private final @NotNull Predicate<ItemStack> checkTelePadItem;
	private final @NotNull Supplier<I> constructorTelePadItem;
	
	public TelePadInfo(@NotNull Predicate<ItemStack> checkTelePadItem,@NotNull Supplier<I> constructorTelePadItem) {
		this.checkTelePadItem = checkTelePadItem;
		this.constructorTelePadItem = constructorTelePadItem;
	}
	
	@NotNull
	public Predicate<ItemStack> checkTelePadItem() {
		return checkTelePadItem;
	}
	
	@NotNull
	public Supplier<I> constructorTelePadItem() {
		return constructorTelePadItem;
	}
}