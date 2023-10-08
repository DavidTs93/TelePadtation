package me.DMan16.TelePadtation.TelePads;

import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TelePadCommand extends TelePad.TelePadPortable {
	public TelePadCommand(@NotNull Player player) {
		super(TelePadPocket.KEY,player);
	}
	
	@NotNull
	public String skin() {
		return TelePadtationMain.configManager().headPocket();}
	
	@Override
	boolean teleportPlayer(@NotNull Player player,@NotNull Location location) {
		return player.teleport(location);
	}
}