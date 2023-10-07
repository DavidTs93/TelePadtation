package me.DMan16.TelePadtation.TelePadItems;

import me.DMan16.TelePadtation.TelePads.TelePadStandard;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TelePadItemStandard extends TelePadItem.TelePadItemPlaceable<TelePadStandard> {
	public static final NamespacedKey KEY_NAMESPACED = Utils.namespacedKey(TelePadStandard.KEY);
	
	public TelePadItemStandard() {
		super(TelePadStandard.KEY,TelePadStandard::new);
	}
	
	@NotNull
	public String displayName() {
		return TelePadtationMain.languageManager().nameTelePadStandard();
	}
	
	@Nullable
	protected List<@NotNull String> lore() {
		return TelePadtationMain.languageManager().loreTelePadStandard();
	}
	
	public static boolean isTelePadItemStandard(@Nullable ItemStack item) {
		return isTelePadItemPDC(TelePadStandard.KEY,item);
	}
}