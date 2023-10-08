package me.DMan16.TelePadtation.Managers;

import me.DMan16.TelePadtation.Classes.AbstractConfigManager;
import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.Classes.Pair;
import me.DMan16.TelePadtation.Enums.SortType;
import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class LanguageManager extends AbstractConfigManager {
	private static final @NotNull String INFINITY = "\u221e";
	private static final @NotNull String PLACEHOLDER_STATUS = "<status>";
	private static final @NotNull String PLACEHOLDER_WORLD = "<world>";
	private static final @NotNull String PLACEHOLDER_COORDINATES = "<coordinates>";
	private static final @NotNull String PLACEHOLDER_COORDS = "<coords>";
	private static final @NotNull String PLACEHOLDER_X = "<x>";
	private static final @NotNull String PLACEHOLDER_Y = "<y>";
	private static final @NotNull String PLACEHOLDER_Z = "<z>";
	private static final @NotNull String PLACEHOLDER_USES_MAX = "<uses_max>";
	private static final @NotNull String PLACEHOLDER_USES_LEFT = "<uses_left>";
	private static final @NotNull String PLACEHOLDER_LIMIT = "<limit>";
	private static final @NotNull String PLACEHOLDER_OWNED = "<owned>";
	private static final @NotNull String PLACEHOLDER_PAGES = "<pages>";
	private static final @NotNull String PLACEHOLDER_TYPE = "<type>";
	private static final @NotNull String PLACEHOLDER_PLAYER = "<player>";
	
	private final @NotNull String nameTelePadBasic;
	private final @NotNull String nameTelePadStandard;
	private final @NotNull String nameTelePadAdvanced;
	private final @NotNull String nameTelePadPocket;
	private final @Nullable List<@NotNull String> loreTelePadBasic;
	private final @Nullable List<@NotNull String> loreTelePadStandard;
	private final @Nullable List<@NotNull String> loreTelePadAdvanced;
	private final @Nullable List<@NotNull String> loreTelePadPocket;
	private String titlePortable;
	private String titleGlobal;
	private String titlePrivate;
	private String statusGlobal;
	private String statusPortable;
	private String statusActive;
	private String statusInactive;
	private String statusObstructed;
	private List<@NotNull String> loreGlobal;
	private List<@NotNull String> lorePortable;
	private List<@NotNull String> lorePrivate;
	private String nameClose;
	private String nameBack;
	private List<@NotNull String> loreBack;
	private String nameEdit;
	private List<@NotNull String> loreEdit;
	private String nameNext;
	private List<@NotNull String> loreNext;
	private String namePrevious;
	private List<@NotNull String> lorePrevious;
	private String nameSortAll;
	private String nameSortPrivate;
	private String nameSortGlobal;
	private List<@NotNull String> loreSortAll;
	private List<@NotNull String> loreSortPrivate;
	private List<@NotNull String> loreSortGlobal;
	private String nameChangePrivate;
	private String nameChangeGlobal;
	private List<@NotNull String> loreChangePrivate;
	private List<@NotNull String> loreChangeGlobal;
	private String nameFuel;
	private List<@NotNull String> loreFuel;
	private String nameDisplayname;
	private List<@NotNull String> loreDisplayname;
	private String nameRemove;
	private List<@NotNull String> loreRemove;
	private String nameConfirm;
	private List<@NotNull String> loreConfirm;
	private String limitReached;
	private String nearbyError;
	private String telePadCreated;
	private String telePadRemovedPrivate;
	private String telePadRemovedGlobal;
	private String telePadRemovedOther;
	private String enterDisplayname;
	private String errorDatabase;
	private String usage;
	private String playerOnly;
	private String typeNotFound;
	private String playerNotFound;
	private String playerGive;
	private String playerFullInventory;
	private String ownedLimitSelf;
	private String ownedLimitOther;
	
	public LanguageManager() throws IOException {
		super(TelePadtationMain.getInstance(),"messages.yml");
		this.nameTelePadBasic = getString("telepad.name.basic");
		this.nameTelePadStandard = getString("telepad.name.standard");
		this.nameTelePadAdvanced = getString("telepad.name.advanced");
		this.nameTelePadPocket = getString("telepad.name.pocket");
		this.loreTelePadBasic = getLines("telepad.lore.basic");
		this.loreTelePadStandard = getLines("telepad.lore.standard");
		this.loreTelePadAdvanced = getLines("telepad.lore.advanced");
		this.loreTelePadPocket = getLines("telepad.lore.pocket");
	}
	
	protected void load() {
		this.titlePortable = getString("menu.title.portable");
		this.titleGlobal = getString("menu.title.global");
		this.titlePrivate = getString("menu.title.private");
		this.statusGlobal = getString("menu.telepad.status.global");
		this.statusPortable = getString("menu.telepad.status.portable");
		this.statusActive = getString("menu.telepad.status.active");
		this.statusInactive = getString("menu.telepad.status.inactive");
		this.statusObstructed = getString("menu.telepad.status.obstructed");
		this.loreGlobal = addOrSetLine(getLines("menu.telepad.lore.global"),PLACEHOLDER_STATUS);
		this.lorePortable = addOrSetLine(getLines("menu.telepad.lore.portable"),PLACEHOLDER_STATUS);
		this.lorePrivate = addOrSetLine(getLines("menu.telepad.lore.private"),PLACEHOLDER_STATUS);
		this.nameClose = getString("menu.button.close");
		this.nameBack = getString("menu.button.back.name");
		this.loreBack = getLines("menu.button.back.lore");
		this.nameEdit = getString("menu.button.edit.name");
		this.loreEdit = getLines("menu.button.edit.lore");
		this.nameNext = getString("menu.button.next.name");
		this.loreNext = getLines("menu.button.next.lore");
		this.namePrevious = getString("menu.button.previous.name");
		this.lorePrevious = getLines("menu.button.previous.lore");
		this.nameSortAll = getString("menu.button.sort.name.all");
		this.nameSortPrivate = getString("menu.button.sort.name.private");
		this.nameSortGlobal = getString("menu.button.sort.name.global");
		this.loreSortAll = getLines("menu.button.sort.lore.all");
		this.loreSortPrivate = getLines("menu.button.sort.lore.private");
		this.loreSortGlobal = getLines("menu.button.sort.lore.global");
		this.nameChangePrivate = getString("menu.button.change-type.name.private");
		this.nameChangeGlobal = getString("menu.button.change-type.name.global");
		this.loreChangePrivate = getLines("menu.button.change-type.lore.private");
		this.loreChangeGlobal = getLines("menu.button.change-type.lore.global");
		this.nameFuel = getString("menu.button.fuel.name");
		this.loreFuel = getLines("menu.button.fuel.lore");
		this.nameDisplayname = getString("menu.button.displayname.name");
		this.loreDisplayname = getLines("menu.button.displayname.lore");
		this.nameRemove = getString("menu.button.remove.name");
		this.loreRemove = getLines("menu.button.remove.lore");
		this.nameConfirm = getString("menu.button.confirm.name");
		this.loreConfirm = getLines("menu.button.confirm.lore");
		this.limitReached = join(getLines("message.limit-reached"));
		this.nearbyError = join(getLines("message.nearby-error"));
		this.telePadCreated = join(getLines("message.telepad-created"));
		this.telePadRemovedPrivate = join(getLines("message.telepad-removed.private"));
		this.telePadRemovedGlobal = join(getLines("message.telepad-removed.global"));
		this.telePadRemovedOther = join(getLines("message.telepad-removed.other"));
		this.enterDisplayname = join(getLines("message.enter-displayname"));
		if (Utils.isNullOrEmpty(this.enterDisplayname)) this.enterDisplayname = join(getDefaultLines("message.enter-displayname"));
		this.errorDatabase = join(getLines("message.error-database"));
		this.usage = join(getLines("message.usage"));
		if (this.usage == null) this.usage = "";
		this.playerOnly = join(getLines("message.player-only"));
		this.typeNotFound = join(getLines("message.type-not-found"));
		this.playerNotFound = join(getLines("message.player-not-found"));
		this.playerGive = join(getLines("message.player-give"));
		this.playerFullInventory = join(getLines("message.player-full-inventory"));
		this.ownedLimitSelf = join(getLines("message.owned-limit.self"));
		if (Utils.isNullOrEmpty(this.ownedLimitSelf)) this.ownedLimitSelf = join(getDefaultLines("message.owned-limit.self"));
		this.ownedLimitOther = join(getLines("message.owned-limit.self.other"));
		if (Utils.isNullOrEmpty(this.ownedLimitOther)) this.ownedLimitOther = join(getDefaultLines("message.owned-limit.other"));
	}
	
	@Nullable
	private List<@NotNull String> getDefaultLines(@NotNull String option) {
		List<?> list = defaults().getList(option);
		return list == null ? Utils.applyNotNull(Utils.applyNotNull(defaults().getString(option),Utils::chatColors),Collections::singletonList) : (list.isEmpty() ? null : Utils.chatColors(list.stream().map(line -> line == null ? "" : line.toString()).collect(Collectors.toList())));
	}
	
	@NotNull
	public String nameTelePadBasic() {
		return nameTelePadBasic;
	}
	
	@NotNull
	public String nameTelePadStandard() {
		return nameTelePadStandard;
	}
	
	@NotNull
	public String nameTelePadAdvanced() {
		return nameTelePadAdvanced;
	}
	
	@NotNull
	public String nameTelePadPocket() {
		return nameTelePadPocket;
	}
	
	@Nullable
	public List<@NotNull String> loreTelePadBasic() {
		return Utils.applyNotNull(loreTelePadBasic,l -> replace(new ArrayList<>(l),new Pair<>(PLACEHOLDER_USES_MAX,TelePadtationMain.configManager().usesBasic())));
	}
	
	@Nullable
	public List<@NotNull String> loreTelePadStandard() {
		return Utils.applyNotNull(loreTelePadStandard,l -> replace(new ArrayList<>(l),new Pair<>(PLACEHOLDER_USES_MAX,TelePadtationMain.configManager().usesStandard())));
	}
	
	@Nullable
	public List<@NotNull String> loreTelePadAdvanced() {
		return Utils.applyNotNull(loreTelePadAdvanced,l -> replace(new ArrayList<>(l),new Pair<>(PLACEHOLDER_USES_MAX,TelePadtationMain.configManager().usesAdvanced())));
	}
	
	@Nullable
	public List<@NotNull String> loreTelePadPocket() {
		return Utils.applyNotNull(loreTelePadPocket,ArrayList::new);
	}
	
	@NotNull
	public String titleMenu(@NotNull TelePad telePad) {
		if (telePad.isPortable()) return titlePortable;
		if (telePad.isPrivate()) return titlePrivate;
		String title;
		return !TelePadtationMain.configManager().nameAsTitle() || (title = telePad.name()) == null ? titleGlobal : title;
	}
	
	@NotNull
	public List<@NotNull String> loreMenu(@NotNull TelePad telePad,@NotNull TelePadStatus status) {
		BlockLocation location = telePad.location();
		String coords = "(" + location.x() + "," + location.y() + "," + location.z() + ")";
		String statusText;
		if (telePad.isPortable()) statusText = statusPortable;
		else if (telePad.isGlobal()) statusText = statusGlobal;
		else if (status == TelePadStatus.ACTIVE) statusText = statusActive;
		else if (status == TelePadStatus.OBSTRUCTED) statusText = statusObstructed;
		else statusText = statusInactive;
		return replace(telePad.isPortable() ? lorePortable : (telePad.isGlobal() ? loreGlobal : lorePrivate),
				new Pair<>(PLACEHOLDER_STATUS,statusText),
				new Pair<>(PLACEHOLDER_WORLD,location.world().getName()),
				new Pair<>(PLACEHOLDER_COORDINATES,coords),
				new Pair<>(PLACEHOLDER_COORDS,coords),
				new Pair<>(PLACEHOLDER_X,location.x()),
				new Pair<>(PLACEHOLDER_Y,location.y()),
				new Pair<>(PLACEHOLDER_Z,location.z()),
				new Pair<>(PLACEHOLDER_USES_MAX,telePad.isGlobal() ? INFINITY : telePad.usesMax()),
				new Pair<>(PLACEHOLDER_USES_LEFT,telePad.isGlobal() ? INFINITY : telePad.usesLeft())
		);
	}
	
	@NotNull
	public String nameClose() {
		return nameClose;
	}
	
	@NotNull
	public String nameBack() {
		return nameBack;
	}
	
	@Nullable
	public List<@NotNull String> loreBack() {
		return loreBack;
	}
	
	@NotNull
	public String nameEdit() {
		return nameEdit;
	}
	
	@Nullable
	public List<@NotNull String> loreEdit() {
		return loreEdit;
	}
	
	@NotNull
	public String nameNext() {
		return nameNext;
	}
	
	@Nullable
	public List<@NotNull String> loreNext() {
		return Utils.applyNotNull(loreNext,lore -> replace(lore,new Pair<>(PLACEHOLDER_PAGES,TelePadtationMain.configManager().rightClickJump())));
	}
	
	@NotNull
	public String namePrevious() {
		return namePrevious;
	}
	
	@Nullable
	public List<@NotNull String> lorePrevious() {
		return Utils.applyNotNull(lorePrevious,lore -> replace(lore,new Pair<>(PLACEHOLDER_PAGES,TelePadtationMain.configManager().rightClickJump())));
	}
	
	@NotNull
	public String nameSort(@NotNull SortType sortType) {
		return sortType == SortType.ALL ? nameSortAll : (sortType == SortType.GLOBAL ? nameSortGlobal : nameSortPrivate);
	}
	
	@Nullable
	public List<@NotNull String> loreSort(@NotNull SortType sortType) {
		return Utils.applyNotNull(sortType == SortType.ALL ? loreSortAll : (sortType == SortType.GLOBAL ? loreSortGlobal : loreSortPrivate),ArrayList::new);
	}
	
	@NotNull
	public String nameChange(boolean isGlobal) {
		return isGlobal ? nameChangeGlobal : nameChangePrivate;
	}
	
	@Nullable
	public List<@NotNull String> loreChange(boolean isGlobal) {
		return isGlobal ? loreChangeGlobal : loreChangePrivate;
	}
	
	public void limitReached(@NotNull Player player,long limit) {
		if (limitReached != null) player.sendMessage(replace(limitReached,new Pair<>(PLACEHOLDER_LIMIT,limit)));
	}
	
	public void nearbyError(@NotNull Player player) {
		Utils.runNotNull(nearbyError,player::sendMessage);
	}
	
	private void telePadAction(@Nullable String msg,@NotNull Player player,@NotNull TelePad telePad,@Nullable Long owned,@Nullable Long limit,boolean uses) {
		if (msg == null) return;
		BlockLocation location = telePad.location();
		String coords = "(" + location.x() + "," + location.y() + "," + location.z() + ")";
		msg = replace(msg,
				new Pair<>(PLACEHOLDER_WORLD,location.world().getName()),
				new Pair<>(PLACEHOLDER_COORDINATES,coords),
				new Pair<>(PLACEHOLDER_COORDS,coords),
				new Pair<>(PLACEHOLDER_X,location.x()),
				new Pair<>(PLACEHOLDER_Y,location.y()),
				new Pair<>(PLACEHOLDER_Z,location.z())
		);
		if (owned != null) msg = replace(msg,new Pair<>(PLACEHOLDER_OWNED,owned));
		if (limit != null) msg = replace(msg,new Pair<>(PLACEHOLDER_LIMIT,limit));
		if (uses) msg = replace(msg,new Pair<>(PLACEHOLDER_USES_MAX,telePad.usesMax()));
		player.sendMessage(msg);
	}
	
	public void telePadCreated(@NotNull Player player,@NotNull TelePad telePad,long owned,long limit) {
		telePadAction(telePadCreated,player,telePad,owned,limit,true);
	}
	
	public void telePadRemoved(@NotNull Player player,@NotNull TelePad telePad,@Nullable Pair<@NotNull Long,@NotNull Long> info) {
		telePadAction(info == null ? (telePad.isGlobal() ? telePadRemovedGlobal : telePadRemovedOther) : telePadRemovedPrivate,player,telePad,Utils.applyNotNull(info,Pair::first),Utils.applyNotNull(info,Pair::second),false);
	}
	
	@NotNull
	public String nameFuel() {
		return nameFuel;
	}
	
	@Nullable
	public List<@NotNull String> loreFuel() {
		return loreFuel;
	}
	
	@NotNull
	public String nameDisplayname() {
		return nameDisplayname;
	}
	
	@Nullable
	public List<@NotNull String> loreDisplayname() {
		return loreDisplayname;
	}
	
	@NotNull
	public String nameRemove() {
		return nameRemove;
	}
	
	@Nullable
	public List<@NotNull String> loreRemove() {
		return loreRemove;
	}
	
	@NotNull
	public String nameConfirm() {
		return nameConfirm;
	}
	
	@Nullable
	public List<@NotNull String> loreConfirm() {
		return loreConfirm;
	}
	
	@NotNull
	public String enterDisplayname() {
		return enterDisplayname;
	}
	
	public void errorDatabase(@NotNull Player player) {
		Utils.runNotNull(errorDatabase,player::sendMessage);
	}
	
	@NotNull
	public String usage() {
		return usage;
	}
	
	public void playerOnly(@NotNull CommandSender sender) {
		Utils.runNotNull(playerOnly,sender::sendMessage);
	}
	
	public void typeNotFound(@NotNull CommandSender sender,@NotNull String type) {
		if (typeNotFound != null) sender.sendMessage(replace(typeNotFound,new Pair<>(PLACEHOLDER_TYPE,type)));
	}
	
	public void playerNotFound(@NotNull CommandSender sender,@NotNull String player) {
		if (playerNotFound != null) sender.sendMessage(replace(playerNotFound,new Pair<>(PLACEHOLDER_PLAYER,player)));
	}
	
	public void playerGive(@NotNull CommandSender sender,@NotNull String player,@NotNull TelePadItem<?> telePadItem) {
		if (playerGive != null) sender.sendMessage(replace(playerGive,new Pair<>(PLACEHOLDER_PLAYER,player),new Pair<>(PLACEHOLDER_TYPE,telePadItem.type())));
	}
	
	public void playerFullInventory(@NotNull CommandSender sender,@NotNull String player,@NotNull TelePadItem<?> telePadItem) {
		if (playerFullInventory != null) sender.sendMessage(replace(playerFullInventory,new Pair<>(PLACEHOLDER_PLAYER,player),new Pair<>(PLACEHOLDER_TYPE,telePadItem.type())));
	}
	
	public void ownedLimit(@NotNull CommandSender sender,@NotNull Player player) {
		boolean self = (sender instanceof Player) && ((Player) sender).getUniqueId().equals(player.getUniqueId());
		long owned;
		try {
			owned = TelePadtationMain.databaseConnection().owned(player);
		} catch (SQLException e) {
			errorDatabase(player);
			return;
		}
		long limit = TelePadtationMain.configManager().limit(player);
		String msg = replace(self ? ownedLimitSelf : ownedLimitOther,new Pair<>(PLACEHOLDER_OWNED,owned),new Pair<>(PLACEHOLDER_LIMIT,limit));
		if (!self) msg = replace(msg,new Pair<>(PLACEHOLDER_PLAYER,player.getName()));
		sender.sendMessage(msg);
	}
}