package me.DMan16.TelePadtation.TelePads;

import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TelePadPocket extends TelePad.TelePadPortable {
	public static final String KEY = "pocket";
	
	public TelePadPocket(@NotNull Player player) {
		super(KEY,player);
	}
	
	@NotNull
	public String skin() {
		return TelePadtationMain.configManager().headPocket();
	}
}