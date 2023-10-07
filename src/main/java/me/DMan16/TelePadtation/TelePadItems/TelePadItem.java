package me.DMan16.TelePadtation.TelePadItems;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TelePadItem<V extends TelePad> {
	private final @NotNull String type;
	private final @NotNull BiFunction<@NotNull Player,@NotNull BlockLocation,@NotNull V> constructorTelePad;
	
	private TelePadItem(@NotNull String type,@NotNull BiFunction<@NotNull Player,@NotNull BlockLocation,@NotNull V> constructorTelePad) {
		this.type = Objects.requireNonNull(Utils.fixKey(type));
		this.constructorTelePad = constructorTelePad;
	}
	
	@NotNull
	public final String type() {
		return type;
	}
	
	@NotNull
	public V createTelePad(@NotNull Player player,@NotNull BlockLocation block) {
		return constructorTelePad.apply(player,block);
	}
	
	@NotNull
	public Material material() {
		return TelePad.MATERIAL_BLOCK;
	}
	
	public abstract @NotNull String displayName();
	
	protected abstract @Nullable List<@NotNull String> lore();
	
	@NotNull
	protected ItemStack alterItem(@NotNull ItemStack item) {
		return item;
	}
	
	@NotNull
	@Contract(" -> new")
	public ItemStack toItem() {
		ItemStack item = new ItemStack(material());
		ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
		Utils.setDisplayNameLore(meta,displayName(),lore());
		meta.getPersistentDataContainer().set(Utils.namespacedKey(type()),PersistentDataType.STRING,"");
		item.setItemMeta(meta);
		return alterItem(item);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TelePadItem)) return false;
		TelePadItem<?> other = (TelePadItem<?>) obj;
		return type.equals(other.type) && constructorTelePad.equals(other.constructorTelePad);
	}
	
	@Override
	public int hashCode() {
		return type().hashCode();
	}
	
	protected static boolean isTelePadItemPDC(@NotNull String key,@Nullable ItemStack item) {
		if (Utils.isNull(item)) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(Utils.namespacedKey(key),PersistentDataType.STRING);
	}
	
	public static abstract class TelePadItemPlaceable<T extends TelePad.TelePadPlaceable> extends TelePadItem<T> {
		protected TelePadItemPlaceable(@NotNull String type,@NotNull BiFunction<@NotNull Player,@NotNull BlockLocation,@NotNull T> constructorTelePad) {
			super(type,constructorTelePad);
		}
	}
	
	public static abstract class TelePadItemPortable<T extends TelePad.TelePadPortable> extends TelePadItem<T> {
		protected TelePadItemPortable(@NotNull String type,@NotNull Function<@NotNull Player,@NotNull T> constructorTelePad) {
			super(type,((player,location) -> constructorTelePad.apply(player)));
		}
	}
}