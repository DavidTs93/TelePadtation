package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.Classes.Pair;
import me.DMan16.TelePadtation.Enums.Heads;
import me.DMan16.TelePadtation.Enums.SortType;
import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.Collectors;

public abstract class TelePadMenuTeleport<T extends TelePad> extends TelePadMenu<T> {
	protected final @Nullable @Unmodifiable List<TelePad.@NotNull TelePadPlaceable> destinations;
	protected final @Nullable @Unmodifiable List<@NotNull @Unmodifiable Map<@NotNull Integer,@NotNull Pair<TelePad.@NotNull TelePadPlaceable,@NotNull TelePadStatus>>> destinationsPrivate;
	protected final @Nullable @Unmodifiable List<@NotNull @Unmodifiable Map<@NotNull Integer,@NotNull Pair<TelePad.@NotNull TelePadPlaceable,@NotNull TelePadStatus>>> destinationsGlobal;
	private @NotNull SortType sortType;
	
	protected TelePadMenuTeleport(@NotNull T telePad,@Range(from = 1,to = 5) int lines,@NotNull String title,@NotNull Player player,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		super(telePad,lines,title,player,status);
		List<Integer> slotsList = Arrays.asList(slotClose(),slotNext(),slotPrevious(),slotInfo(),slotSort());
		Integer slotEdit = slotEdit();
		if (slotEdit != null) {
			slotsList = new ArrayList<>(slotsList);
			slotsList.add(slotEdit);
		}
		if (slotsList.stream().filter(this::legalSlot).distinct().count() != slotsList.size() || !slotsList.stream().allMatch(this::isBorder)) throw new IllegalArgumentException("All TelePadMenu slots must be within the inventory, unique, and on the borders!");
		if (Utils.isNullOrEmpty(destinations)) {
			this.destinations = null;
			this.destinationsPrivate = null;
			this.destinationsGlobal = null;
		} else {
			this.destinations = Collections.unmodifiableList(destinations);
			List<Pair<TelePad.TelePadPlaceable,TelePadStatus>> dests = this.destinations.stream().map(t -> new Pair<>(t,t.status())).collect(Collectors.toList());
			Comparator<Pair<TelePad.TelePadPlaceable,TelePadStatus>> comparator = createComparator(telePad);
			Iterator<Pair<TelePad.TelePadPlaceable,TelePadStatus>> iterPrivate = dests.stream().filter(t -> t.first().isPrivate() && !telePad.equals(t.first())).sorted(comparator).iterator();
			Iterator<Pair<TelePad.TelePadPlaceable,TelePadStatus>> iterGlobal = dests.stream().filter(t -> t.first().isGlobal() && !telePad.equals(t.first())).sorted(comparator).iterator();
			this.destinationsPrivate = !iterPrivate.hasNext() ? null : Collections.unmodifiableList(Utils.generateCompactPages(iterPrivate,Utils::self,null,1,lines,0,8,this::isBorder,null).stream().map(Collections::unmodifiableMap).collect(Collectors.toList()));
			this.destinationsGlobal = !iterGlobal.hasNext() ? null : Collections.unmodifiableList(Utils.generateCompactPages(iterGlobal,Utils::self,null,1,lines,0,8,this::isBorder,null).stream().map(Collections::unmodifiableMap).collect(Collectors.toList()));
		}
		this.sortType = SortType.ALL;
		prepareAndOpen();
	}
	
	@Nullable
	@Unmodifiable
	protected Map<@NotNull Integer,@NotNull Pair<TelePad.@NotNull TelePadPlaceable,@NotNull TelePadStatus>> currentPageContents() {
		boolean privateDestinations = false,globalDestinations = false;
		if (sortType == SortType.ALL) {
			if (destinationsPrivate == null) {
				if (destinationsGlobal == null) return null;
				globalDestinations = true;
			} else {
				privateDestinations = true;
				if (destinationsGlobal != null) globalDestinations = true;
			}
		} else if (sortType == SortType.GLOBAL) {
			if (destinationsGlobal == null) return null;
			globalDestinations = true;
		} else {
			if (destinationsPrivate == null) return null;
			privateDestinations = true;
		}
		int page = currentPage() - 1;
		if (privateDestinations) {
			if (page < destinationsPrivate.size()) return destinationsPrivate.get(page);
			page -= destinationsPrivate.size();
		}
		return globalDestinations && page < destinationsGlobal.size() ? destinationsGlobal.get(page) : null;
	}
	
