package me.DMan16.TelePadtation.TelePads;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.Events.TelePadPostTeleportEvent;
import me.DMan16.TelePadtation.Events.TelePadPreTeleportEvent;
import me.DMan16.TelePadtation.Menus.TelePadMenuEdit;
import me.DMan16.TelePadtation.Menus.TelePadMenuPlaced;
import me.DMan16.TelePadtation.Menus.TelePadMenuPortable;
import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class TelePad {
	public static final @NotNull String PERMISSION_REMOVE_PRIVATE = "telepadtation.remove";
	public static final @NotNull Material MATERIAL_BLOCK = Material.END_PORTAL_FRAME;
	public static final @NotNull Material MATERIAL_FUEL = Material.ENDER_PEARL;
	private static final @NotNull ItemStack ITEM_FUEL = new ItemStack(MATERIAL_FUEL);
	public static final int SLOTS_FILL = 5;
	public static final int AMOUNT_FILL = 16 * SLOTS_FILL;
	
	private final @NotNull String type;
	private final @NotNull UUID ownerID;
	private final @NotNull BlockLocation location;
	private int used;
	private @Range(from = 0,to = AMOUNT_FILL) int extraFuel;
	private boolean isGlobal;
	private String name;
	private boolean removed;
	
	private TelePad(@NotNull String type,@NotNull UUID ownerID,@NotNull BlockLocation location,int used,@Range(from = 0,to = AMOUNT_FILL) int extraFuel,boolean isGlobal,@Nullable String name) {
		if (!(this instanceof TelePadPortable) && !(this instanceof TelePadPlaceable)) throw new RuntimeException("TelePad extend either TelePadPortable or TelePadPlaceable");
		this.type = Objects.requireNonNull(Utils.fixKey(type));
		this.ownerID = ownerID;
		this.location = location;
		this.used = used;
		this.extraFuel = extraFuel;
		this.isGlobal = isGlobal;
		this.name = name;
	}
	
	@Contract("null -> false")
	public boolean isFuel(@Nullable ItemStack item) {
		if (Utils.isNull(item) || item.getType() != MATERIAL_FUEL) return false;
		if (!TelePadtationMain.configManager().fuelPure()) item = Utils.setDisplayNameLore(item.clone(),Utils.supplier(null),null);
		return ITEM_FUEL.isSimilar(item);
	}
	
	@NotNull
	@Contract(" -> new")
	public ItemStack defaultFuel() {
		return ITEM_FUEL.clone();
	}
	
	@NotNull
	public String type() {
		return type;
	}
	
	@NotNull
	public final UUID ownerID() {
		return ownerID;
	}
	
	public final boolean isOwner(@NotNull Player player) {
		return ownerID().equals(player.getUniqueId());
	}
	
	@NotNull
	public final BlockLocation location() {
		return location;
	}
	
	public final int used() {
		return used;
	}
	
	protected final void used(int used) {
		this.used = used;
	}
	
	
	protected void usedDatabase(int newUsed,@NotNull Runnable run) {
		TelePadtationMain.databaseConnection().use(this,newUsed,() -> {
			used(newUsed);
			run.run();
		});
	}
	
	@Range(from = 0,to = AMOUNT_FILL)
	public final int extraFuel() {
		return extraFuel;
	}
	
	protected final void extraFuel(@Range(from = 0,to = AMOUNT_FILL) int extraFuel) {
		this.extraFuel = extraFuel;
	}
	
	protected void extraFuelDatabase(@Range(from = 0,to = AMOUNT_FILL) int extraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.databaseConnection().setExtraFuel(this,extraFuel,() -> {
			extraFuel(extraFuel);
			Utils.runNotNull(onSuccess);
		},onFail);
	}
	
	public final void setExtraFuel(int extraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		if (extraFuel < 0) extraFuel = 0;
		else if (extraFuel > AMOUNT_FILL) extraFuel = AMOUNT_FILL;
		extraFuelDatabase(extraFuel,onSuccess,onFail);
	}
	
	public abstract int usesMax();
	
	public final int usesLeft() {
		return Math.max(usesMax() - used(),0);
	}
	
	public abstract int fuelUses();
	
	public boolean isValid() {
		return location().toLocation().getBlock().getType() == MATERIAL_BLOCK;
	}
	
	public boolean isActive() {
		return isFree() || usesLeft() > 0;
	}
	
	public boolean canAccess(@NotNull Player player) {
		return isGlobal() || isOwner(player);
	}
	
	protected void rechargeDatabase(int newUsed,int newExtraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.databaseConnection().recharge(this,newUsed,newExtraFuel,() -> {
			used(newUsed);
			extraFuel(newExtraFuel);
			Utils.runNotNull(onSuccess);
		},onFail);
	}
	
	protected final void recharge() {
		recharge(null,null);
	}
	
	public final void recharge(@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		if (isActive()) {
			Utils.runNotNull(onSuccess);
			return;
		}
		if (used() > usesMax()) {
			used(usesMax());
			recharge(onSuccess,onFail);
			return;
		}
		if (extraFuel() == 0) {
			Utils.runNotNull(onFail);
			return;
		}
		int newExtraFuel = extraFuel();
		int newUsed = used();
		int fuelUses = fuelUses();
		while (newExtraFuel > 0 && newUsed > 0) {
			newExtraFuel--;
			newUsed -= fuelUses;
		}
		rechargeDatabase(Math.max(newUsed,0),newExtraFuel,onSuccess,onFail);
	}
	
	public final boolean isGlobal() {
		return isGlobal;
	}
	
	public final boolean isPrivate() {
		return !isGlobal();
	}
	
	public abstract boolean isPortable();
	
	public abstract boolean isFree();
	
	protected void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
	
	protected void setGlobalDatabase(@NotNull Player player,boolean newIsGlobal,@Nullable Runnable onSuccess,@Nullable Consumer<@NotNull Long> onFailLimit,@Nullable Runnable onFailDatabase) {
		TelePadtationMain.databaseConnection().setGlobal(this,player,newIsGlobal,() -> {
			setGlobal(newIsGlobal);
			Utils.runNotNull(onSuccess);
		},onFailLimit,onFailDatabase);
	}
	
	public final void toggleGlobal(@NotNull Player player,@Nullable Runnable onSuccess,@Nullable Consumer<@NotNull Long> onFailLimit,@Nullable Runnable onFailDatabase) {
		setGlobalDatabase(player,!isGlobal,onSuccess,onFailLimit,onFailDatabase);
	}
	
	@Nullable
	public final String name() {
		return Utils.chatColors(name);
	}
	
	protected final void name(@Nullable String name) {
		this.name = name;
	}
	
	protected void setNameDatabase(@Nullable String name,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		String newName = Utils.isNullOrEmpty(name) || Utils.isNullOrEmpty(ChatColor.stripColor(Utils.chatColors(name))) ? null : name;
		TelePadtationMain.databaseConnection().setName(this,newName,() -> {
			name(newName);
			Utils.runNotNull(onSuccess);
		},onFail);
	}
	
	public final void setName(@Nullable String name,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		setNameDatabase(name,onSuccess,onFail);
	}
	
	public boolean removed() {
		return this.removed;
	}
	
	public void setRemoved() {
		this.removed = true;
	}
	
	public static boolean isAllowedRightAboveTelePad(@NotNull Material material) {
		return Tag.CARPETS.isTagged(material);
	}
	
	public static boolean canBlockTelePadTeleport(@NotNull BlockLocation blockTelePad) {
		Block block = blockTelePad.toBlock().getRelative(BlockFace.UP);
		if (!block.isEmpty() && !isAllowedRightAboveTelePad(block.getType())) return false;
		return block.getRelative(BlockFace.UP).isEmpty();
	}
	
	protected abstract boolean testTeleportTo(boolean consumeUse,boolean allowInactive);
	
	public final boolean testTeleportTo() {
		return testTeleportTo(TelePadtationMain.configManager().destinationConsumeUse(),TelePadtationMain.configManager().destinationAllowInactive());
	}
	
	@NotNull
	public final TelePadStatus status() {
		if (this instanceof TelePadPortable) return TelePadStatus.PORTABLE;
		if (isGlobal()) return TelePadStatus.GLOBAL;
		if (!isActive()) return TelePadStatus.INACTIVE;
		return testTeleportTo() ? TelePadStatus.ACTIVE : TelePadStatus.OBSTRUCTED;
	}
	
	boolean teleportPlayer(@NotNull Player player,@NotNull Location location) {
		return player.teleport(location);
	}
	
	public <V extends TelePad> void teleport(@NotNull V destination,@NotNull Player player) {
		boolean destinationConsumeUse = TelePadtationMain.configManager().destinationConsumeUse();
		if (!isActive() || !destination.isActive() || !destination.testTeleportTo(destinationConsumeUse,TelePadtationMain.configManager().destinationAllowInactive())) return;
		if (!Utils.callEventCancellable(new TelePadPreTeleportEvent<>(this,destination,player))) return;
		Location playerLoc = player.getLocation(),loc = destination.location().toLocation().add(0.5,1.1,0.5);
		loc.setYaw(playerLoc.getYaw());
		loc.setPitch(playerLoc.getPitch());
		if (!teleportPlayer(player,loc)) return;
		Runnable run = () -> Utils.callEvent(new TelePadPostTeleportEvent<>(this,destination,player));
		if (!isFree()) usedDatabase(used() + 1,() -> {
			recharge();
			if (destinationConsumeUse && !destination.isFree()) destination.usedDatabase(destination.used() + 1,destination::recharge);
			run.run();
		});
		else if (destinationConsumeUse && !destination.isFree()) destination.usedDatabase(destination.used() + 1,() -> {
			destination.recharge();
			run.run();
		});
		else run.run();
	}
	
	public abstract void access(@NotNull Player player,@NotNull EquipmentSlot hand);
	
	@Override
	public int hashCode() {
		return location().hashCode();
	}
	
	public static abstract class TelePadPlaceable extends TelePad {
		protected TelePadPlaceable(@NotNull String key,@NotNull Player player,@NotNull BlockLocation location) {
			super(key,player.getUniqueId(),location,0,0,false,null);
		}
		
		protected TelePadPlaceable(@NotNull String key,@NotNull UUID ownerID,@NotNull BlockLocation location,int used,@Range(from = 0,to = AMOUNT_FILL) int extraFuel,boolean isGlobal,@Nullable String name) {
			super(key,ownerID,location,used,extraFuel,isGlobal,name);
		}
		
		protected boolean testTeleportTo(boolean consumeUse,boolean allowInactive) {
			if (!isValid() || !canBlockTelePadTeleport(location())) return false;
			if (consumeUse || !allowInactive) return isActive();
			return true;
		}
		
		public boolean canAccessRemove(@NotNull Player player) {
			return !isGlobal() && player.hasPermission(PERMISSION_REMOVE_PRIVATE);
		}
		
		public final boolean isPortable() {
			return false;
		}
		
		public boolean isFree() {
			return isGlobal();
		}
		
		public final void access(@NotNull Player player,@NotNull EquipmentSlot hand) {
			TelePadtationMain.configManager().fixOrOk(location().toBlock(),this,() -> {
				TelePadStatus status = status();
				if (canAccess(player)) {
					if (status == TelePadStatus.OBSTRUCTED) new TelePadMenuPlaced(this,player,status,null);
					else recharge(() -> TelePadtationMain.databaseConnection().getApplicableTelePads(player,telePads -> new TelePadMenuPlaced(this,player,status(),telePads),() -> TelePadtationMain.languageManager().errorDatabase(player)),() -> new TelePadMenuPlaced(this,player,status,null));
				} else if (canAccessRemove(player)) try {
					new TelePadMenuEdit(this,TelePadtationMain.languageManager().titleMenu(this),player,status,null,null);
				} catch (IllegalAccessException e) {}
			},null);
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof TelePadPlaceable) && location().equals(((TelePadPlaceable) obj).location());
		}
	}
	
	public static abstract class TelePadPortable extends TelePad {
		protected TelePadPortable(@NotNull String key,@NotNull Player player) {
			super(key,player.getUniqueId(),new BlockLocation(player.getWorld(),player.getLocation()),0,0,false,null);
		}
		
		public final void access(@NotNull Player player,@NotNull EquipmentSlot hand) {
//			ItemStack item = consumeBeforeOpen(player,hand);
//			if (item != null)
			TelePadtationMain.databaseConnection().getApplicableTelePads(player,telePads -> new TelePadMenuPortable(this,player,telePads),() -> TelePadtationMain.languageManager().errorDatabase(player));
		}
		
		public abstract @NotNull String skin();
		
		public int usesMax() {
			return 1;
		}
		
		public int fuelUses() {
			return 1;
		}
		
		protected final boolean testTeleportTo(boolean consumeUse,boolean allowInactive) {
			return false;
		}
		
		public final boolean isPortable() {
			return true;
		}
		
		public final boolean isFree() {
			return true;
		}
		
		@Override
		protected final void setGlobal(boolean isGlobal) {}
		
		@Override
		protected final void usedDatabase(int newUsed,@NotNull Runnable run) {}
		
		@Override
		protected final void extraFuelDatabase(@Range(from = 0,to = AMOUNT_FILL) int extraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {}
		
		@Override
		protected final void rechargeDatabase(int newUsed,int newExtraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {}
		
		@Override
		protected final void setGlobalDatabase(@NotNull Player player,boolean newIsGlobal,@Nullable Runnable onSuccess,@Nullable Consumer<@NotNull Long> onFailLimit,@Nullable Runnable onFailDatabase) {}
		
		@Override
		protected final void setNameDatabase(@Nullable String name,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {}
		
		@Override
		boolean teleportPlayer(@NotNull Player player,@NotNull Location location) {
			TelePadItem.TelePadItemPortable<?> itemTelePad = TelePadtationMain.TelePadsManager().getPortable(type());
			if (itemTelePad == null) return player.teleport(location);
			ItemStack item = itemTelePad.toItem();
			if (!player.getInventory().removeItem(item).isEmpty()) return false;
			if (player.teleport(location)) return true;
			Utils.givePlayer(player,player.getWorld(),player.getLocation(),item);
			return false;
		}

//		() -> addAfterClose(player,item)
//		protected void addAfterClose(@NotNull Player player,@Nullable ItemStack item) {
//			if (Utils.notNull(item)) player.getInventory().addItem(item);
//		}
		
		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}
}