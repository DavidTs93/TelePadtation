package me.DMan16.TelePadtation.Managers;

import me.DMan16.TelePadtation.Classes.Pair;
import me.DMan16.TelePadtation.Classes.TelePadInfoPlaceable;
import me.DMan16.TelePadtation.Classes.TelePadInfoPortable;
import me.DMan16.TelePadtation.TelePadItems.*;
import me.DMan16.TelePadtation.TelePads.TelePadAdvanced;
import me.DMan16.TelePadtation.TelePads.TelePadBasic;
import me.DMan16.TelePadtation.TelePads.TelePadPocket;
import me.DMan16.TelePadtation.TelePads.TelePadStandard;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class RecipesManager {
	private static final @NotNull String KEY_STANDARD_2 = TelePadStandard.KEY + "2";
	private static final @NotNull NamespacedKey KEY_NAMESPACED_STANDARD_2 = Utils.namespacedKey(KEY_STANDARD_2);
	private static final @NotNull String KEY_ADVANCED_2 = TelePadAdvanced.KEY + "2";
	private static final @NotNull NamespacedKey KEY_NAMESPACED_ADVANCED_2 = Utils.namespacedKey(KEY_ADVANCED_2);
	private static final @NotNull List<@NotNull NamespacedKey> KEYS = Arrays.asList(TelePadItemBasic.KEY_NAMESPACED,TelePadItemStandard.KEY_NAMESPACED,KEY_NAMESPACED_STANDARD_2,TelePadItemAdvanced.KEY_NAMESPACED,KEY_NAMESPACED_ADVANCED_2);
	
	private final List<@NotNull ShapedRecipe> recipes = new ArrayList<>(6);
	
	public RecipesManager() throws Exception {
		removeRecipes();
		if (!TelePadtationMain.TelePadsManager().registerPlaceable(new TelePadInfoPlaceable<>(TelePadItemBasic::isTelePadItemBasic,TelePadItemBasic::new,TelePadBasic::new))) throw new Exception("Failed to register \"basic\" TelePad!");
		if (!TelePadtationMain.TelePadsManager().registerPlaceable(new TelePadInfoPlaceable<>(TelePadItemStandard::isTelePadItemStandard,TelePadItemStandard::new,TelePadStandard::new))) throw new Exception("Failed to register \"standard\" TelePad!");
		if (!TelePadtationMain.TelePadsManager().registerPlaceable(new TelePadInfoPlaceable<>(TelePadItemAdvanced::isTelePadItemAdvanced,TelePadItemAdvanced::new,TelePadAdvanced::new))) throw new Exception("Failed to register \"advanced\" TelePad!");
		if (!TelePadtationMain.TelePadsManager().registerPortable(new TelePadInfoPortable<>(TelePadItemPocket::isTelePadItemPocket,TelePadItemPocket::new))) throw new Exception("Failed to register \"pocket\" TelePad!");
		addRecipes();
	}
	
	private void addRecipe(@NotNull ShapedRecipe recipe) {
		Bukkit.addRecipe(recipe);
		this.recipes.add(recipe);
	}
	
	public void addRecipes() {
		if (!this.recipes.isEmpty()) return;
		addRecipe(recipeTelePadBasic());
		addRecipe(recipeTelePadStandard());
		addRecipe(recipeTelePadStandard2());
		addRecipe(recipeTelePadAdvanced());
		addRecipe(recipeTelePadAdvanced2());
		addRecipe(recipeTelePadPocket());
	}
	
	public void removeRecipes() {
		KEYS.forEach(Bukkit::removeRecipe);
		this.recipes.clear();
	}
	
	@NotNull
	@SafeVarargs
	private static ShapedRecipe recipe(@NotNull String key,@NotNull NamespacedKey namespacedKey,@NotNull Supplier<@NotNull TelePadItem<?>> constructor,@NotNull String[] shape,@NotNull Pair<@NotNull Character,@NotNull Material> @NotNull ... ingredients) {
		ShapedRecipe recipe = new ShapedRecipe(namespacedKey,constructor.get().toItem());
		recipe.setGroup(key);
		recipe.shape(shape);
		for (Pair<Character,Material> ingredient : ingredients) recipe.setIngredient(ingredient.first(),ingredient.second());
		return recipe;
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadBasic() {
		return recipe(TelePadBasic.KEY,TelePadItemBasic.KEY_NAMESPACED,TelePadItemBasic::new,new String[] {"ECE","SSS"},new Pair<>('E',Material.ENDER_PEARL),new Pair<>('C',Material.COMPASS),new Pair<>('S',Material.SANDSTONE_SLAB));
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadStandard() {
		return recipe(TelePadStandard.KEY,TelePadItemStandard.KEY_NAMESPACED,TelePadItemStandard::new,new String[] {"YCY","ESE","QQQ"},new Pair<>('Y',Material.ENDER_EYE),new Pair<>('C',Material.COMPASS),new Pair<>('E',Material.ENDER_PEARL),new Pair<>('S',Material.SANDSTONE_SLAB),new Pair<>('Q',Material.QUARTZ_SLAB));
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadStandard2() {
		return recipe(KEY_STANDARD_2,KEY_NAMESPACED_STANDARD_2,TelePadItemStandard::new,new String[] {"YTY","QQQ"},new Pair<>('Q',Material.QUARTZ_SLAB),new Pair<>('Y',Material.ENDER_EYE)).setIngredient('T',new RecipeChoice.ExactChoice(new TelePadItemBasic().toItem()));
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadAdvanced() {
		return recipe(TelePadAdvanced.KEY,TelePadItemAdvanced.KEY_NAMESPACED,TelePadItemAdvanced::new,new String[] {"YYY","BNB","BBB"},new Pair<>('Y',Material.ENDER_EYE),new Pair<>('B',Material.END_STONE_BRICKS),new Pair<>('N',Material.NETHERITE_INGOT));
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadAdvanced2() {
		return recipe(KEY_ADVANCED_2,KEY_NAMESPACED_ADVANCED_2,TelePadItemAdvanced::new,new String[] {" N ","BTB","BBB"},new Pair<>('B',Material.END_STONE_BRICKS),new Pair<>('N',Material.NETHERITE_INGOT)).setIngredient('T',new RecipeChoice.ExactChoice(new TelePadItemStandard().toItem()));
	}
	
	@NotNull
	private ShapedRecipe recipeTelePadPocket() {
		return recipe(TelePadPocket.KEY,TelePadItemPocket.KEY_NAMESPACED,TelePadItemPocket::new,new String[] {"B","E"},new Pair<>('E',Material.ENDER_PEARL)).setIngredient('B',new RecipeChoice.MaterialChoice(Tag.BUTTONS));
	}
}