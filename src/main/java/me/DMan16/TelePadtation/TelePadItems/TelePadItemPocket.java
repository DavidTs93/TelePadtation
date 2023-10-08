package me.DMan16.TelePadtation.TelePadItems;

import me.DMan16.TelePadtation.Enums.Heads;
import me.DMan16.TelePadtation.TelePads.TelePadPocket;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class TelePadItemPocket extends TelePadItem.TelePadItemPortable<TelePadPocket> {
	public static final NamespacedKey KEY_NAMESPACED = Utils.namespacedKey(TelePadPocket.KEY);
	
	public TelePadItemPocket() {
		super(TelePadPocket.KEY,TelePadPocket::new);
	}
	
	@NotNull
	public String displayName() {
		return TelePadtationMain.languageManager().nameTelePadPocket();
	}
	
	@Nullable
	protected List<@NotNull String> lore() {
		return TelePadtationMain.languageManager().loreTelePadPocket();
	}
	
	@Override
	@NotNull
	public Material material() {
		return TelePadtationMain.configManager().headPocket().isEmpty() ? super.material() : Material.PLAYER_HEAD;
	}
	
	@Override
	@NotNull
	protected ItemStack alterItem(@NotNull ItemStack item) {
		if (item.getType() != Material.PLAYER_HEAD) return item;
		item.setItemMeta(Heads.setSkin((SkullMeta) Objects.requireNonNull(item.getItemMeta()),TelePadtationMain.configManager().headPocket()));
		return item;
	}
	
	public static boolean isTelePadItemPocket(@Nullable ItemStack item) {
		return isTelePadItemPDC(TelePadPocket.KEY,item);
	}
}