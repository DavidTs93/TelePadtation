package me.DMan16.TelePadtation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("deprecation")
public class Recipes implements Listener {
	List<ShapedRecipe> recipes;
	private final ShapedRecipe compass;
	
	Recipes() {
		recipes = new ArrayList<ShapedRecipe>();
		recipes.add(recipeTelePad1());
		recipes.add(recipeTelePad2());
		recipes.add(recipeTelePad2_2());
		recipes.add(recipeTelePad3());
		recipes.add(recipeTelePad3_2());
		recipes.add(recipePocketTelePad());
		for (ShapedRecipe recipe : recipes) Bukkit.addRecipe(recipe);
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		ShapedRecipe compass = null;
		while (recipes.hasNext()) {
			Recipe recipe = recipes.next();
			if (recipe instanceof ShapedRecipe && recipe.getResult() != null && recipe.getResult().getType() == Material.COMPASS) {
				compass = (ShapedRecipe) recipe;
				break;
			}
		}
		this.compass = compass;
		if (Bukkit.getServer().getPluginManager().getPlugin("StarDirt") != null) {
			me.DMan16.StarDirt.Recipes.Recipes.newRecipes.add(recipeTelePad1());
			me.DMan16.StarDirt.Recipes.Recipes.newRecipes.add(recipeTelePad2());
			me.DMan16.StarDirt.Recipes.Recipes.newRecipes.add(recipeTelePad3());
			me.DMan16.StarDirt.Recipes.Recipes.newRecipes.add(recipePocketTelePad());
		}
	}
	
	static ItemStack get(int limit) {
		if (TelePad.maxUses.contains(limit)) return TelePad(limit);
		return null;
	}
	
	private static ItemStack TelePad(int limit) {
    	ItemStack item = new ItemStack(Material.END_PORTAL_FRAME);
    	ItemMeta meta = item.getItemMeta();
    	meta.setDisplayName(Utils.chatColors("&bTele&6Pad"));
    	List<String> lore = new ArrayList<String>();
    	lore.add("");
    	lore.add("&6Max uses: &e" + limit);
    	meta.setLore(Utils.chatColors(lore));
    	meta.getPersistentDataContainer().set(Utils.namespacedKey("telepad_" + limit),PersistentDataType.STRING,"protected");
    	item.setItemMeta(meta);
		return item;
	}
	
	static ShapedRecipe recipeTelePad1() {
		ItemStack item = TelePad(TelePad.maxUses.get(0));
    	NamespacedKey key = Utils.namespacedKey("telepad_1");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
    	recipe.setGroup("telepad1");
		recipe.shape("ECE","SSS");
		recipe.setIngredient('E',Material.ENDER_PEARL);
		recipe.setIngredient('C',Material.COMPASS);
		recipe.setIngredient('S',Material.SANDSTONE_SLAB);
		return recipe;
	}
	
	static ShapedRecipe recipeTelePad2() {
		ItemStack item = TelePad(TelePad.maxUses.get(1));
    	NamespacedKey key = Utils.namespacedKey("telepad_2");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
    	recipe.setGroup("telepad2");
		recipe.shape("YCY","ESE","QQQ");
		recipe.setIngredient('Y',Material.ENDER_EYE);
		recipe.setIngredient('C',Material.COMPASS);
		recipe.setIngredient('E',Material.ENDER_PEARL);
		recipe.setIngredient('S',Material.SANDSTONE_SLAB);
		recipe.setIngredient('Q',Material.QUARTZ_SLAB);
		return recipe;
	}
	
	static ShapedRecipe recipeTelePad2_2() {
		ItemStack item = TelePad(TelePad.maxUses.get(1));
    	NamespacedKey key = Utils.namespacedKey("telepad_2_2");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
    	recipe.setGroup("telepad2");
		recipe.shape("YTY","QQQ");
		recipe.setIngredient('T', new RecipeChoice.ExactChoice(recipeTelePad1().getResult()));
		recipe.setIngredient('Q',Material.QUARTZ_SLAB);
		recipe.setIngredient('Y',Material.ENDER_EYE);
		return recipe;
	}
	
	static ShapedRecipe recipeTelePad3() {
		ItemStack item = TelePad(TelePad.maxUses.get(2));
    	NamespacedKey key = Utils.namespacedKey("telepad_3");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
    	recipe.setGroup("telepad3");
		recipe.shape("YYY","BNB","BBB");
		recipe.setIngredient('Y',Material.ENDER_EYE);
		recipe.setIngredient('B',Material.END_STONE_BRICKS);
		recipe.setIngredient('N',Material.NETHERITE_INGOT);
		return recipe;
	}
	
	static ShapedRecipe recipeTelePad3_2() {
		ItemStack item = TelePad(TelePad.maxUses.get(2));
    	NamespacedKey key = Utils.namespacedKey("telepad_3_2");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
    	recipe.setGroup("telepad3");
		recipe.shape(" N ","BTB","BBB");
		recipe.setIngredient('T', new RecipeChoice.ExactChoice(recipeTelePad2_2().getResult()));
		recipe.setIngredient('B',Material.END_STONE_BRICKS);
		recipe.setIngredient('N',Material.NETHERITE_INGOT);
		return recipe;
	}
	
	static ShapedRecipe recipePocketTelePad() {
		ItemStack item = Buttons.pocketTelePad().item();
    	NamespacedKey key = Utils.namespacedKey("telepad_pocket");
    	ShapedRecipe recipe = new ShapedRecipe(key,item);
		recipe.shape("B","E");
		recipe.setIngredient('B', new RecipeChoice.MaterialChoice(Tag.BUTTONS));
		recipe.setIngredient('E',Material.ENDER_PEARL);
		return recipe;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDiscover(PlayerRecipeDiscoverEvent event) {
		if (Bukkit.getRecipe(event.getRecipe()).getResult().getType() == Material.COMPASS) discover(recipeTelePad1(),event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasDiscoveredRecipe(compass.getKey())) discover(recipeTelePad1(),event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCraft(CraftItemEvent event) {
		if (!(event.getRecipe() instanceof ShapedRecipe)) return;
		Player player = (Player) event.getWhoClicked();
		if ((((ShapedRecipe) event.getRecipe()).getKey()).equals(recipeTelePad1().getKey())) {
			discover(recipeTelePad2(),player);
			discover(recipeTelePad2_2(),player);
			discover(recipePocketTelePad(),player);
		} else if ((((ShapedRecipe) event.getRecipe()).getKey()).equals(recipeTelePad2().getKey()) ||
				(((ShapedRecipe) event.getRecipe()).getKey()).equals(recipeTelePad2_2().getKey())) {
			discover(recipeTelePad3(),player);
			discover(recipeTelePad3_2(),player);
		}
	}
	
	private void discover(ShapedRecipe recipe, Player player) {
		NamespacedKey key = recipe.getKey();
		if (key != null && !player.hasDiscoveredRecipe(key)) player.discoverRecipe(key);
	}
}