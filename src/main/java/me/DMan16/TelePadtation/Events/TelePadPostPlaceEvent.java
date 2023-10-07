package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TelePadPostPlaceEvent extends TelePadPlaceEvent {
	public TelePadPostPlaceEvent(TelePad.@NotNull TelePadPlaceable telePad,@NotNull Player player) {
		super(telePad,player);
	}
}