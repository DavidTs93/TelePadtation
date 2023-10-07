package me.DMan16.TelePadtation.Menus;

import me.DMan16.TelePadtation.Classes.Pair;
import me.DMan16.TelePadtation.Enums.Heads;
import me.DMan16.TelePadtation.Enums.TelePadStatus;
import me.DMan16.TelePadtation.Events.TelePadPostRemoveEvent;
import me.DMan16.TelePadtation.Events.TelePadPreRemoveEvent;
import me.DMan16.TelePadtation.Interfaces.Backable;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public final class TelePadMenuEdit extends TelePadMenu<TelePad.TelePadPlaceable> implements Backable {
	private static final @NotNull String CANCEL = "cancel";
	
	private final @Nullable @Unmodifiable List<TelePad.@NotNull TelePadPlaceable> destinations;
	private final @Nullable TelePadMenuTeleportOpener opener;
	private final boolean isGlobal;
	private final boolean accessFuel;
	private final boolean changeGlobal;
	private boolean disabled;
	
	public TelePadMenuEdit(TelePad.@NotNull TelePadPlaceable telePad,@NotNull String title,@NotNull Player player,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations,@Nullable TelePadMenuTeleportOpener opener) throws IllegalAccessException {
		super(telePad,4,title,player,null,status);
		if (status == TelePadStatus.PORTABLE) throw new IllegalAccessException("Cannot edit Portable TelePads!");
		if (!canEdit()) throw new IllegalAccessException("The player doesn't have editing access to the TelePad!");
		this.destinations = destinations;
		this.opener = opener;
		this.isGlobal = status == TelePadStatus.GLOBAL;
		this.accessFuel = canEditPrivate() && !this.isGlobal;
		this.changeGlobal = canEditPrivate() && testPermissionGlobal();
		prepareAndOpen();
	}
	
	@Override
	protected boolean canEdit() {
		return canEditPrivate() || canEditGlobal() || telePad.canAccessRemove(player);
	}
	
	@Override
	protected boolean slotCheck(int slot,int inventorySlot,@NotNull ClickType click,@NotNull InventoryAction action,int hotbarSlot) {
		if (!super.slotCheck(slot,inventorySlot,click,action,hotbarSlot)) return false;
		if (!disabled) return true;
		if (slot == slotClose()) clickClose(click);
		return false;
	}
	
	private void goBack(@NotNull ClickType click,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations) {
		if (opener != null) opener.open(telePad,title,player,status,destinations);
	}
	
	public void goBack(@NotNull ClickType click) {
		goBack(click,status,destinations);
	}
	
	@Nullable
	public Integer slotBack() {
		return opener == null ? null : 0;
	}
	
	@Nullable
	public Integer slotGlobal() {
		return changeGlobal ? 20 : null;
	}
	
	@Nullable
	public Integer slotFuelDisplayname() {
		return accessFuel || changeGlobal ? 22 : null;
	}
	
	public int slotRemove() {
		return accessFuel || changeGlobal ? 24 : 22;
	}
	
	@NotNull
	private ItemStack itemBack(boolean isSubMenu) {
		return Utils.setDisplayNameLore(new ItemStack(Material.ARROW),TelePadtationMain.languageManager().nameBack(),isSubMenu ? TelePadtationMain.languageManager().loreBack() : null);
	}
	
	@NotNull
	public ItemStack itemBack() {
		return itemBack(false);
	}
	
	@NotNull
	public ItemStack itemFuelDisplayname() {
		return Utils.setDisplayNameLore(new ItemStack(accessFuel ? Material.ENDER_PEARL : Material.WRITABLE_BOOK),accessFuel ? TelePadtationMain.languageManager().nameFuel() : TelePadtationMain.languageManager().nameDisplayname(),
				accessFuel ? TelePadtationMain.languageManager().loreFuel() : TelePadtationMain.languageManager().loreDisplayname());
	}
	
	@NotNull
	public ItemStack itemGlobal() {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		item.setItemMeta(Heads.setSkin((SkullMeta) Objects.requireNonNull(item.getItemMeta()),isGlobal ? TelePadtationMain.configManager().skinGeneralGlobal() : TelePadtationMain.configManager().skinGeneralPrivate()));
		return Utils.setDisplayNameLore(item,TelePadtationMain.languageManager().nameChange(isGlobal),TelePadtationMain.languageManager().loreChange(isGlobal));
	}
	
	@NotNull
	public ItemStack itemRemove() {
		return Utils.setDisplayNameLore(new ItemStack(Material.REDSTONE_BLOCK),TelePadtationMain.languageManager().nameRemove(),TelePadtationMain.languageManager().loreRemove());
	}
	
	protected int maxPage() {
		return 1;
	}
	
	@Override
	protected void setUpdatingButtons() {
		Utils.runNotNull(slotGlobal(),slot -> setItem(slot,itemGlobal()));
		Utils.runNotNull(slotFuelDisplayname(),slot -> setItem(slot,itemFuelDisplayname()));
		setItem(slotRemove(),itemRemove());
		super.setUpdatingButtons();
	}
	
	protected void setPageContents() {}
	
	protected void otherSlot(@NotNull InventoryClickEvent event,int slot,ItemStack slotItem,@NotNull ClickType click) {
		if (disabled) return;
		if (slot == slotRemove()) new TelePadMenuConfirmRemove(this);
		else if (Objects.equals(slot,slotFuelDisplayname())) {
			if (accessFuel) new TelePadMenuFuel(this);
			else {
				cancelCloseUnregister = true;
				startDisplaynameConversation();
			}
		} else if (Objects.equals(slot,slotGlobal())) telePad.toggleGlobal(player,this::setUpdatingButtons,limit -> TelePadtationMain.languageManager().limitReached(player,limit),() -> TelePadtationMain.languageManager().errorDatabase(player));
	}
	
	private void startDisplaynameConversation() {
		new ConversationFactory(TelePadtationMain.getInstance()).withLocalEcho(false).withFirstPrompt(new Prompt() {
			@NotNull
			public String getPromptText(@NotNull ConversationContext context) {
				return TelePadtationMain.languageManager().enterDisplayname();
			}
			
			public boolean blocksForInput(@NotNull ConversationContext context) {
				return true;
			}
			
			@Nullable
			public Prompt acceptInput(@NotNull ConversationContext context,@Nullable String input) {
				if (!CANCEL.equalsIgnoreCase(input)) {
					disabled = true;
					telePad.setName(input,() -> {
						setInfo();
						disabled = false;
					},() -> {
						TelePadtationMain.languageManager().errorDatabase(player);
						disabled = false;
					});
					open();
				}
				return END_OF_CONVERSATION;
			}
		}).buildConversation(player);
	}
	
	@FunctionalInterface
	public interface TelePadMenuTeleportOpener {
		void open(TelePad.@NotNull TelePadPlaceable telePad,@NotNull String title,@NotNull Player player,@NotNull TelePadStatus status,@Nullable List<TelePad.@NotNull TelePadPlaceable> destinations);
	}
	
	@NotNull
	private static List<@NotNull ItemStack> amountItems(@NotNull ItemStack item,int amount) {
		int stack = item.getMaxStackSize();
		List<ItemStack> items = new ArrayList<>();
		while (amount > 0) {
			ItemStack clone = item.clone();
			int newAmount = amount - stack;
			clone.setAmount(newAmount >= 0 ? stack : amount);
			items.add(clone);
			amount = newAmount;
		}
		return items;
	}
	
	@Nullable
	private static List<@NotNull ItemStack> fuelItems(TelePad.@NotNull TelePadPlaceable telePad) {
		return telePad.extraFuel() > 0 ? amountItems(telePad.defaultFuel(),telePad.extraFuel()) : null;
	}
	
	private static class TelePadMenuConfirmRemove extends TelePadMenu<TelePad.TelePadPlaceable> implements Backable {
		private static final int SLOT_CONFIRM = 22;
		private static final @NotNull List<@NotNull Pair<Integer,Integer>> SLOTS = Arrays.asList(new Pair<>(10,34),new Pair<>(11,33),new Pair<>(12,32),new Pair<>(13,31),new Pair<>(14,30),new Pair<>(15,29),new Pair<>(16,28),
				new Pair<>(19,25),new Pair<>(20,24),new Pair<>(21,23),new Pair<>(SLOT_CONFIRM,null));
		private static final @NotNull ItemStack WAIT = Utils.setDisplayNameLore(new ItemStack(Material.RED_STAINED_GLASS_PANE)," ",null);
		private static final @NotNull ItemStack READY = Utils.setDisplayNameLore(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE)," ",null);
		
		private final @NotNull TelePadMenuEdit editMenu;
		private boolean ready = false;
		private BukkitTask task;
		private boolean disabled;
		
		public TelePadMenuConfirmRemove(@NotNull TelePadMenuEdit editMenu) {
			super(editMenu.telePad,4,editMenu.title,editMenu.player,null,editMenu.status);
			this.editMenu = editMenu;
			prepareAndOpen();
		}
		
		@Override
		protected void open() {
			super.open();
			if (task != null) task.cancel();
			task = new BukkitRunnable() {
				private final Iterator<@NotNull Pair<Integer,Integer>> iter = SLOTS.iterator();
				
				public void run() {
					if (!iter.hasNext()) {
						cancel();
						ready = true;
						task = null;
						return;
					}
					Pair<Integer,Integer> pair = iter.next();
					ItemStack item = iter.hasNext() ? READY : Utils.setDisplayNameLore(new ItemStack(Material.GREEN_STAINED_GLASS_PANE),TelePadtationMain.languageManager().nameConfirm(),TelePadtationMain.languageManager().loreConfirm());
					if (pair.first() != null) setItem(pair.first(),item);
					if (pair.second() != null) setItem(pair.second(),item);
				}
			}.runTaskTimer(TelePadtationMain.getInstance(),0,2);
		}
		
		@Override
		protected void afterClose(@Nullable InventoryCloseEvent event) {
			if (task != null) task.cancel();
		}
		
		@Override
		protected boolean slotCheck(int slot,int inventorySlot,@NotNull ClickType click,@NotNull InventoryAction action,int hotbarSlot) {
			if (!super.slotCheck(slot,inventorySlot,click,action,hotbarSlot)) return false;
			if (!disabled) return true;
			if (slot == slotClose()) clickClose(click);
			return false;
		}
		
		public void goBack(@NotNull ClickType click) {
			if (click.isLeftClick() || editMenu.opener == null) try {
				new TelePadMenuEdit(telePad,title,player,status,editMenu.destinations,editMenu.opener);
			} catch (IllegalAccessException e) {}
			else editMenu.goBack(click);
		}
		
		@NotNull
		public Integer slotBack() {
			return 0;
		}
		
		@NotNull
		public ItemStack itemBack() {
			return editMenu.itemBack(true);
		}
		
		protected int maxPage() {
			return 1;
		}
		
		protected void setPageContents() {
			IntStream.range(9,size - 9).filter(slot -> !isBorder(slot)).boxed().forEach(slot -> setItem(slot,WAIT));
		}
		
		protected void otherSlot(@NotNull InventoryClickEvent event,int slot,ItemStack slotItem,@NotNull ClickType click) {
			if (disabled || !ready || slot != SLOT_CONFIRM) return;
			if (!Utils.callEventCancellable(new TelePadPreRemoveEvent(telePad,player))) return;
			disabled = true;
			TelePadtationMain.databaseConnection().remove(telePad,editMenu.accessFuel ? player : null,owned -> {
				close();
				telePad.location().toBlock().setType(Material.AIR);
				if (status != TelePadStatus.GLOBAL) {
					List<ItemStack> items = fuelItems(telePad);
					if (items != null) {
						World world = telePad.location().world();
						Location loc = telePad.location().toLocation();
						items.forEach(i -> world.dropItemNaturally(loc,i));
					}
				}
				TelePadtationMain.languageManager().telePadRemoved(player,telePad,owned == null ? null : new Pair<>(owned,TelePadtationMain.configManager().limit(player)));
				Utils.callEvent(new TelePadPostRemoveEvent(telePad,player));
			},() -> {
				TelePadtationMain.languageManager().errorDatabase(player);
				disabled = false;
			});
		}
	}
	
	private static class TelePadMenuFuel extends TelePadMenu<TelePad.TelePadPlaceable> implements Backable {
		private static final @NotNull List<@NotNull Integer> INNER_SLOTS = Arrays.asList(11,12,13,14,15);
		
		private final @NotNull TelePadMenuEdit editMenu;
		private boolean disabled;
		
		public TelePadMenuFuel(@NotNull TelePadMenuEdit editMenu) {
			super(editMenu.telePad,2,editMenu.title,editMenu.player,null,editMenu.status);
			this.editMenu = editMenu;
			prepareAndOpen();
		}
		
		@Override
		protected boolean isBorder(int slot) {
			return !INNER_SLOTS.contains(slot);
		}
		
		@Override
		protected boolean slotCheck(int slot,int inventorySlot,@NotNull ClickType click,@NotNull InventoryAction action,int hotbarSlot) {
			if (!super.slotCheck(slot,inventorySlot,click,action,hotbarSlot)) return false;
			if (!disabled) return true;
			if (slot == slotClose()) clickClose(click);
			return false;
		}
		
		@Override
		protected void setBorders() {
			setItem(10,itemBorder());
			setItem(16,itemBorder());
			super.setBorders();
		}
		
		private void afterClose(@Nullable Consumer<@NotNull Boolean> run) {
			int amount = 0;
			for (Integer slot : INNER_SLOTS) {
				ItemStack item = getItem(slot);
				if (telePad.isFuel(item)) amount += item.getAmount();
			}
			int diff = amount - telePad.extraFuel();
			if (diff != 0) telePad.setExtraFuel(amount,run == null ? null : () -> run.accept(true),() -> {
				TelePadtationMain.languageManager().errorDatabase(player);
				if (diff > 0) Utils.givePlayer(player,telePad.location().world(),telePad.location().toLocation(),amountItems(telePad.defaultFuel(),diff));
				if (run != null) run.accept(false);
			});
			else if (run != null) run.accept(false);
		}
		
		@Override
		protected void afterClose(@Nullable InventoryCloseEvent event) {
			if (!disabled) afterClose((Consumer<Boolean>) null);
		}
		
		public void goBack(@NotNull ClickType click) {
			disabled = true;
			BiFunction<@NotNull TelePadStatus,@Nullable List<TelePad.@NotNull TelePadPlaceable>,@NotNull Runnable> run = (status,destinations) -> click.isRightClick() && editMenu.opener != null ? () -> editMenu.goBack(click,status,destinations) : () -> {
				try {
					new TelePadMenuEdit(telePad,title,player,status,destinations,editMenu.opener);
				} catch (IllegalAccessException e) {}
			};
			Runnable runRegular = () -> run.apply(status,editMenu.destinations);
			Runnable runChangedInactive = () -> {
				Function<@NotNull TelePadStatus,@NotNull Runnable> runStatus = status -> {
					if (status.isInactive()) return runRegular;
					return () -> TelePadtationMain.databaseConnection().getApplicableTelePads(player,destinations -> run.apply(status,destinations),() -> {
						close();
						TelePadtationMain.languageManager().errorDatabase(player);
					});
				};
				telePad.recharge(() -> runStatus.apply(telePad.status()),runRegular);
			};
			afterClose(changed -> (changed && status == TelePadStatus.INACTIVE ? runChangedInactive : runRegular).run());
		}
		
		@NotNull
		public Integer slotBack() {
			return 0;
		}
		
		@NotNull
		public ItemStack itemBack() {
			return editMenu.itemBack(true);
		}
		
		protected int maxPage() {
			return 1;
		}
		
		protected void setPageContents() {
			Iterator<ItemStack> items = Utils.applyNotNull(fuelItems(telePad),List::iterator);
			if (items == null) return;
			Iterator<Integer> iter = INNER_SLOTS.iterator();
			while (iter.hasNext() && items.hasNext()) setItem(iter.next(),items.next());
		}
		
		protected void otherSlot(@NotNull InventoryClickEvent event,int slot,ItemStack slotItem,@NotNull ClickType click) {
			if (disabled) return;
			InventoryAction action = event.getAction();
			switch (action) {
				case PLACE_ALL:
				case PLACE_SOME:
				case PLACE_ONE:
				case SWAP_WITH_CURSOR:
				case MOVE_TO_OTHER_INVENTORY:
				case HOTBAR_SWAP:
					break;
				case NOTHING:
				case PICKUP_ALL:
				case PICKUP_SOME:
				case PICKUP_HALF:
				case PICKUP_ONE:
				case DROP_ALL_CURSOR:
				case DROP_ONE_CURSOR:
				case DROP_ALL_SLOT:
				case DROP_ONE_SLOT:
				case HOTBAR_MOVE_AND_READD:
				case CLONE_STACK:
				case COLLECT_TO_CURSOR:
				case UNKNOWN:
					return;
			}
			if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				if (!legalSlot(slot) && !Utils.isNull(slotItem) && !telePad.isFuel(slotItem)) event.setCancelled(true);
				return;
			}
			if (action == InventoryAction.HOTBAR_SWAP) {
				if (legalSlot(slot)) {
					ItemStack item = click == ClickType.SWAP_OFFHAND ? player.getInventory().getItemInOffHand() : player.getInventory().getItem(event.getHotbarButton());
					if (!Utils.isNull(item) && !telePad.isFuel(item)) event.setCancelled(true);
				}
				return;
			}
			if (!telePad.isFuel(slotItem)) event.setCancelled(true);
		}
	}
}