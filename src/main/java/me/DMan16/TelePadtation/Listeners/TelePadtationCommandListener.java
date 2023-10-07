package me.DMan16.TelePadtation.Listeners;

import me.DMan16.TelePadtation.TelePadItems.TelePadItem;
import me.DMan16.TelePadtation.TelePads.TelePadCommand;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TelePadtationCommandListener implements CommandExecutor,TabCompleter {
	private static final List<String> BASE = Arrays.asList("help","open","reload","give");
	
	private final PluginCommand command;
	
	public TelePadtationCommandListener() {
		this.command = Objects.requireNonNull(TelePadtationMain.getInstance().getCommand("TelePadtation"));
		this.command.setExecutor(this);
		this.command.setDescription(Utils.chatColors(TelePadtationMain.PLUGIN_NAME_COLORS + "&f command"));
		this.command.setUsage(TelePadtationMain.languageManager().usage());
	}
	
	public boolean onCommand(@NotNull CommandSender sender,@NotNull Command cmd,@NotNull String label,@NotNull String @NotNull [] args) {
		int idx;
		if (args.length == 0 || (idx = BASE.indexOf(Utils.toLowercase(args[0]))) == 1) {
			if (!(sender instanceof Player)) {
				TelePadtationMain.languageManager().playerOnly(sender);
				return true;
			}
			Player player = (Player) sender;
			new TelePadCommand(player).access(player,EquipmentSlot.HAND);
			return true;
		}
		if (idx <= 0) return false;
		if (idx == 2) {
			TelePadtationMain.configManager().reload();
			TelePadtationMain.languageManager().reload();
			this.command.setUsage(TelePadtationMain.languageManager().usage());
			return true;
		} if (idx == 3) {
			if (args.length == 1) return false;
			TelePadItem<?> telePadItem = TelePadtationMain.TelePadsManager().get(args[1]);
			if (telePadItem == null) {
				TelePadtationMain.languageManager().typeNotFound(sender,args[1]);
				return true;
			}
			Player player;
			if (args.length == 2) {
				if (!(sender instanceof Player)) {
					TelePadtationMain.languageManager().playerOnly(sender);
					return true;
				}
				player = (Player) sender;
			} else player = Bukkit.getPlayer(args[2]);
			if (player == null || !player.isOnline()) TelePadtationMain.languageManager().playerNotFound(sender,args[2]);
			else if (player.getInventory().addItem(telePadItem.toItem()).isEmpty()) TelePadtationMain.languageManager().playerGive(sender,player.getName(),telePadItem);
			else TelePadtationMain.languageManager().playerFullInventory(sender,player.getName(),telePadItem);
		} else return false;
		return true;
	}
	
	@NotNull
	public List<String> onTabComplete(@NotNull CommandSender sender,@NotNull Command command,@NotNull String alias,@NotNull String @NotNull [] args) {
		if (args.length == 0) return BASE;
		if (args.length == 1) return BASE.stream().filter(name -> containsTabComplete(args[0],name)).collect(Collectors.toList());
		int idx = BASE.indexOf(Utils.toLowercase(args[0]));
		return idx == 3 ? TelePadtationMain.TelePadsManager().getTypes() : new ArrayList<>();
	}
	
	private static boolean containsTabComplete(String arg1,String arg2) {
		return (Utils.isNullOrEmpty(arg1) || Utils.toLowercase(arg2).contains(Utils.toLowercase(arg1)));
	}
}