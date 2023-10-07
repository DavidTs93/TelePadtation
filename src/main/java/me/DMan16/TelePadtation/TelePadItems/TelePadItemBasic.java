package me.DMan16.TelePadtation.TelePadItems;

import me.DMan16.TelePadtation.TelePads.TelePadBasic;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TelePadItemBasic extends TelePadItem.TelePadItemPlaceable<TelePadBasic> {
	public static final NamespacedKey KEY_NAMESPACED = Utils.namespacedKey(TelePadBasic.KEY);
	
	public TelePadItemBasic() {
		super(TelePadBasic.KEY,TelePadBasic::new);
	}
	
	@NotNull
	public String displayName() {
		return TelePadtationMain.languageManager().nameTelePadBasic();
	}
	
	@Nullable
	protected List<@NotNull String> lore() {
		return TelePadtationMain.languageManager().loreTelePadBasic();
	}
	
	public static boolean isTelePadItemBasic(@Nullable ItemStack item) {
		return isTelePadItemPDC(TelePadBasic.KEY,item);
	}
}