package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TelePadMenuPortable extends TelePadMenuTeleport<TelePad.TelePadPortable> {
	public TelePadMenuPortable(TelePad.@NotNull TelePadPortable telePad,@NotNull String title,@NotNull Player player,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		super(telePad,5,title,player,TelePadStatus.PORTABLE,destinations);
		
		
	}
	public TelePadMenuPortable(TelePad.@NotNull TelePadPortable telePad,@NotNull Player player,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		this(telePad,TelePadtationMain.languageManager().titleMenu(telePad),player,destinations);
	}
	
	protected void handleEdit() {}
	
	@Override
	@Nullable
	protected Integer slotEdit() {
		return null;
	}
}