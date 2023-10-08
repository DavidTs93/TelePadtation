package me.DMan16.TelePadtation.Managers;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.Classes.TelePadInfoPlaceable;
import me.DMan16.TelePadtation.Classes.TelePadInfoPortable;
import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TelePadsManager {
	private final LinkedHashMap<@NotNull String,@NotNull TelePadInfoPlaceable<?,?>> placeables = new LinkedHashMap<>();
	private final LinkedHashMap<@NotNull String,@NotNull TelePadInfoPortable<?,?>> portables = new LinkedHashMap<>();
	
	public boolean registerPlaceable(@NotNull TelePadInfoPlaceable<?,?> info) {
		TelePadItem<?> item = info.constructorTelePadItem().get();
		String key = item.type();
		if (placeables.containsKey(key) || portables.containsKey(key) || !item.material().isBlock()) return false;
		placeables.put(key,info);
		return true;
	}
	
	public boolean registerPortable(@NotNull TelePadInfoPortable<?,?> info) {
		String key = info.constructorTelePadItem().get().type();
		if (portables.containsKey(key) || placeables.containsKey(key)) return false;
		portables.put(key,info);
		return true;
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem.TelePadItemPlaceable<?> getPlaceable(@Nullable ItemStack item) {
		if (Utils.notNull(item)) for (TelePadInfoPlaceable<?,?> info : placeables.values()) if (info.checkTelePadItem().test(item)) return info.constructorTelePadItem().get();
		return null;
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem.TelePadItemPlaceable<?> getPlaceable(@Nullable String type) {
		return Utils.applyNotNull(Utils.fixKey(type),key -> Utils.applyNotNull(placeables.get(key),info -> info.constructorTelePadItem().get()));
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem.TelePadItemPortable<?> getPortable(@Nullable ItemStack item) {
		if (Utils.notNull(item)) for (TelePadInfoPortable<?,?> info : portables.values()) if (info.checkTelePadItem().test(item)) return info.constructorTelePadItem().get();
		return null;
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem.TelePadItemPortable<?> getPortable(@Nullable String type) {
		return Utils.applyNotNull(Utils.fixKey(type),key -> Utils.applyNotNull(portables.get(key),info -> info.constructorTelePadItem().get()));
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem<?> get(@Nullable ItemStack item) {
		TelePadItem<?> placeableTelePadItem = getPlaceable(item);
		return placeableTelePadItem == null ? getPortable(item) : placeableTelePadItem;
	}
	
	@Nullable
	@Contract("null -> null")
	public TelePadItem<?> get(@Nullable String type) {
		TelePadItem<?> placeableTelePadItem = getPlaceable(type);
		return placeableTelePadItem == null ? getPortable(type) : placeableTelePadItem;
	}
	
	@NotNull
	@Unmodifiable
	public List<@NotNull String> getTypes() {
		return Collections.unmodifiableList(Stream.concat(placeables.keySet().stream(),portables.keySet().stream()).map(String::toUpperCase).collect(Collectors.toList()));
	}
	
	@Nullable
	public TelePad.TelePadPlaceable getFromDatabase(@NotNull String type,@NotNull UUID ownerID,@NotNull BlockLocation location,int used,@Range(from = 0,to = TelePad.AMOUNT_FILL) int extraFuel,boolean isGlobal,@Nullable String name) {
		return Utils.applyNotNull(Utils.fixKey(type),key -> Utils.applyNotNull(placeables.get(key),info -> info.constructorTelePadPlaceable().constructor(ownerID,location,used,extraFuel,isGlobal,name)));
	}
}