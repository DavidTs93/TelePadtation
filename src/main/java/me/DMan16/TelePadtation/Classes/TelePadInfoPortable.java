package me.DMan16.TelePadtation.Classes;

import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class TelePadInfoPortable<I extends TelePadItem.TelePadItemPortable<T>,T extends TelePad.TelePadPortable> extends TelePadInfo<I,T> {
	public TelePadInfoPortable(@NotNull Predicate<ItemStack> checkTelePadItem,@NotNull Supplier<I> constructorTelePadItem) {
		super(checkTelePadItem,constructorTelePadItem);
	}
}