package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.Events.TelePadPostRemoveEvent;
import me.DMan16.TelePadtation.Interfaces.Backable;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TelePadMenu<T extends TelePad> extends ListenerInventory {
	private static final @NotNull ItemStack ITEM_BORDER;
	private static final @NotNull ItemStack AIR = new ItemStack(Material.AIR);
	
	static {
		ITEM_BORDER = Utils.setDisplayNameLore(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)," ",null);
	}
	
	protected final @NotNull T telePad;
	protected final @Range(from = 2,to = 6) int lines;
	protected final @NotNull String title;
	protected final @NotNull Player player;
	protected final @NotNull TelePadStatus status;
	protected final @NotNull @Unmodifiable Set<@NotNull Integer> borders;
	protected final int rightClickJump;
	protected int currentPage = 1;
	
	protected TelePadMenu(@NotNull T telePad,@Range(from = 1,to = 5) int lines,@NotNull String title,@NotNull Player player,@NotNull TelePadStatus status) {
		super(Bukkit.createInventory(player,(lines + 1) * LINE_SIZE,title),player.getUniqueId());
		if (((telePad instanceof TelePad.TelePadPortable) && status != TelePadStatus.PORTABLE) || ((telePad instanceof TelePad.TelePadPlaceable) && ((telePad.isGlobal() && status != TelePadStatus.GLOBAL) || (!telePad.isActive() && status != TelePadStatus.INACTIVE))))
			throw new IllegalArgumentException("TelePad status is incorrect!");
		this.telePad = telePad;
		this.lines = lines + 1;
		this.title = title;
		this.player = player;
		this.status = status;
		this.borders = Collections.unmodifiableSet(IntStream.range(0,size).filter(slot -> (slot >= 0 && slot < 9) || (slot >= size - 9 && slot < size) || (slot % 9) == 0 || ((slot + 1) % 9) == 0).boxed().collect(Collectors.toSet()));
		this.rightClickJump = TelePadtationMain.configManager().rightClickJump();
	}
	
	protected TelePadMenu(@NotNull T telePad,@Range(from = 1,to = 5) int lines,@NotNull String title,@NotNull Player player) {
		this(telePad,lines,title,player,telePad.status());
	}
	
	protected boolean canEditPrivate() {
		return telePad.isPrivate() && telePad.isOwner(player);
	}
	
	protected boolean testPermissionGlobal() {
		return player.hasPermission("telepadtation.global");
	}
	
	protected boolean canEditGlobal() {
		return status == TelePadStatus.GLOBAL && testPermissionGlobal();
	}
	
	protected boolean canEdit() {
		return !telePad.isPortable() && (canEditPrivate() || canEditGlobal());
	}
	
	protected void open() {
		open(TelePadtationMain.getInstance(),player);
	}
	
	protected final int currentPage() {
		return currentPage;
	}
	
	protected boolean isBorder(int slot) {
		return borders.contains(slot);
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemBorder() {
		return ITEM_BORDER.clone();
	}
	
	protected int slotInfo() {
		return 4;
	}
	
	protected int slotNext() {
		return size - 1;
	}
	
	protected int slotPrevious() {
		return size - 9;
	}
	
	protected int slotClose() {
		return size - 5;
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemInfo() {
		return TelePadtationMain.configManager().itemMenu(telePad,status);
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemNext() {
		return Utils.setDisplayNameLore(new ItemStack(Material.ARROW),TelePadtationMain.languageManager().nameNext(),TelePadtationMain.languageManager().loreNext());
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemPrevious() {
		return Utils.setDisplayNameLore(new ItemStack(Material.ARROW),TelePadtationMain.languageManager().namePrevious(),TelePadtationMain.languageManager().lorePrevious());
	}
	
	@NotNull
	@Contract(" -> new")
	protected ItemStack itemClose() {
		return Utils.setDisplayNameLore(new ItemStack(Material.BARRIER),TelePadtationMain.languageManager().nameClose(),null);
	}
	
	protected final void setInfo() {
		setItem(slotInfo(),itemInfo());
	}
	
	protected final void setNextPrevious() {
		setItem(slotNext(),currentPage() < maxPage() ? itemNext() : ITEM_BORDER);
		setItem(slotPrevious(),currentPage() > 1 ? itemPrevious() : ITEM_BORDER);
	}
	
	protected final void setClose() {
		setItem(slotClose(),itemClose());
	}
	
	protected void setUpdatingButtons() {
		setNextPrevious();
		setClose();
	}
	
	protected void setBack() {
		if (this instanceof Backable) Utils.runNotNull(((Backable) this).slotBack(),slot -> setItem(slot,((Backable) this).itemBack()));
	}
	
	protected void setButtons() {
		setInfo();
		setBack();
		setUpdatingButtons();
	}
	
	protected void setBorders() {
		borders.forEach(slot -> setItem(slot,ITEM_BORDER));
		setButtons();
	}
	
	protected void prepareAndOpen() {
		setBorders();
		setPage(1);
		open();
	}
	
	protected void clickClose(@NotNull ClickType click) {
		TelePadtationMain.taskChainFactory().newChain().delay(1).sync(this::close).execute();
	}
	
	private int change(@NotNull ClickType click) {
		return click.isRightClick() ? rightClickJump : 1;
	}
	
	protected void clickNext(@NotNull ClickType click) {
		changePage(change(click));
	}
	
	protected void clickPrevious(@NotNull ClickType click) {
		changePage(-change(click));
	}
	
	protected void clickInfo(@NotNull ClickType click) {}
	
	protected void clearInside() {
		IntStream.range(9,size - 9).filter(slot -> !isBorder(slot)).boxed().forEach(slot -> setItem(slot,null));
	}
	
	protected abstract int maxPage();
	
	protected void changePage(int num) {
		setPage(Math.min(Math.max(currentPage + num,1),maxPage()));
	}
	
	protected abstract void setPageContents();
	
	public void setPage(int newPage) {
		if (newPage < 1 || newPage > maxPage()) return;
		currentPage = newPage;
		clearInside();
		setPageContents();
		setUpdatingButtons();
	}
	
	protected boolean checkCancelled(@NotNull InventoryClickEvent event) {
		return event.isCancelled();
	}
	
	protected boolean cancelCheck(int slot,int inventorySlot,@NotNull ClickType click,@NotNull InventoryAction action,int hotbarSlot) {
		return true;
	}
	
	protected boolean clickCheck(@NotNull ClickType click) {
		return click != ClickType.DOUBLE_CLICK && (click.isRightClick() || click.isLeftClick() || click.isCreativeAction());
	}
	
	protected boolean slotCheck(int slot,int inventorySlot,@NotNull ClickType click,@NotNull InventoryAction action,int hotbarSlot) {
		return !click.isCreativeAction() && slot >= 0;
	}
	
	@Contract("null -> true")
	protected boolean isEmptyItem(@Nullable ItemStack item) {
		return Utils.isNull(item);
	}
	
	protected void emptyItem(@NotNull InventoryClickEvent event,int slot,@NotNull ClickType click,boolean isNull) {}
	
	protected boolean shouldSetNext() {
		return currentPage < maxPage();
	}
	
	protected boolean shouldSetPrevious() {
		return currentPage > 1;
	}
	
	@EventHandler
	public void onTelePadRemove(TelePadPostRemoveEvent event) {
		if (!event.telePad().equals(telePad)) return;
		telePad.setRemoved();
		cancelCloseUnregister = false;
		close();
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!equals(event) || checkCancelled(event)) return;
		int slot = event.getRawSlot();
		int inventorySlot = event.getSlot();
		ClickType click = event.getClick();
		InventoryAction action = event.getAction();
		int hotbar = event.getHotbarButton();
		if (cancelCheck(slot,inventorySlot,click,action,hotbar)) event.setCancelled(true);
		if (clickCheck(click) && slotCheck(slot,inventorySlot,click,action,hotbar)) {
			ItemStack slotItem = event.getView().getItem(slot);
			if (isEmptyItem(slotItem)) emptyItem(event,slot,click,Utils.isNull(slotItem));
			else if (slot == slotClose()) clickClose(click);
			else if (slot == slotNext() && shouldSetNext()) clickNext(click);
			else if (slot == slotPrevious() && shouldSetPrevious()) clickPrevious(click);
			else if (slot == slotInfo()) clickInfo(click);
			else if ((this instanceof Backable) && Objects.equals(slot,((Backable) this).slotBack())) ((Backable) this).goBack(click);
			else otherSlot(event,slot,slotItem,click);
		}
		if (!event.isCancelled() || click != ClickType.SWAP_OFFHAND) return;
		ItemStack item = player.getInventory().getItemInOffHand();
		ItemStack offHand = Utils.isNull(item) ? null : item;
		TelePadtationMain.taskChainFactory().newChain().sync(() -> {
			player.getInventory().setItemInOffHand(AIR);
			player.getInventory().setItemInOffHand(offHand);
		}).execute();
	}
	
	protected abstract void otherSlot(@NotNull InventoryClickEvent event,int slot,ItemStack slotItem,@NotNull ClickType click);
}