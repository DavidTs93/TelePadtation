package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class TelePadTeleportEvent<V extends TelePad,U extends TelePad> extends TelePadtationEvent<V> {
	private final @NotNull Player player;
	private final @NotNull U destination;
	
	public TelePadTeleportEvent(@NotNull V origin,@NotNull U destination,@NotNull Player player) {
		super(origin);
		this.destination = destination;
		this.player = player;
	}
	
	@NotNull
	public U destination() {
		return destination;
	}
	
	@NotNull
	public Player player() {
		return player;
	}
}