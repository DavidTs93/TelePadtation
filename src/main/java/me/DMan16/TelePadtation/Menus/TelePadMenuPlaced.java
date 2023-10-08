package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TelePadMenuPlaced extends TelePadMenuTeleport<TelePad.TelePadPlaceable> {
	public TelePadMenuPlaced(TelePad.@NotNull TelePadPlaceable telePad,@NotNull String title,@NotNull Player player,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		super(telePad,5,title,player,status,destinations);
	}
	
	public TelePadMenuPlaced(TelePad.@NotNull TelePadPlaceable telePad,@NotNull Player player,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		this(telePad,TelePadtationMain.languageManager().titleMenu(telePad),player,status,destinations);
	}
	
	protected void handleEdit() {
		try {
			new TelePadMenuEdit(telePad,title,player,status,destinations,TelePadMenuPlaced::new);
		} catch (IllegalAccessException e) {}
	}
}