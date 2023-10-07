package me.DMan16.TelePadtation.Classes;

import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TelePadInfoPlaceable<I extends TelePadItem.TelePadItemPlaceable<T>,T extends TelePad.TelePadPlaceable> extends TelePadInfo<I,T> {
	private final @NotNull ConstructorTelePadPlaceable<T> constructorTelePad;
	
	public TelePadInfoPlaceable(@NotNull Predicate<ItemStack> checkTelePadItem,@NotNull Supplier<I> constructorTelePadItem,@NotNull TelePadInfoPlaceable.ConstructorTelePadPlaceable<T> constructorTelePad) {
		super(checkTelePadItem,constructorTelePadItem);
		this.constructorTelePad = constructorTelePad;
	}
	
	@NotNull
	public ConstructorTelePadPlaceable<T> constructorTelePadPlaceable() {
		return constructorTelePad;
	}
	
	@FunctionalInterface
	public interface ConstructorTelePadPlaceable<T> {
		@Contract("_,_,_,_,_,_ -> new") T constructor(@NotNull UUID ownerID,@NotNull BlockLocation location,int used,@Range(from = 0,to = TelePad.AMOUNT_FILL) int extraFuel,boolean isGlobal,@Nullable String name);
	}
}