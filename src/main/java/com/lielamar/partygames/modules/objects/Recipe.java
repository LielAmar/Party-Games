package com.lielamar.partygames.modules.objects;

import org.bukkit.Material;

public class Recipe {

    private RecipeType recipeType;

    public Recipe(RecipeType recipeType) {
        this.recipeType = recipeType;
    }

    public RecipeType getRecipeType() {
        return this.recipeType;
    }

    public enum RecipeType {
        DIAMOND_BOOTS(Material.DIAMOND_BOOTS, new Material[][] {
                {Material.DIAMOND,    Material.DIAMOND,         null},
                {null,                null,                     null},
                {Material.DIAMOND,    Material.DIAMOND,         null},
        }),
        DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, new Material[][] {
                {Material.DIAMOND,    Material.DIAMOND,    Material.DIAMOND},
                {Material.DIAMOND,    null,                null},
                {Material.DIAMOND,    Material.DIAMOND,    Material.DIAMOND},
        }),
        DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, new Material[][] {
                {Material.DIAMOND,    Material.DIAMOND,    Material.DIAMOND},
                {null,                Material.DIAMOND,    Material.DIAMOND},
                {Material.DIAMOND,    Material.DIAMOND,    Material.DIAMOND},
        }),
        DIAMOND_HELMET(Material.DIAMOND_HELMET, new Material[][] {
                {Material.DIAMOND,    Material.DIAMOND,    null},
                {Material.DIAMOND,    null,                null},
                {Material.DIAMOND,    Material.DIAMOND,    null},
        }),

        IRON_BOOTS(Material.IRON_BOOTS, new Material[][] {
                {Material.IRON_INGOT,    Material.IRON_INGOT,         null},
                {null,                   null,                     null},
                {Material.IRON_INGOT,    Material.IRON_INGOT,         null},
        }),
        IRON_LEGGINGS(Material.IRON_LEGGINGS, new Material[][] {
                {Material.IRON_INGOT,    Material.IRON_INGOT,    Material.IRON_INGOT},
                {Material.IRON_INGOT,    null,                   null},
                {Material.IRON_INGOT,    Material.IRON_INGOT,    Material.IRON_INGOT},
        }),
        IRON_CHESTPLATE(Material.IRON_CHESTPLATE, new Material[][] {
                {Material.IRON_INGOT,    Material.IRON_INGOT,    Material.IRON_INGOT},
                {null,                   Material.IRON_INGOT,    Material.IRON_INGOT},
                {Material.IRON_INGOT,    Material.IRON_INGOT,    Material.IRON_INGOT},
        }),
        IRON_HELMET(Material.IRON_HELMET, new Material[][] {
                {Material.IRON_INGOT,    Material.IRON_INGOT,    null},
                {Material.IRON_INGOT,    null,                   null},
                {Material.IRON_INGOT,    Material.IRON_INGOT,    null},
        }),

        GOLD_BOOTS(Material.GOLD_BOOTS, new Material[][] {
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,         null},
                {null,                   null,                     null},
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,         null},
        }),
        GOLD_LEGGINGS(Material.GOLD_LEGGINGS, new Material[][] {
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    Material.GOLD_INGOT},
                {Material.GOLD_INGOT,    null,                   null},
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    Material.GOLD_INGOT},
        }),
        GOLD_CHESTPLATE(Material.GOLD_CHESTPLATE, new Material[][] {
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    Material.GOLD_INGOT},
                {null,                   Material.GOLD_INGOT,    Material.GOLD_INGOT},
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    Material.GOLD_INGOT},
        }),
        GOLD_HELMET(Material.GOLD_HELMET, new Material[][] {
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    null},
                {Material.GOLD_INGOT,    null,                   null},
                {Material.GOLD_INGOT,    Material.GOLD_INGOT,    null},
        }),

        BOW(Material.BOW, new Material[][] {
                {null,              Material.STICK,    null},
                {Material.STICK,    null,              Material.STICK},
                {Material.WEB,      Material.WEB,      Material.WEB},
        }),

        FISHING_ROD(Material.FISHING_ROD, new Material[][] {
                {null,              null,             Material.STICK},
                {null,              Material.STICK,   null},
                {Material.STICK,    Material.WEB,     Material.WEB},
        }),

        COMPASS(Material.COMPASS, new Material[][] {
                {null,                   Material.IRON_INGOT,       null},
                {Material.IRON_INGOT,    Material.REDSTONE,   Material.IRON_INGOT},
                {null,                   Material.IRON_INGOT,       null},
        }),

        WATCH(Material.WATCH, new Material[][] {
                {null,                   Material.GOLD_INGOT,       null},
                {Material.GOLD_INGOT,    Material.REDSTONE,   Material.GOLD_INGOT},
                {null,                   Material.GOLD_INGOT,       null},
        });

        private Material product;
        private Material[][] ingredients;
        RecipeType(Material product, Material[][] ingredients) {
            this.product = product;
            this.ingredients = ingredients;
        }

        public Material getProduct() { return this.product; }
        public Material[][] getIngredients() {
            return this.ingredients;
        }
    }
}