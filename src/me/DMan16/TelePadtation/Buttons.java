package me.DMan16.TelePadtation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

public class Buttons {
	private final static Material head = Material.PLAYER_HEAD;
	private static TelePadsManager manager = TelePadtation.TelePadsManager;
	
	static Button next() {
		Consumer<Pair<Menu,Location>> method = (info) -> {info.getFirst().next();};
		return createButton(head,"&6Next Page",null,heads.NEXT,method);
	}
	
	static Button previous() {
		Consumer<Pair<Menu,Location>> method = (info) -> {info.getFirst().previous();};
		return createButton(head,"&6Previous Page",null,heads.PREVIOUS,method);
	}
	
	static Button back() {
		Consumer<Pair<Menu,Location>> method = (info) -> {
			info.getFirst().back();
		};
		return createButton(head,"&dBack",null,heads.BACK,method);
	}
	
	static Button close() {
		Consumer<Pair<Menu,Location>> method = (info) -> {info.getFirst().player.closeInventory();};
		return createButton(head,"&cClose",null,heads.CLOSE,method);
	}
	
	static Button empty() {
		return createButton(Material.BLACK_STAINED_GLASS_PANE," ",null,null,null);
	}
	
	static Button edge() {
		return createButton(Material.WHITE_STAINED_GLASS_PANE," ",null,null,null);
	}
	
	private static List<String> loreTelePad(Location location) {
		TelePad telePad = manager.get(location);
		List<String> lore = new ArrayList<String>();
		lore.add(telePad.global() ? "&9Global" : (telePad.canUse() ? "&a&oACTIVE" : "&c&oINACTIVE"));
		lore.add("");
		lore.add("&6World: &f&o" + location.world);
		lore.add("&6Coords: &f(&o" + location.x + "," + location.y + "," + location.z + "&f)");
		lore.add("");
		lore.add("&6Max uses: &e" + (telePad.global() ? "∞" : telePad.limit()));
		lore.add("&6Uses left: &f&o" + (telePad.global() ? "∞" : (telePad.limit() - telePad.used())));
		return lore;
	}
	
	static Button info(Location location) {
		TelePad telePad = manager.get(location);
		Consumer<Pair<Menu,Location>> method = (info) -> {info.getFirst().fill();};
		List<String> lore = loreTelePad(location);
		if (telePad.global()) lore.add(0,manager.getGlobalName(location));
		return createButton(head,"&a&lInfo",lore,telePad.canUse() ? heads.ACTIVE : heads.INACTIVE,method);
	}

	static Button remove(Location location) {
		Consumer<Pair<Menu,Location>> method = (info) -> {
			Player player = info.getFirst().player;
			player.closeInventory();
			Bukkit.getWorld(location.world).getBlockAt(location.x,location.y,location.z).setType(Material.AIR);
			if (!player.getGameMode().equals(GameMode.CREATIVE)) {
				ItemStack item = manager.get(location).toItem();
				if (item != null) player.getInventory().addItem(item);
			}
			manager.remove(location);
		};
		return createButton(Material.BARRIER,"&c&lRemove",null,null,method);
	}

	static Button TelePad(Location origin, Location destination) {
		return TelePad(manager.get(origin),destination);
	}

	static Button TelePad(TelePad telePadOrigin, Location destination) {
		TelePad telePadDestination = manager.get(destination);
		Consumer<Pair<Menu,Location>> method = (info) -> {
			if (telePadDestination.canUse() && telePadOrigin.canUse()) telePadOrigin.use(destination,info.getFirst().player);
		};
		String name = null;
		if (telePadDestination.global()) name = manager.getGlobalName(destination);
		if (name == null) name = "&bTele&6Pad";
		return createButton(head,name,loreTelePad(destination),telePadDestination.canUse() ? heads.ACTIVE : heads.INACTIVE,method);
	}

	static Button TelePads(Location location) {
		TelePad telePad = manager.get(location);
		Consumer<Pair<Menu,Location>> method = (info) -> {
			if (telePad.canUse()) info.getFirst().privateTelePads();;
		};
		return createButton(telePad.canUse() ? Material.END_PORTAL_FRAME : Material.END_STONE,"&b&lTele&6Pad&b&ls",
				Arrays.asList(telePad.canUse() ? "&a&oACTIVE" : "&c&oINACTIVE"),null,method);
	}

	static Button toggleGlobal(Location location) {
		TelePad telePad = manager.get(location);
		Consumer<Pair<Menu,Location>> method = (info) -> {
			if (info.getFirst().isFill() && info.getFirst().player.isOp()) {
				telePad.toggleGlobal();
				if (telePad.global()) manager.TelePadsGlobal.add(location);
				else manager.TelePadsGlobal.remove(location);
				info.getFirst().button(1,-1,toggleGlobal(location));
			}
		};
		return createButton(head,"&aGlobal Option",Arrays.asList("&bConvert to " + (telePad.global() ? "&4&oPrivate" : "&2&oGlobal") + " &bTele&6Pad"),
				telePad.global() ? null : heads.GLOBAL,method);
	}

