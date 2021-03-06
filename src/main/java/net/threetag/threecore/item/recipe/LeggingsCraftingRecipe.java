package net.threetag.threecore.item.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.threetag.threecore.util.RecipeUtil;

public class LeggingsCraftingRecipe extends AbstractConstructionTableRecipe {

    public static final IRecipeType<LeggingsCraftingRecipe> RECIPE_TYPE = RecipeUtil.register("leggings_crafting");

    public LeggingsCraftingRecipe(ResourceLocation id, String group, NonNullList<Ingredient> recipeItems, Ingredient toolIngredient, ItemStack recipeOutput, boolean consumesTool) {
        super(id, group, recipeItems, toolIngredient, recipeOutput, consumesTool);
    }

    @Override
    public IRecipeSerializer<LeggingsCraftingRecipe> getSerializer() {
        return TCRecipeSerializers.LEGGINGS_CRAFTING.get();
    }

    @Override
    public IRecipeType<LeggingsCraftingRecipe> getType() {
        return RECIPE_TYPE;
    }

    public static class Serializer extends AbstractConstructionTableRecipe.Serializer<LeggingsCraftingRecipe> {

        public Serializer() {
            super(new String[]{
                    "XXX",
                    "XX",
                    "XX",
                    "XX"
            });
        }

        @Override
        public LeggingsCraftingRecipe create(ResourceLocation id, String group, NonNullList<Ingredient> recipeItems, Ingredient toolIngredient, ItemStack result, boolean consumesTool) {
            return new LeggingsCraftingRecipe(id, group, recipeItems, toolIngredient, result, consumesTool);
        }
    }
}
