package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TelePadPostTeleportEvent<V extends TelePad,U extends TelePad> extends TelePadTeleportEvent<V,U> {
	
	public TelePadPostTeleportEvent(@NotNull V origin,@NotNull U destination,@NotNull Player player) {
		super(origin,destination,player);
	}
}