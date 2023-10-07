package me.DMan16.TelePadtation.Utils;

import me.DMan16.TelePadtation.TelePadtationMain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
	public static final String PURPLE = "&#7033ad";
	public static final String VERSION_STR = Bukkit.getServer().getVersion().split("\\(MC:")[1].split("\\)")[0].trim().split(" ")[0].trim();
	/**
	 * 1
	 */
	public static final int VERSION_MAIN;
	public static final int VERSION;
	public static final Integer SUBVERSION;
	public static final @NotNull Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([a-fA-F0-9]{4})");
	public static final @NotNull Function<String,Character> UNICODE_FUNCTION = str -> (char) Integer.parseInt(str,16);
	public static final @NotNull Pattern COLOR_PATTERN = Pattern.compile("&(#[a-fA-F0-9]{6})");
	public static final @NotNull Function<String,ChatColor> COLOR_FUNCTION = ChatColor::of;
	
	static {
		String[] versionStr = VERSION_STR.split("\\.");
		VERSION_MAIN = Integer.parseInt(versionStr[0]);
		VERSION = Integer.parseInt(versionStr[1]);
		SUBVERSION = versionStr.length > 2 ? Integer.parseInt(versionStr[2]) : null;
	}
	
	public static void runNotNull(@Nullable Runnable run) {
		if (run != null) run.run();
	}
	
	public static <V> void runNotNull(@Nullable V obj,@Nullable Consumer<@NotNull V> apply) {
		if (apply != null && obj != null) apply.accept(obj);
	}
	
	public static <V,T> T applyNotNull(@Nullable V obj,@NotNull Function<@NotNull V,T> apply) {
		return obj == null ? null : apply.apply(obj);
	}
	
	public static <V,U,T> T applyNotNull(@Nullable V obj,@NotNull BiFunction<@NotNull V,U,T> apply,U arg) {
		return applyNotNull(obj,o -> apply.apply(o,arg));
	}
	
	@Contract("null -> true")
	public static boolean isNullOrEmpty(@Nullable String str) {
		return str == null || str.trim().isEmpty();
	}
	
	@Contract("null -> true")
	public static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
	
	@NotNull
	public static String matchAndReplace(@NotNull final String str,@NotNull Pattern pattern,@NotNull Function<String,?> replace) {
		int lastIndex = 0;
		StringBuilder output = new StringBuilder();
		Matcher matcher = pattern.matcher(str);
		Object replaced;
		while (matcher.find()) {
			replaced = replace.apply(matcher.group(1));
			if (replaced == null) continue;
			output.append(str,lastIndex,matcher.start()).append(replaced);
			lastIndex = matcher.end();
		}
		if (lastIndex < str.length()) output.append(str,lastIndex,str.length());
		return output.toString();
	}
	
	@Nullable
	@Contract("null -> null; !null -> !null")
	public static String unicode(@Nullable String str) {
		return applyNotNull(str,s -> matchAndReplace(s,UNICODE_PATTERN,UNICODE_FUNCTION));
	}
	
	@NotNull
	public static String colors(char colorChar,@NotNull String str) {
		return ChatColor.translateAlternateColorCodes(colorChar,matchAndReplace(str,COLOR_PATTERN,COLOR_FUNCTION));
	}
	
	@Nullable
	@Contract("null -> null; !null -> !null")
	public static String colors(@Nullable String str) {
		return applyNotNull(str,s -> colors('&',s));
	}
	
	/**
	 * @return Converts to color code using &.
	 * 1.16+ HEX colors can be used via &#??????.
	 */
	@Nullable
	@Contract("null -> null; !null -> !null")
	public static String chatColors(@Nullable String str) {
		return colors(unicode(str));
	}
	
	@Nullable
	@Contract("null,_ -> null; !null,_ -> !null")
	public static List<String> chatColors(@Nullable Collection<String> collection,boolean nullToEmpty) {
		return applyNotNull(collection,c -> (nullToEmpty ? c.stream().map(str -> str == null ? "" : str) : c.stream().filter(Objects::nonNull)).map(str -> isNullOrEmpty(str) ? "" : chatColors(str)).collect(Collectors.toList()));
	}
	
	@Nullable
	@Contract("null -> null; !null -> !null")
	public static List<String> chatColors(@Nullable Collection<String> collection) {
		return chatColors(collection,false);
	}
	
	@NotNull
	public static String chatColorsPlugin(@NotNull String str) {
		return chatColors(PURPLE + "[" + TelePadtationMain.PLUGIN_NAME_COLORS + PURPLE + "]&r " + str);
	}
	
	public static void log(@NotNull String str) {
		Bukkit.getLogger().info(str);
	}
	
	public static void chatColorsLogPlugin(@NotNull String str) {
		log(chatColorsPlugin(str));
	}
	
	public static NamespacedKey namespacedKey(String name) {
		return new NamespacedKey(TelePadtationMain.getInstance(),name);
	}
	
	@Contract(value = "null -> true",pure = true)
	public static boolean isNull(@Nullable Material material) {
		return material == null || material.isAir();
	}
	
	@Contract(value = "null -> true",pure = true)
	public static boolean isNull(@Nullable ItemStack item) {
		return item == null || isNull(item.getType()) || item.getAmount() <= 0;
	}
	
	@Contract(value = "null -> false",pure = true)
	public static boolean notNull(@Nullable ItemStack item) {
		return !isNull(item);
	}
	
	@NotNull
	public static String toLowercase(@NotNull String str) {
		return str.toLowerCase(Locale.ENGLISH);
	}
	
	@Nullable
	@Contract("null -> null")
	public static String fixKey(@Nullable String key) {
		if (key == null) return null;
		key = toLowercase(key.trim()).replace(" ","_").replace("-","_");
		return key.isEmpty() ? null : key;
	}
	
	@NotNull
	public static <V> Supplier<V> supplier(@Nullable V obj) {
		return () -> obj;
	}
	
	public static <V extends Event> void callEvent(@NotNull V event) {
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
	
	/**
	 * @return true if the event is NOT cancelled, false if the event IS cancelled
	 */
	public static <V extends Event & Cancellable> boolean callEventCancellable(@NotNull V event) {
		Bukkit.getServer().getPluginManager().callEvent(event);
		return !event.isCancelled();
	}
	
	public static int executeUpdateFailNoResults(@NotNull PreparedStatement statement) throws SQLException {
		int result = statement.executeUpdate();
		if (result <= 0) throw new SQLException("Nothing updated!");
		return result;
	}
	
	@Nullable
	@Contract("null -> null; !null -> !null")
	public static <V> V self(@Nullable V val) {
		return val;
	}
	
	@NotNull
	public static ItemMeta setDisplayNameLore(@NotNull ItemMeta meta,@Nullable Supplier<@Nullable String> displayName,@Nullable Supplier<@Nullable List<String>> lore) {
		if (displayName != null) meta.setDisplayName(displayName.get());
		if (lore != null) meta.setLore(lore.get());
		return meta;
	}
	
	@NotNull
	public static ItemMeta setDisplayNameLore(@NotNull ItemMeta meta,@Nullable String displayName,@Nullable List<String> lore) {
		return setDisplayNameLore(meta,supplier(displayName),supplier(lore));
	}
	
	@NotNull
	public static ItemStack setDisplayNameLore(@NotNull ItemStack item,@Nullable Supplier<@Nullable String> displayName,@Nullable Supplier<@Nullable List<String>> lore) {
		item.setItemMeta(setDisplayNameLore(Objects.requireNonNull(item.getItemMeta()),displayName,lore));
		return item;
	}
	
	@NotNull
	public static ItemStack setDisplayNameLore(@NotNull ItemStack item,@Nullable String displayName,@Nullable List<String> lore) {
		return setDisplayNameLore(item,supplier(displayName),supplier(lore));
	}
	
	public static void givePlayer(@NotNull Player player,@NotNull World world,@NotNull Location loc,@NotNull Collection<ItemStack> items) {
		if (items.isEmpty()) return;
		Collection<ItemStack> leftovers = items;
		if (player.isOnline() && !player.isDead()) leftovers = player.getInventory().addItem(items.stream().filter(Utils::notNull).toArray(ItemStack[]::new)).values();
		if (!leftovers.isEmpty()) leftovers.forEach(item -> world.dropItemNaturally(loc,item));
	}
	
	@NotNull
	public static <V,T> List<@NotNull HashMap<@NotNull Integer,@NotNull T>> generateCompactPages(@NotNull Iterator<@Nullable V> iter,@NotNull Function<@Nullable V,@Nullable T> convert,@Nullable Function<@NotNull T,@NotNull Boolean> isNull,
																								 @Range(from = 0,to = 5) int startLine,@Range(from = 0,to = 5) int endLine,@Range(from = 0,to = 8) int startColumn,@Range(from = 0,to = 8) int endColumn,
																								 @Nullable Function<@NotNull Integer,@NotNull Boolean> ignoreSlot,@Nullable Function<@NotNull Integer,@NotNull Boolean> allowSlot) {
		if (endLine < startLine) throw new IllegalArgumentException("End line (" + endLine + ") is smaller than the start line (" + startLine + ")");
		if (endColumn < startColumn) throw new IllegalArgumentException("End column (" + endColumn + ") is smaller than the start column (" + startColumn + ")");
		List<HashMap<Integer,T>> pages = new ArrayList<>();
		List<Integer> slots = new ArrayList<>();
		int slot;
		for (int i = startLine; i <= endLine; i++) for (int j = startColumn; j <= endColumn; j++) {
			slot = (i * 9) + j;
			if ((allowSlot == null || allowSlot.apply(slot)) && (ignoreSlot == null || !ignoreSlot.apply(slot))) slots.add(slot);
		}
		if (slots.isEmpty()) return pages;
		LinkedHashMap<Integer,T> page = null;
		while (iter.hasNext()) {
			if (page == null) page = new LinkedHashMap<>();
			for (int i = 0; i < slots.size() && iter.hasNext(); i++) {
				T item = convert.apply(iter.next());
				if (item != null && (isNull == null || !isNull.apply(item))) page.put(slots.get(i),item);
			}
			if (!page.isEmpty()) {
				pages.add(page);
				page = null;
			}
		}
		return pages;
	}
}