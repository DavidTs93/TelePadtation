package me.DMan16.TelePadtation.Managers;

import me.DMan16.TelePadtation.Classes.AbstractConfigManager;
import me.DMan16.TelePadtation.Enums.Heads;
import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConfigManager extends AbstractConfigManager {
	private @NotNull final String headPocket;
	private boolean fix;
	private boolean removeBroken;
	private int limitBase;
	private int limitPermission;
	private @Nullable Set<@NotNull String> limitPermissions;
	private int usesBasic;
	private int usesStandard;
	private int usesAdvanced;
	private boolean fuelPure;
	private int fuelUsesBasic;
	private int fuelUsesStandard;
	private int fuelUsesAdvanced;
	private boolean destinationConsumeUse;
	private boolean destinationAllowInactive;
	private boolean nameAsTitle;
	private int rightClickJump;
	private String skinEdit;
	private String skinGeneralGlobal;
	private String skinGeneralPrivate;
	private String skinIndividualGlobal;
	private String skinIndividualActive;
	private String skinIndividualObstructed;
	private String skinIndividualInactive;
	
	public ConfigManager() {
		super(TelePadtationMain.getInstance());
		this.headPocket = getSkin("telepad.display-head-pocket",Heads.PORTABLE,true);
	}
	
	protected void load() {
		TelePadtationMain.getInstance().reloadConfig();
		FileConfiguration config = TelePadtationMain.getInstance().getConfig();
		this.fix = config.getBoolean("telepad.fix",true);
		this.removeBroken = config.getBoolean("telepad.remove-broken",true);
		this.limitBase = Math.max(config.getInt("telepad.limit.base",2),0);
		this.limitPermission = Math.max(config.getInt("telepad.limit.per-permission",1),1);
		List<?> list = config.getList("telepad.limit.permissions",null);
		if (Utils.isNullOrEmpty(list)) this.limitPermissions = null;
		else {
			this.limitPermissions = list.stream().filter(Objects::nonNull).map(Object::toString).map(String::trim).map(Utils::toLowercase).collect(Collectors.toSet());
			if (this.limitPermissions.isEmpty()) this.limitPermissions = null;
		}
		this.usesBasic = Math.max(config.getInt("telepad.uses.basic",4),1);
		this.usesStandard = Math.max(config.getInt("telepad.uses.standard",this.usesBasic * 2),this.usesBasic + 1);
		this.usesAdvanced = Math.max(config.getInt("telepad.uses.advanced",this.usesStandard * 2),this.usesStandard + 1);
		this.fuelPure = config.getBoolean("telepad.fuel.pure",false);
		this.fuelUsesBasic = Math.max(config.getInt("telepad.fuel.uses.basic",1000),1);
		this.fuelUsesStandard = Math.max(config.getInt("telepad.fuel.uses.standard",1000),1);
		this.fuelUsesAdvanced = Math.max(config.getInt("telepad.fuel.uses.advanced",1000),1);
		this.destinationConsumeUse = config.getBoolean("telepad.destination.consume-use",false);
		this.destinationAllowInactive = config.getBoolean("telepad.destination.allow-inactive",false);
		this.nameAsTitle = config.getBoolean("menu.name-as-title",true);
		this.rightClickJump = Math.max(config.getInt("menu.right-click-jump",5),1);
		this.skinEdit = getSkin("menu.display-head.general.global",Heads.EDIT,false);
		this.skinGeneralGlobal = getSkin("menu.display-head.general.global",Heads.GLOBAL,false);
		this.skinGeneralPrivate = getSkin("menu.display-head.general.private",Heads.ACTIVE,false);
		this.skinIndividualGlobal = getSkin("menu.display-head.individual.global",Heads.GLOBAL,true);
		this.skinIndividualActive = getSkin("menu.display-head.individual.private.active",Heads.ACTIVE,true);
		this.skinIndividualObstructed = getSkin("menu.display-head.individual.private.obstructed",Heads.OBSTRUCTED,true);
		this.skinIndividualInactive = getSkin("menu.display-head.individual.private.inactive",Heads.INACTIVE,true);
	}
	
	@NotNull
	private String getSkin(@NotNull String option,@NotNull Heads defaultValue,boolean allowEmpty) {
		String str = config().getString(option,null);
		if (str == null) return defaultValue.skin();
		if (allowEmpty && Utils.isNullOrEmpty(str)) return "";
		return Heads.isSkin(str) ? str : defaultValue.skin();
	}
	
	@NotNull
	public String headPocket() {
		return headPocket;
	}
	
	@NotNull
	private TelePadState fixOrRemoveTelePadState(@NotNull Block block) {
		if (block.getType() == TelePad.MATERIAL_BLOCK) return TelePadState.PERFECT;
		if (fix) {
			block.setType(TelePad.MATERIAL_BLOCK);
			return TelePadState.FIXED;
		}
		return removeBroken ? TelePadState.REMOVE : TelePadState.BROKEN;
	}
	
	@NotNull
	public List<TelePad.@NotNull TelePadPlaceable> fixOrShouldRemove(@NotNull Collection<TelePad.@NotNull TelePadPlaceable> telePads) {
		return telePads.stream().filter(telePad -> fixOrRemoveTelePadState(telePad.location().toBlock()) == TelePadState.REMOVE).collect(Collectors.toList());
	}
	
	public void fixOrOk(@NotNull Block block,@NotNull TelePad telePad,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadState state = fixOrRemoveTelePadState(block);
		if (state == TelePadState.PERFECT || state == TelePadState.FIXED) Utils.runNotNull(onSuccess);
		else if (removeBroken) TelePadtationMain.databaseConnection().remove(telePad,null,Utils.applyNotNull(onFail,o -> owned -> o.run()),onFail);
	}
	
	public long limit(@NotNull Player player) {
		long limit = limitBase;
		if (limitPermissions != null) limit += (limitPermissions.stream().filter(player::hasPermission).count() * limitPermission);
		return limit;
	}
	
	public int usesBasic() {
		return usesBasic;
	}
	
	public int usesStandard() {
		return usesStandard;
	}
	
	public int usesAdvanced() {
		return usesAdvanced;
	}
	
	public boolean fuelPure() {
		return fuelPure;
	}
	
	public int fuelUsesBasic() {
		return fuelUsesBasic;
	}
	
	public int fuelUsesStandard() {
		return fuelUsesStandard;
	}
	
	public int fuelUsesAdvanced() {
		return fuelUsesAdvanced;
	}
	
	public boolean destinationConsumeUse() {
		return destinationConsumeUse;
	}
	
	public boolean destinationAllowInactive() {
		return destinationAllowInactive;
	}
	
	public boolean nameAsTitle() {
		return nameAsTitle;
	}
	
	public int rightClickJump() {
		return rightClickJump;
	}
	
	@NotNull
	public String skinEdit() {
		return skinEdit;
	}
	
	@NotNull
	public String skinGeneralGlobal() {
		return skinGeneralGlobal;
	}
	
	@NotNull
	public String skinGeneralPrivate() {
		return skinGeneralPrivate;
	}
	
	@NotNull
	public ItemStack itemMenu(@NotNull TelePad telePad,@NotNull TelePadStatus status) {
		String name = Objects.requireNonNull(TelePadtationMain.TelePadsManager().get(telePad.type())).displayName();
		String skin;
		if (status == TelePadStatus.PORTABLE) skin = ((TelePad.TelePadPortable) telePad).skin();
		else if (status == TelePadStatus.GLOBAL) skin = skinIndividualGlobal;
		else if (status == TelePadStatus.ACTIVE) skin = skinIndividualActive;
		else if (status == TelePadStatus.OBSTRUCTED) skin = skinIndividualObstructed;
		else skin = skinIndividualInactive;
		ItemStack item;
		if (skin.isEmpty()) item = new ItemStack(Material.END_PORTAL_FRAME);
		else {
			item = new ItemStack(Material.PLAYER_HEAD);
			item.setItemMeta(Heads.setSkin((SkullMeta) Objects.requireNonNull(item.getItemMeta()),skin));
		}
		return Utils.setDisplayNameLore(item,name,TelePadtationMain.languageManager().loreMenu(telePad,status));
	}
	
	private enum TelePadState {
		PERFECT,
		FIXED,
		REMOVE,
		BROKEN
	}
}