	@Nullable
	protected Pair<TelePad.@NotNull TelePadPlaceable,@NotNull TelePadStatus> fromSlot(int slot) {
		return isBorder(slot) ? null : Utils.applyNotNull(currentPageContents(),Map::get,slot);
	}
	
	@Nullable
	protected Integer slotEdit() {
		return 0;
	}
	
	protected int slotSort() {
		return 8;
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemEdit() {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		item.setItemMeta(Heads.setSkin((SkullMeta) Objects.requireNonNull(item.getItemMeta()),TelePadtationMain.configManager().skinEdit()));
		return Utils.setDisplayNameLore(item,TelePadtationMain.languageManager().nameEdit(),TelePadtationMain.languageManager().loreEdit());
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemSort() {
		ItemStack item = new ItemStack(sortType == SortType.ALL ? Material.END_PORTAL_FRAME : Material.PLAYER_HEAD);
		if (sortType != SortType.ALL) item.setItemMeta(Heads.setSkin((SkullMeta) Objects.requireNonNull(item.getItemMeta()),sortType == SortType.GLOBAL ? TelePadtationMain.configManager().skinGeneralGlobal() : TelePadtationMain.configManager().skinGeneralPrivate()));
		return Utils.setDisplayNameLore(item,TelePadtationMain.languageManager().nameSort(sortType),TelePadtationMain.languageManager().loreSort(sortType));
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemClose() {
		return Utils.setDisplayNameLore(new ItemStack(Material.BARRIER),TelePadtationMain.languageManager().nameClose(),null);
	}
	
	protected final void setEdit() {
		if (canEdit()) Utils.runNotNull(slotEdit(),slot -> setItem(slot,itemEdit()));
	}
	
	protected final void setSort() {
		setItem(slotSort(),itemSort());
	}
	
	@Override
	protected final void setUpdatingButtons() {
		setSort();
		super.setUpdatingButtons();
	}
	
	@Override
	protected final void setButtons() {
		setEdit();
		super.setButtons();
	}
	
	protected int maxPage() {
		int sizeGlobal = destinationsGlobal == null ? 0 : destinationsGlobal.size(),sizePrivate = destinationsPrivate == null ? 0 : destinationsPrivate.size();
		return Math.max(1,sortType == SortType.ALL ? sizeGlobal + sizePrivate : (sortType == SortType.GLOBAL ? sizeGlobal : sizePrivate));
	}
	
	protected void setPageContents() {
		Utils.runNotNull(currentPageContents(),page -> page.forEach((slot,info) -> setItem(slot,TelePadtationMain.configManager().itemMenu(info.first(),info.second()))));
	}
	
	@NotNull
	protected static Comparator<@NotNull Pair<TelePad.@NotNull TelePadPlaceable,@NotNull TelePadStatus>> createComparator(@NotNull TelePad origin) {
		final BlockLocation loc = origin.location();
		final World world = loc.world();
		return (t1,t2) -> {
			TelePad.TelePadPlaceable telePad1 = t1.first(),telePad2 = t2.first();
			TelePadStatus status1 = t1.second(),status2 = t2.second();
			if (status1 != status2) return Integer.compare(status1.ordinal(),status2.ordinal());
			BlockLocation loc1 = telePad1.location(),loc2 = telePad2.location();
			World world1 = loc1.world(),world2 = loc2.world();
			if (!world.equals(world1)) {
				if (world.equals(world2)) return 1;
				if (!world1.equals(world2)) {
					int cmp = String.CASE_INSENSITIVE_ORDER.compare(world1.getName(),world2.getName());
					if (cmp != 0) return cmp;
				}
			} else if (!world.equals(world2)) return -1;
			int cmp = loc.compareDistance(loc1,loc2);
			return cmp != 0 ? cmp : -1;
		};
	}
	
	protected abstract void handleEdit();
	
	protected void otherSlot(@NotNull InventoryClickEvent event,int slot,ItemStack slotItem,@NotNull ClickType click) {
		if (slot == slotSort()) {
			if (sortType == SortType.ALL) sortType = click.isRightClick() ? SortType.GLOBAL : SortType.PRIVATE;
			else if (sortType == SortType.PRIVATE) sortType = click.isRightClick() ? SortType.ALL : SortType.GLOBAL;
			else sortType = click.isRightClick() ? SortType.PRIVATE : SortType.ALL;
			setPage(1);
			return;
		}
		if (Objects.equals(slot,slotEdit())) {
			handleEdit();
			return;
		}
		Utils.runNotNull(fromSlot(slot),info -> telePad.teleport(info.first(),player));
	}
}