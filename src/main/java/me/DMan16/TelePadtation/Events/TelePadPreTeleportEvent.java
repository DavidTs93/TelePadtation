package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public final class TelePadPreTeleportEvent<V extends TelePad,U extends TelePad> extends TelePadTeleportEvent<V,U> implements Cancellable {
	private boolean cancel = false;
	
	public TelePadPreTeleportEvent(@NotNull V origin,@NotNull U destination,@NotNull Player player) {
		super(origin,destination,player);
	}
	
	public void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}
	
	public boolean isCancelled() {
		return cancel;
	}
}