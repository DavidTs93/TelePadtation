package me.DMan16.TelePadtation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Menu {
	private final List<PairInt> smallBox = Arrays.asList(pairInt(-1,0),pairInt(0,-1),pairInt(0,1),pairInt(1,0));
	private final List<PairInt> bigBox = Arrays.asList(pairInt(-1,-1),pairInt(-1,0),pairInt(-1,1),pairInt(0,-1),pairInt(0,1),
			pairInt(1,-1),pairInt(1,0),pairInt(1,1));
	private final static int defaultLines = 5;
	private final PairInt nextLoc = pairInt(-1,-1);
	private final PairInt prevLoc = pairInt(-1,1);
	private final int edgeTop = 1;
	private final int edgeBot = 1;
	private TelePadsManager manager = TelePadtation.TelePadsManager;
	
	private Inventory inv;
	private HashMap<Integer,Button> invMap;
	final Player player;
	private HashMap<Integer,List<Button>> pages;
	private int page;
	private final int lines;
	private int main;
	private int fill;
	private int privateTelePads;
	private int globalTelePads;
	final Location location;
	
	private Menu(Player player, Location location, int lines) {
		this.player = player;
		this.invMap = new HashMap<Integer,Button>();
		this.lines = lines;
		this.location = location;
		inv = Bukkit.createInventory(player,lines * 9,Utils.chatColors(TelePadtation.getPluginNameColors().replace("&","&l&")));
		createPages();
		openMenu();
		if (location != null) main();
		else privateTelePads();
	}
	
	PairInt pairInt(int a1, int a2) {
		return new PairInt(a1,a2);
	}

	public Menu(Player player, Location location) {
		this(player,location,defaultLines);
	}

	public Menu(Player player, org.bukkit.Location location) {
		this(player, new Location(location));
	}
	
	private void openMenu() {
		if (inv != null) {
			player.closeInventory();
			if (TelePadtation.MenuManager.containsKey(player)) {
				TelePadtation.MenuManager.replace(player,this);
			} else {
				TelePadtation.MenuManager.put(player,this);
			}
			player.openInventory(inv);
		}
	}
	
	int size() {
		return inv.getSize();
	}
	
	boolean isFill() {
		return (page >= fill && page < privateTelePads);
	}
	
	boolean isPrivateTelePads() {
		return (page >= privateTelePads && page < globalTelePads);
	}
	
	boolean isGlobalTelePads() {
		return (page >= globalTelePads);
	}
	
	void button(int loc, Button button) {
		if (loc < 0 || loc >= inv.getSize()) return;
		inv.setItem(loc,null);
		invMap.remove(loc);
		inv.setItem(loc,button == null ? null : button.item());
		invMap.put(loc,button == null ? null : (button.item() == null ? null : button));
	}
	
	void button(int line, int loc, Button button) {
		loc = (loc - (loc > 1 ? 1 : -1)) % 9 + (loc > 1 ? 1 : -1);
		line = (line - (line > 1 ? 1 : -1)) % lines + (line > 1 ? 1 : -1);
		if (line == 0 || loc == 0) return;
		if (line < 0) {
			line += lines + 1;
		}
		if (loc < 0) {
			loc += 9 + 1;
		}
		button(loc(line,loc),button);
	}
	
	void button(PairInt loc, Button button) {
		button(loc.first,loc.second,button);
	}
	
	int loc(int line, int loc) {
		if (line == 0 || loc == 0) return -1;
		if (line < 0) {
			line += lines + 1;
		}
		if (loc < 0) {
			loc += 9 + 1;
		}
		return (line - 1) * 9 + loc - 1;
	}
	
	int loc(PairInt loc) {
		int a = loc.first;
		int b = loc.second;
		return loc(a,b);
	}
	
	PairInt loc(int loc) {
		return pairInt((loc / 9) + 1,(loc % 9) + 1);
	}
	
	void main() {
		setPage(main);
	}
	
	void fill() {
		setPage(fill);
		ItemStack item = TelePad.fuel.clone();
		for (int i = 0; i < manager.get(location).extra(); i++) inv.addItem(item);
	}
	
	void toggleTelePads() {
		if (isPrivateTelePads()) globalTelePads();
		else privateTelePads();
	}
	
	void privateTelePads() {
		setPage(privateTelePads);
	}
	
	void globalTelePads() {
		setPage(globalTelePads);
	}
	
	private void createPages() {
		pages = new HashMap<Integer,List<Button>>();
		addPages(null);
		main = addPages(null);
		addPages(null);
		fill = addPages(createFill());
		addPages(null);
		privateTelePads = addPages(getTelePads(manager.getPrivate(player.getUniqueId().toString())));
		addPages(null);
		globalTelePads = addPages(getTelePads(manager.TelePadsGlobal));
	}
	
	private List<Button> createFill() {
		List<Button> buttons = new ArrayList<Button>();
		for (int i = 0; i < TelePad.fillSlots; i++) {
			buttons.add(null);
		}
		return buttons;
	}
	
	private List<Button> getTelePads(List<Location> telePads) {
		List<Button> buttons = new ArrayList<Button>();
		if (telePads != null) for (Location telePad : telePads) if (this.location == null || !telePad.equals(this.location))
			buttons.add(this.location == null ? Buttons.TelePad(manager.TelePadsSingleUse.get(player.getUniqueId().toString()),telePad) :
				Buttons.TelePad(this.location,telePad));
		return buttons;
	}
	
	private int nextAddPage() {
		return (pages == null || pages.isEmpty()) ? 0 : Collections.max(pages.keySet()) + 1;
	}
	
	int addPages(List<Button> buttons) {
		List<Button> content = new ArrayList<Button>();
		int start = nextAddPage();
		if (buttons == null || buttons.isEmpty()) {
			for (int i = 0; i < 9 * (lines - edgeTop - edgeBot); i ++) {
				content.add(null);
			}
			pages.put(start,content);
		} else {
			for (Button button : buttons) {
				if (content.size() == 9 * (lines - edgeTop - edgeBot)) {
					pages.put(nextAddPage(),content);
					content = new ArrayList<Button>();
				}
				content.add(button);
			}
			if (!content.isEmpty()) {
				for (int i = content.size(); i < inv.getSize() - (9 * (edgeTop + edgeBot)); i++) {
					content.add(Buttons.empty());
				}
				pages.put(nextAddPage(),content);
			}
		}
		return start;
	}
	
	void back() {
		if (isFill()) {
			int charge = 0;
			for (int i = 0; i < TelePad.fillSlots; i++) {
				ItemStack item = getInv(loc(2,1) + i);
				if (TelePad.isFuel(item)) charge += item.getAmount();
			}
			manager.get(location).extra(charge);
		}
		main();
	}
	
	void smallBox(PairInt loc, Button item) {
		box(loc,smallBox,item);
	}
	
	void bigBox(PairInt loc, Button item) {
		box(loc,bigBox,item);
	}
	
	void box(PairInt loc, List<PairInt> box, Button item) {
		for (PairInt add : box) {
			button(loc.add(add),item);
		}
	}
	
	boolean isPageEmpty(int page) {
		List<Button> current = pages.get(page);
		for (Button button : current) {
			if (button != null) return false;
		}
		return true;
	}
	
	void setPage(int page) {
		if (page >= 0 && page < pages.size()) {
			this.page = page;
			update();
		}
	}
	
	boolean isNext() {
		return (page < pages.size() - 1 && !isPageEmpty(page) && !isPageEmpty(page + 1));
	}
	
	boolean isPrev() {
		return (page > 0 && !isPageEmpty(page) && !isPageEmpty(page - 1));
	}
	
	void next() {
		if (isNext()) setPage(page + 1);
	}
	
	void previous() {
		if (isPrev()) setPage(page - 1);
	}
	
	void update() {
		TelePad telePad = location == null ? manager.TelePadsSingleUse.get(player.getUniqueId().toString()) : manager.get(location);
		telePad.recharge();
		if (pages != null) {
			for (int i = 0; i < inv.getSize(); i++) {
				button(i,Buttons.empty());
			}
			for (int i = 1; i <= 9; i++) {
				button(1,i,Buttons.edge());
				button(-1,i,Buttons.edge());
			}
			button(-1,5,Buttons.close());
			if (page == main) {
				PairInt TelePads = pairInt(3,5);
				PairInt info = pairInt(3,-2);
				if (telePad.global() && !telePad.ownerID().equals(player.getUniqueId().toString()) && !player.isOp()) {
					TelePads = TelePads.add(0,-1);
					info = info.add(0,1);
				} else {
					Button removeButton = Buttons.remove(location);
					PairInt remove = pairInt(3,2);
					button(remove,removeButton);
				}
				button(TelePads,Buttons.TelePads(location));
				button(info,Buttons.info(location));
			} else {
				if (location != null) button(1,1,Buttons.back());
				List<Button> current = pages.get(page) == null ? new ArrayList<Button>() : pages.get(page);
				for (int i = 9 * edgeTop; i < inv.getSize() - (9 * edgeBot); i++) {
					Button button = current.get(i - (9 * edgeTop));
					if (page >= privateTelePads && button == null) button = Buttons.empty();
					button(i,button);
				}
				if (isNext()) button(nextLoc,Buttons.next());
				if (isPrev()) button(prevLoc,Buttons.previous());
				if (isFill() && player.isOp()) button(1,-1,Buttons.toggleGlobal(location));
				if (isPrivateTelePads() || isGlobalTelePads()) button(1,-1,Buttons.toggleTelePads(isPrivateTelePads()));
			}
		}
	}
	
	Button get(int loc) {
		return invMap.get(loc);
	}
	
	ItemStack getInv(int loc) {
		return inv.getItem(loc);
	}
	
	class PairInt {
		final int first;
		final int second;
		
		PairInt(int first, int second) {
			this.first = first;
			this.second = second;
		}
		
		boolean equals(PairInt pair) {
			return (first == pair.first) && (second == pair.second);
		}
		
		PairInt add(PairInt add) {
			return new PairInt(this.first + add.first,this.second + add.second);
		}
		
		PairInt add(int first, int second) {
			return add(new PairInt(first,second));
		}
	}
}