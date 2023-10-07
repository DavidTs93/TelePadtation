package me.DMan16.TelePadtation.Events;

import me.DMan16.TelePadtation.TelePads.TelePad;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class TelePadtationEvent<V extends TelePad> extends Event {
	private final HandlerList Handlers = new HandlerList();
	private final @NotNull V telePad;
	
	protected TelePadtationEvent(@NotNull V telePad) {
		this.telePad = telePad;
	}
	
	@NotNull
	public final V telePad() {
		return telePad;
	}
	
	@Override
	@NotNull
	public HandlerList getHandlers() {
		return Handlers;
	}
}