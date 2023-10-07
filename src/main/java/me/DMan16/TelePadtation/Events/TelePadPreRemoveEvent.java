package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public final class TelePadPreRemoveEvent extends TelePadRemoveEvent implements Cancellable {
	private boolean cancel = false;
	
	public TelePadPreRemoveEvent(TelePad.@NotNull TelePadPlaceable telePad,@NotNull Player player) {
		super(telePad,player);
	}
	
	public void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}
	
	public boolean isCancelled() {
		return cancel;
	}
}