package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class TelePadPlaceEvent extends TelePadtationEvent<TelePad.TelePadPlaceable> {
	private final @NotNull Player player;
	
	public TelePadPlaceEvent(TelePad.@NotNull TelePadPlaceable telePad,@NotNull Player player) {
		super(telePad);
		this.player = player;
	}
	
	@NotNull
	public Player player() {
		return player;
	}
}