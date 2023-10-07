package me.DMan16.TelePadtation.TelePads;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.TelePadtationMain;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public final class TelePadStandard extends TelePad.TelePadPlaceable {
	public static final String KEY = "standard";
	
	public TelePadStandard(@NotNull Player player,@NotNull BlockLocation location) {
		super(KEY,player,location);
	}
	
	public TelePadStandard(@NotNull UUID ownerID,@NotNull BlockLocation location,int used,@Range(from = 0,to = AMOUNT_FILL) int extraFuel,boolean isGlobal,@Nullable String name) {
		super(KEY,ownerID,location,used,extraFuel,isGlobal,name);
	}
	
	public int usesMax() {
		return TelePadtationMain.configManager().usesStandard();
	}
	
	public int fuelUses() {
		return TelePadtationMain.configManager().fuelUsesStandard();
	}
}