	static Button toggleTelePads(boolean isPrivate) {
		Consumer<Pair<Menu,Location>> method = (info) -> {
			info.getFirst().toggleTelePads();
			info.getFirst().button(1,-1,toggleTelePads(!isPrivate));
		};
		return createButton(head,"&bTele&6Pads",Arrays.asList("&bShow " + (isPrivate ? "&2&oGlobal" : "&4&oPrivate")),
				isPrivate ? heads.GLOBAL : null,method);
	}

	static Button pocketTelePad() {
		return createButton(head,"&ePocket &bTele&6Pad",Arrays.asList("","&7&l(&e&l!&7&l) &7Right click to use."),heads.POCKET,null);
	}
	
	private static ItemStack getHead(String skin) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta = setSkullSkin(meta,skin);
		head.setItemMeta(meta);
		return head;
	}
	
	private static ItemStack getHead(heads info) {
		return getHead(info.skin);
	}
	
	private static Button createButton(Material type, String name, List<String> lore, Object skin, Consumer<Pair<Menu,Location>> method, Location info) {
		ItemStack item = new ItemStack(type);
		if (type == Material.PLAYER_HEAD && skin != null) {
			if (skin instanceof heads) {
				item = getHead((heads) skin);
			} else {
				item = getHead((String) skin);
			}
		}
		ItemMeta meta = item.getItemMeta();
		if (name != null) {
			meta.setDisplayName(Utils.chatColors(name));
		}
		if (lore != null) {
			meta.setLore(Utils.chatColors(lore));
		}
		item.setItemMeta(meta);
		return new Button(item,method,info);
	}

	private static Button createButton(Material type, String name, List<String> lore, Object skin, Consumer<Pair<Menu,Location>> method) {
		return createButton(type,name,lore,skin,method,null);
	}
	
	private static SkullMeta setSkullSkin(SkullMeta skullMeta, String skin) {
		if (skin == null || skin == "" || skin.isEmpty()) return null;
		try {
			Method metaSetProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
			metaSetProfileMethod.setAccessible(true);
			UUID id = new UUID(skin.substring(skin.length() - 20).hashCode(),skin.substring(skin.length() - 10).hashCode());
			GameProfile profile = new GameProfile(id,"1");
			profile.getProperties().put("textures", new Property("textures", skin));
			metaSetProfileMethod.invoke(skullMeta, profile);
		} catch (Exception e) {
			return null;
		}
		return skullMeta;
	}
	
	private enum heads{
		NEXT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0="),
		PREVIOUS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0="),
		BACK("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="),
		CLOSE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZjNjBkYTQxNGJmMDM3MTU5YzhiZThkMDlhOGVjYjkxOWJmODlhMWEyMTUwMWI1YjJlYTc1OTYzOTE4YjdiIn19fQ=="),
		ACTIVE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWNiN2MyMWNjNDNkYzE3Njc4ZWU2ZjE2NTkxZmZhYWIxZjYzN2MzN2Y0ZjZiYmQ4Y2VhNDk3NDUxZDc2ZGI2ZCJ9fX0="),
		INACTIVE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4Y2ZhZmE1ZjAzZjhhZWYwNDJhMTQzNzk5ZTk2NDM0MmRmNzZiN2MxZWI0NjFmNjE4ZTM5OGY4NGE5OWE2MyJ9fX0="),
		GLOBAL("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM0NjdhNTMxOTc4ZDBiOGZkMjRmNTYyODVjNzI3MzRkODRmNWVjODhlMGI0N2M0OTMyMzM2Mjk3OWIzMjNhZiJ9fX0="),
		POCKET("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7ImlkIjoiMTkwM2NhNWE3MjgzNDExODk5NjMwYTY5OTM3MTY3NmMiLCJ0eXBlIjoiU0tJTiIsInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSIsInByb2ZpbGVJZCI6IjgwMThhYjAwYjJhZTQ0Y2FhYzliZjYwZWY5MGY0NWU1IiwidGV4dHVyZUlkIjoiMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSJ9fSwic2tpbiI6eyJpZCI6IjE5MDNjYTVhNzI4MzQxMTg5OTYzMGE2OTkzNzE2NzZjIiwidHlwZSI6IlNLSU4iLCJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkiLCJwcm9maWxlSWQiOiI4MDE4YWIwMGIyYWU0NGNhYWM5YmY2MGVmOTBmNDVlNSIsInRleHR1cmVJZCI6IjJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkifSwiY2FwZSI6bnVsbH0=");
		
		private final String skin;
		
		private heads(String skin) {
			this.skin = skin;
		}
	}
}