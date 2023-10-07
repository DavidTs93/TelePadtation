package me.DMan16.TelePadtation.TelePadItems;

import me.DMan16.TelePadtation.TelePads.TelePadAdvanced;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TelePadItemAdvanced extends TelePadItem.TelePadItemPlaceable<TelePadAdvanced> {
	public static final NamespacedKey KEY_NAMESPACED = Utils.namespacedKey(TelePadAdvanced.KEY);
	
	public TelePadItemAdvanced() {
		super(TelePadAdvanced.KEY,TelePadAdvanced::new);
	}
	
	@NotNull
	public String displayName() {
		return TelePadtationMain.languageManager().nameTelePadAdvanced();
	}
	
	@Nullable
	protected List<@NotNull String> lore() {
		return TelePadtationMain.languageManager().loreTelePadAdvanced();
	}
	
	public static boolean isTelePadItemAdvanced(@Nullable ItemStack item) {
		return isTelePadItemPDC(TelePadAdvanced.KEY,item);
	}
}